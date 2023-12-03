package midend.ir.inst;

import backend.mips.Reg;
import midend.ir.BasicBlock;
import midend.ir.User;
import midend.ir.Value;
import midend.ir.type.Type;

import java.util.ArrayList;
import java.util.LinkedHashSet;

public class Inst extends User {
    protected Opcode opcode;
    private BasicBlock parentBlock;
    private int id;
    private final LinkedHashSet<Value> liveDef;
    private final LinkedHashSet<Value> liveUse;
    private final LinkedHashSet<Value> liveIn;
    private final LinkedHashSet<Value> liveOut;
    private final ArrayList<Reg> activeRegs;
    /**
     * 该指令的schedule early块。
     */
    private BasicBlock earlyBlock;

    public Inst(Type type, String name, Opcode opcode) {
        super(type, name);
        this.opcode = opcode;
        this.parentBlock = null;
        this.id = 0;
        this.liveDef = new LinkedHashSet<>();
        this.liveUse = new LinkedHashSet<>();
        this.liveIn = new LinkedHashSet<>();
        this.liveOut = new LinkedHashSet<>();
        this.activeRegs = new ArrayList<>();
        this.earlyBlock = null;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public BasicBlock getParentBlock() {
        return parentBlock;
    }

    public void setParentBlock(BasicBlock parentBlock) {
        this.parentBlock = parentBlock;
    }

    public Opcode getOpcode() {
        return opcode;
    }

    public LinkedHashSet<Value> getLiveDef() {
        return liveDef;
    }

    public LinkedHashSet<Value> getLiveUse() {
        return liveUse;
    }

    public LinkedHashSet<Value> getLiveIn() {
        return liveIn;
    }

    public LinkedHashSet<Value> getLiveOut() {
        return liveOut;
    }

    public ArrayList<Reg> getActiveRegs() {
        return activeRegs;
    }

    public BasicBlock getEarlyBlock() {
        return earlyBlock;
    }

    public void setEarlyBlock(BasicBlock earlyBlock) {
        this.earlyBlock = earlyBlock;
    }
}
