package midend.pass;

import midend.ir.BasicBlock;
import midend.ir.Function;
import midend.ir.Module;
import midend.ir.inst.BranchInst;
import midend.ir.inst.Inst;
import midend.ir.inst.JumpInst;
import midend.ir.inst.ReturnInst;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class SimplifyBasicBlockPass implements Pass {
    private final Module module;

    /***
     * 移除冗余的br或ret及其后续指令（即保证每个基本块的末尾语句是首条br或ret）；
     * 移除不可到达的基本块。
     */
    public SimplifyBasicBlockPass() {
        this.module = Module.getInstance();
    }

    @Override
    public void run() {
        for (Function function : module.getNotLibFunctions()) {
            removeRedundantInst(function);
            removeUnreachableBasicBlock(function);
        }
    }

    /***
     * 移除冗余的br或ret及其后续指令（即每个基本块的末尾语句是首条br或ret）。
     */
    private void removeRedundantInst(Function function) {
        for (BasicBlock basicBlock : function.getBasicBlocks()) {
            Iterator<Inst> iterator = basicBlock.getInstructions().iterator();
            boolean allowRemove = false;
            while (iterator.hasNext()) {
                Inst inst = iterator.next();
                if (allowRemove) {
                    iterator.remove();
                } else if (inst instanceof BranchInst || inst instanceof JumpInst || inst instanceof ReturnInst) {
                    allowRemove = true;
                }
            }
        }
    }

    /***
     * 移除不可到达的基本块。
     */
    private void removeUnreachableBasicBlock(Function function) {
        HashMap<BasicBlock, Integer> refCountMap = new HashMap<>(); // 每个基本块的引用计数
        ArrayList<BasicBlock> basicBlocks = function.getBasicBlocks();
        // 初始化引用计数map
        for (int i = 0; i < basicBlocks.size(); i++) {
            if (i == 0) {
                refCountMap.put(basicBlocks.get(i), 1); // 每个函数的首个基本块是无条件进入的，其引用计数默认为1
            } else {
                refCountMap.put(basicBlocks.get(i), 0);
            }
        }
        // 填充引用计数map
        for (BasicBlock basicBlock : basicBlocks) {
            if (basicBlock.getLastInst() instanceof JumpInst jumpInst) {
                refCountMap.put(jumpInst.getTargetBasicBlock(), refCountMap.get(jumpInst.getTargetBasicBlock()) + 1);
            } else if (basicBlock.getLastInst() instanceof BranchInst branchInst) {
                refCountMap.put(branchInst.getTrueBlock(), refCountMap.get(branchInst.getTrueBlock()) + 1);
                refCountMap.put(branchInst.getFalseBlock(), refCountMap.get(branchInst.getFalseBlock()) + 1);
            }
        }
        // 删除引用计数为0的基本块
        basicBlocks.removeIf(basicBlock -> refCountMap.get(basicBlock) == 0);
    }
}
