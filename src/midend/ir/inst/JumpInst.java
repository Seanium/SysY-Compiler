package midend.ir.inst;

import midend.ir.BasicBlock;
import midend.ir.type.VoidType;

public class JumpInst extends Inst {

    /***
     * 无条件跳转指令。
     * @param targetBasicBlock 要跳转到的基本块。
     */
    public JumpInst(BasicBlock targetBasicBlock) {
        super(VoidType.voidType, "", Opcode.jump);
        addOperand(targetBasicBlock);
    }

    public BasicBlock getTargetBasicBlock() {
        return (BasicBlock) operandList.get(0);
    }

    @Override
    public String toString() {
        BasicBlock targetBasicBlock = getTargetBasicBlock();
        return "br label %" + targetBasicBlock.getName();
    }
}
