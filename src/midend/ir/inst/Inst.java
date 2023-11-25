package midend.ir.inst;

import midend.ir.BasicBlock;
import midend.ir.User;
import midend.ir.Value;
import midend.ir.type.Type;

import java.util.HashSet;

public class Inst extends User {
    protected Opcode opcode;
    private BasicBlock parentBasicBlock;
    private int id;
    private final HashSet<Value> liveDef;
    private final HashSet<Value> liveUse;
    private final HashSet<Value> liveIn;
    private final HashSet<Value> liveOut;

    public Inst(Type type, String name, Opcode opcode) {
        super(type, name);
        this.opcode = opcode;
        this.parentBasicBlock = null;
        this.id = 0;
        this.liveDef = new HashSet<>();
        this.liveUse = new HashSet<>();
        this.liveIn = new HashSet<>();
        this.liveOut = new HashSet<>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public HashSet<Value> getLiveDef() {
        return liveDef;
    }

    public HashSet<Value> getLiveUse() {
        return liveUse;
    }

    public HashSet<Value> getLiveIn() {
        return liveIn;
    }

    public HashSet<Value> getLiveOut() {
        return liveOut;
    }
}
