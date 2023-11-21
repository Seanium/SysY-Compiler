package midend.ir.inst;

import midend.ir.BasicBlock;
import midend.ir.User;
import midend.ir.type.Type;

public class Inst extends User {
    protected Opcode opcode;
    private BasicBlock parentBasicBlock;

    public Inst(Type type, String name, Opcode opcode) {
        super(type, name);
        this.opcode = opcode;
        this.parentBasicBlock = null;
    }

    public BasicBlock getParentBasicBlock() {
        return parentBasicBlock;
    }

    public void setParentBasicBlock(BasicBlock parentBasicBlock) {
        this.parentBasicBlock = parentBasicBlock;
    }

    public Opcode getOpcode() {
        return opcode;
    }
}
