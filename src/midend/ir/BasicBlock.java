package midend.ir;

import midend.ir.inst.Inst;
import midend.ir.type.OtherType;

import java.util.ArrayList;

public class BasicBlock extends Value {
    private final Function parentFunction;
    private final ArrayList<Inst> instructions;

    public BasicBlock(String name, Function parentFunction) {
        super(OtherType.basicBlock, name);  // 基本块的 name 就是其 label
        this.parentFunction = parentFunction;
        this.instructions = new ArrayList<>();
    }

    public void addInst(Inst inst) {
        instructions.add(inst);
    }

    public ArrayList<Inst> getInstructions() {
        return instructions;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append(":\n");
        for (Inst inst : instructions) {
            sb.append("    ").append(inst.toString()).append("\n");
        }
        return sb.toString();
    }
}