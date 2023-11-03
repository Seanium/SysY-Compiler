package midend.ir.inst;

import midend.ir.BasicBlock;
import midend.ir.type.VoidType;

public class JumpInst extends Inst {
    private final BasicBlock targetBasicBlock;

    /***
     * 无条件跳转指令。
     * @param targetBasicBlock 要跳转到的基本块。
     */
    public JumpInst(BasicBlock targetBasicBlock) {
        super(VoidType.voidType, "", Opcode.jump);
        this.targetBasicBlock = targetBasicBlock;
        addOperand(targetBasicBlock);   // todo 是否需要此operand
    }

    @Override
    public String toString() {
        return "br label %" + targetBasicBlock.getName();
    }
}
