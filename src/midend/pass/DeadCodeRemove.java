package midend.pass;

import midend.ir.BasicBlock;
import midend.ir.Function;
import midend.ir.Module;
import midend.ir.Value;
import midend.ir.inst.*;

import java.util.HashSet;
import java.util.Iterator;

public class DeadCodeRemove implements IRPass {

    private final Module module;
    private final HashSet<Inst> usefulInstClosure;

    /***
     * 死代码删除。
     */
    public DeadCodeRemove() {
        this.module = Module.getInstance();
        this.usefulInstClosure = new HashSet<>();
    }

    @Override
    public void run() {
        for (Function function : module.getNotLibFunctions()) {
            // 删除无用指令
            deadInstRemove(function);
        }
    }

    private boolean isUseful(Inst inst) {
        return inst instanceof JumpInst ||
                inst instanceof BranchInst ||
                inst instanceof ReturnInst ||
                inst instanceof CallInst ||
                inst instanceof StoreInst;
    }

    private void deadInstRemove(Function function) {
        usefulInstClosure.clear();
        // 找到该函数的有用指令闭包
        for (BasicBlock basicBlock : function.getBasicBlocks()) {
            for (Inst inst : basicBlock.getInsts()) {
                if (isUseful(inst)) {
                    findUsefulClosure(inst);
                }
            }
        }
        // 删除不在有用指令闭包中的指令
        for (BasicBlock basicBlock : function.getBasicBlocks()) {
            Iterator<Inst> iterator = basicBlock.getInsts().iterator();
            while (iterator.hasNext()) {
                Inst inst = iterator.next();
                if (!usefulInstClosure.contains(inst)) {
                    iterator.remove();
                    inst.delThisUserFromAllOperand();  // 删除该指令作为user的信息
                }
            }
        }
    }

    private void findUsefulClosure(Inst inst) {
        if (!usefulInstClosure.contains(inst)) {
            usefulInstClosure.add(inst);
            for (Value operand : inst.getOperandList()) {
                if (operand instanceof Inst inst1) {
                    findUsefulClosure(inst1);
                }
            }
        }
    }
}
