package midend.ir.inst;

import midend.ir.BasicBlock;
import midend.ir.Value;
import midend.ir.type.VoidType;

public class BranchInst extends Inst {

    /***
     * 条件跳转指令。
     */
    public BranchInst(Value cond, BasicBlock trueBlock, BasicBlock falseBlock) {
        super(VoidType.voidType, "", Opcode.branch);
        addOperand(cond);
        addOperand(trueBlock);
        addOperand(falseBlock);
    }

    public Value getCond() {
        return operandList.get(0);
    }

    public BasicBlock getTrueBlock() {
        return (BasicBlock) operandList.get(1);
    }

    public BasicBlock getFalseBlock() {
        return (BasicBlock) operandList.get(2);
    }

    @Override
    public String toString() {
        Value cond = getCond();
        BasicBlock trueBlock = getTrueBlock();
        BasicBlock falseBlock = getFalseBlock();
        return "br i1 " + cond.getName() +
                ", label %" + trueBlock.getName() + ", label %" + falseBlock.getName();
    }
}
