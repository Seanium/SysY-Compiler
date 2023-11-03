package midend.ir.inst;

import midend.ir.BasicBlock;
import midend.ir.Value;
import midend.ir.type.VoidType;

public class BranchInst extends Inst {
    private final Value cond;
    private final BasicBlock trueBlock;
    private final BasicBlock falseBlock;

    /***
     * 条件跳转指令。
     */
    public BranchInst(Value cond, BasicBlock trueBlock, BasicBlock falseBlock) {
        super(VoidType.voidType, "", Opcode.branch);
        this.cond = cond;
        this.trueBlock = trueBlock;
        this.falseBlock = falseBlock;
        addOperand(cond);   // todo operand是否是这些
        addOperand(trueBlock);
        addOperand(falseBlock);
    }

    @Override
    public String toString() {
        return "br i1 " + cond.getName() +
                ", label %" + trueBlock.getName() + ", label %" + falseBlock.getName();
    }
}
