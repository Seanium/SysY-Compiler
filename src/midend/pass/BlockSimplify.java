package midend.pass;

import midend.ir.BasicBlock;
import midend.ir.Function;
import midend.ir.Module;
import midend.ir.inst.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

public class BlockSimplify implements IRPass {
    private final Module module;
    /**
     * 可达基本块的闭包。
     */
    private final HashSet<BasicBlock> reachableBlockClosure;
    /**
     * 可达函数的闭包。
     */
    private final HashSet<Function> reachableFuncClosure;

    /**
     * 简化每个基本块的尾部指令，并移除不可到达的基本块。
     * 简化尾部指令，指的是移除冗余的br或ret及其后续指令（即保证每个基本块的末尾语句是首条br或ret，为后面计算CFG做好准备）。
     */
    public BlockSimplify() {
        this.module = Module.getInstance();
        this.reachableBlockClosure = new HashSet<>();
        this.reachableFuncClosure = new HashSet<>();
    }

    @Override
    public void run() {
        for (Function function : module.getNotLibFunctions()) {
            removeRedundantInst(function);
            removeUnreachableBlock(function);
        }
        removeUnreachableFunc();
    }

    /**
     * 移除冗余的br或ret及其后续指令（即每个基本块的末尾语句是首条br或ret）。
     */
    private void removeRedundantInst(Function function) {
        for (BasicBlock basicBlock : function.getBasicBlocks()) {
            Iterator<Inst> iterator = basicBlock.getInsts().iterator();
            boolean allowRemove = false;
            while (iterator.hasNext()) {
                Inst inst = iterator.next();
                if (allowRemove) {
                    iterator.remove();
                    inst.delThisUserFromAllOperands();  // 指令被删除后，还需删除该指令作为其他指令user的信息
                } else if (inst instanceof BranchInst || inst instanceof JumpInst || inst instanceof ReturnInst) {
                    allowRemove = true; // 首条br/ret之后的指令均需要删除
                }
            }
        }
    }

    /**
     * 移除不可到达的基本块。
     */
    private void removeUnreachableBlock(Function function) {
        reachableBlockClosure.clear();
        findReachableBlockClosure(function.getBasicBlocks().get(0));    // 首个基本块一定可达
        function.getBasicBlocks().removeIf(basicBlock -> !reachableBlockClosure.contains(basicBlock));
    }

    /**
     * 递归搜索可达基本块的闭包。
     */
    private void findReachableBlockClosure(BasicBlock entry) {
        if (!reachableBlockClosure.contains(entry)) {
            reachableBlockClosure.add(entry);
            ArrayList<Inst> insts = entry.getInsts();
            Inst lastInst = insts.get(insts.size() - 1);
            if (lastInst instanceof JumpInst jumpInst) {
                findReachableBlockClosure(jumpInst.getTargetBasicBlock());
            } else if (lastInst instanceof BranchInst branchInst) {
                findReachableBlockClosure(branchInst.getTrueBlock());
                findReachableBlockClosure(branchInst.getFalseBlock());
            }
        }
    }

    /**
     * 移除不可达函数。
     */
    private void removeUnreachableFunc() {
        reachableFuncClosure.clear();
        Function mainFunc = null;
        for (Function function : module.getNotLibFunctions()) {
            if (function.getName().equals("@main")) {
                mainFunc = function;
            }
        }
        assert mainFunc != null : "错误，不存在main函数!";
        findReachableFuncClosure(mainFunc); // main函数一定可达
        module.getFunctions().removeIf(function -> !reachableFuncClosure.contains(function));
    }

    /**
     * 递归搜索可达函数的闭包。
     */
    private void findReachableFuncClosure(Function entry) {
        if (!reachableFuncClosure.contains(entry)) {
            reachableFuncClosure.add(entry);
            for (BasicBlock basicBlock : entry.getBasicBlocks()) {
                for (Inst inst : basicBlock.getInsts()) {
                    if (inst instanceof CallInst callInst) {
                        findReachableFuncClosure(callInst.getTargetFunc());
                    }
                }
            }
        }
    }
}
