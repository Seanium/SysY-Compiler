package midend.pass;

import midend.ir.BasicBlock;
import midend.ir.Function;
import midend.ir.Module;
import midend.ir.Value;
import midend.ir.inst.*;
import midend.ir.type.PointerType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

public class GCM implements IRPass {
    private final Module module;
    private final HashSet<Inst> visited;

    /**
     * 全局代码移动。Global Code Motion.
     */
    public GCM() {
        this.module = Module.getInstance();
        visited = new HashSet<>();
    }

    @Override
    public void run() {
        for (Function function : module.getNotLibFunctions()) {
            runGCM(function);
        }
    }

    private void runGCM(Function function) {
        if (function.getBasicBlocks().size() == 1) {
            return;
        }

        ArrayList<Inst> insts = new ArrayList<>();
        for (BasicBlock basicBlock : function.getBasicBlocks()) {
            insts.addAll(basicBlock.getInsts());
            for (Inst inst : basicBlock.getInsts()) {
                inst.setEarlyBlock(inst.getParentBlock());  // 初始化early块为所属块
            }
        }

        // 第一步 按逆后序进行schedule early
        visited.clear();
        for (Inst i : insts) {
            scheduleEarly(i);
        }
//        for (Inst i : insts) {
//            System.out.println(i + "\t\t【early】\t\t" + i.getEarlyBlock().getName());
//        }

        // 第二步 按后序进行schedule late
        visited.clear();
        Collections.reverse(insts); // 得到后序遍历顺序（指令）
        for (Inst i : insts) {
            scheduleLate(i);
        }
    }

    /**
     * schedule early 算法将指令移动到被所有操作数所支配的最浅基本块。
     */
    private void scheduleEarly(Inst i) {
        Function func = i.getParentBlock().getParentFunc();
        BasicBlock root = func.getBasicBlocks().get(0); // 入口基本块
        if (visited.contains(i) || isPinned(i)) {
            return;
        }
        visited.add(i);
        i.setEarlyBlock(root);
        for (Value operand : i.getOperandList()) {
            if (operand instanceof Inst x) {
                scheduleEarly(x);
                // schedule early 算法将指令移动到被所有操作数所支配的最浅基本块
                if (x.getEarlyBlock().getImmDomDepth() > i.getEarlyBlock().getImmDomDepth()) {
                    i.setEarlyBlock(x.getEarlyBlock());
                }
            }
        }
    }

    /**
     * schedule late 将一个指令移动到能够支配其所有 users 的最深基本块。
     */
    private void scheduleLate(Inst i) {
        if (visited.contains(i) || isPinned(i)) {
            return;
        }
        visited.add(i);
        BasicBlock lca = null;
        for (Value user : i.getUserList()) {
            if (user instanceof Inst y) {
                scheduleLate(y);
                BasicBlock use = y.getParentBlock();
                if (y instanceof PhiInst phiInst) {
                    // phi结点的use块不是phi所属的块，而是phi中选项i对应的前驱块
                    // phi的选项中，i可能不止出现一次，因此要找到所有i选项对应块的lca
                    for (int j = 0; j < phiInst.getOperandList().size(); j++) {
                        if (phiInst.getOperandList().get(j).equals(i)) {
                            use = phiInst.getCfgPreList().get(j);
                            lca = findLCA(lca, use);
                        }
                    }
                } else {
                    lca = findLCA(lca, use);    // 更新最近公共祖先
                }
            }
        }
//        if (lca.getImmDomDepth() < i.getEarlyBlock().getImmDomDepth()) {
//            StringBuilder gcmInfo = new StringBuilder();
//            gcmInfo.append(i);
//            gcmInfo.append("【lca】").append(lca.getName()).append("【depth】").append(lca.getImmDomDepth());
//            gcmInfo.append("【early】").append(i.getEarlyBlock().getName()).append("【depth】").append(i.getEarlyBlock().getImmDomDepth()).append("\n");
//            System.out.println(gcmInfo);
//        }

        // 第三步 select block
        BasicBlock best;
        if (!i.getUserList().isEmpty()) {
            assert lca != null;
            best = lca;

            // 为指令选择最终的基本块best, 选择的依据是循环深度尽可能浅, 且在支配树中的深度尽可能浅
            while (!lca.equals(i.getEarlyBlock())) {
                lca = lca.getImmDomBy();
                if (lca.getLoopDepth() < best.getLoopDepth()) {
                    best = lca;
                }
            }
        } else {
            best = i.getEarlyBlock();
        }

        // 确定具体插入位置，将i从原基本块移动到best
        i.getParentBlock().getInsts().remove(i);
        int index = best.getInsts().size() - 1; // 默认为最后一条指令之前
        for (Inst inst : best.getInsts()) {
            if (!inst.equals(i) && !(inst instanceof PhiInst) && inst.getOperandList().contains(i)) {
                // 如果被使用，将i插入到i首次被使用的指令inst之前
                index = best.getInsts().indexOf(inst);
                break;
            }
        }
        best.getInsts().add(index, i);
        i.setParentBlock(best);
    }

    /**
     * 返回a和b在支配树中的最近公共祖先。
     */
    private BasicBlock findLCA(BasicBlock a, BasicBlock b) {
        if (a == null) {
            return b;
        }
        while (a.getImmDomDepth() < b.getImmDomDepth()) {
            b = b.getImmDomBy();
        }
        while (a.getImmDomDepth() > b.getImmDomDepth()) {
            a = a.getImmDomBy();
        }
        while (!a.equals(b)) {
            a = a.getImmDomBy();
            b = b.getImmDomBy();
        }
        return a;
    }

    /**
     * 指令是否固定于其基本块。
     */
    private boolean isPinned(Inst inst) {
        if (inst instanceof BinaryInst || inst instanceof IcmpInst || inst instanceof GEPInst || inst instanceof ZextInst ||
                inst instanceof CallInst callInst && canBeMoved(callInst)) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * 判断callInst能否被移动。
     */
    private boolean canBeMoved(CallInst callInst) {
        Function targetFunc = callInst.getTargetFunc();
        // 有副作用的函数不能被移动
        if (targetFunc.hasSideEffect()) {
            return false;
        }
        // 未被使用的函数，无法分析其schedule late
        if (targetFunc.getUserList().isEmpty()) {
            return false;
        }
        // 存在实参不是简单变量，不能被移动
        for (Value arg : callInst.getArgs()) {
            if (arg instanceof GEPInst || arg instanceof LoadInst || arg.getType() instanceof PointerType) {
                return false;
            }
        }
        return true;
    }
}
