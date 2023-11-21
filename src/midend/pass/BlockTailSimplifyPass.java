package midend.pass;

import midend.ir.BasicBlock;
import midend.ir.Function;
import midend.ir.Module;
import midend.ir.inst.BranchInst;
import midend.ir.inst.Inst;
import midend.ir.inst.JumpInst;
import midend.ir.inst.ReturnInst;

import java.util.ArrayList;
import java.util.Iterator;

public class BlockTailSimplifyPass implements Pass {
    private final Module module;

    /***
     * 简化每个基本块的尾部指令，并移除不可到达的基本块。
     * 简化尾部指令，指的是移除冗余的br或ret及其后续指令（即保证每个基本块的末尾语句是首条br或ret，为后面计算CFG做好准备）。
     */
    public BlockTailSimplifyPass() {
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
                    inst.delOperandThisUser();  // 指令被删除后，还需删除该指令作为其他指令user的信息
                } else if (inst instanceof BranchInst || inst instanceof JumpInst || inst instanceof ReturnInst) {
                    allowRemove = true; // 首条br/ret之后的指令均需要删除
                }
            }
        }
    }

    /***
     * 移除不可到达的基本块。
     */
    private void removeUnreachableBasicBlock(Function function) {
        ArrayList<BasicBlock> basicBlocks = new ArrayList<>(function.getBasicBlocks()); // 保存遍历列表
        basicBlocks.remove(0);  // 入口基本块不删除，先排除
        for (BasicBlock basicBlock : basicBlocks) {
            if (basicBlock.getUserList().isEmpty()) {   // user列表为空，说明没有跳转指令跳转到该基本块，需要删除
                function.getBasicBlocks().remove(basicBlock);
            }
        }
    }
}
