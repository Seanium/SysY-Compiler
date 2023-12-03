package midend.pass;

import midend.ir.BasicBlock;
import midend.ir.Function;
import midend.ir.Module;
import midend.ir.Value;
import midend.ir.inst.*;
import midend.ir.type.PointerType;

import java.util.ArrayList;
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
        if(function.getBasicBlocks().size()==1){
            return;
        }
        // 第一步 schedule early
        for (BasicBlock basicBlock : function.getBasicBlocks()) {
            for (Inst i : basicBlock.getInsts()) {
                if (isPinned(i)) {
                    visited.add(i);
                    for (Value operand : i.getOperandList()) {
                        if (operand instanceof Inst x) {
                            scheduleEarly(x);
                        }
                    }
                }
            }
        }
        // 第二步 schedule late
        visited.clear();
        for (BasicBlock basicBlock : function.getBasicBlocks()) {
            ArrayList<Inst> insts = new ArrayList<>(basicBlock.getInsts());     // 先拷贝再遍历
            for (Inst i : insts) {
                if (isPinned(i)) {
                    visited.add(i);
                    for (Value user : i.getUserList()) {
                        if (user instanceof Inst y) {
                            scheduleLate(y);
                        }
                    }
                }
            }
        }
    }

    private void scheduleEarly(Inst i) {
        Function func = i.getParentBlock().getParentFunc();
        BasicBlock root = func.getBasicBlocks().get(0); // 入口基本块
        if (visited.contains(i)) {
            return;
        }
        visited.add(i);
        i.setEarlyBlock(root);
        for (Value operand : i.getOperandList()) {
            if (operand instanceof Inst x) {
                scheduleEarly(x);
                if (x.getParentBlock().getImmDomDepth() < i.getParentBlock().getImmDomDepth()) {
                    i.setEarlyBlock(x.getParentBlock());
                }
            }
        }
    }

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
                    use = phiInst.getCfgPreList().get(phiInst.getOperandList().indexOf(i));
                }
                lca = findLCA(lca, use);    // 更新最近公共祖先
            }
        }

        // 第三步 select block
        BasicBlock best = lca;
        BasicBlock cur = lca;
        // 为指令选择最终的基本块best, 选择的依据是循环深度尽可能浅, 且在支配树中的深度尽可能浅
        while (cur.getImmDomDepth() > i.getEarlyBlock().getImmDomDepth()) {
            if (cur.getLoopDepth() < best.getLoopDepth()) {
                best = cur;
            }
            cur = cur.getImmDomBy();
        }
        // 将i从原基本块移动到best
        i.getParentBlock().getInsts().remove(i);
        int index = best.getInsts().size(); // 插入到best块最后一条指令之前
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
     * 判断指令是否不能被移动。
     */
    private boolean isPinned(Inst inst) {
        if (inst instanceof BinaryInst || inst instanceof GEPInst || inst instanceof ZextInst ||
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
