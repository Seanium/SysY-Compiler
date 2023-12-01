package backend.mips.text;

import backend.mips.Asm;
import backend.mips.Reg;

public class SwInst extends Asm {
    private final int type;
    private final Opcode opcode;
    private final Reg from;
    private int toOffset;
    private Reg to;
    private String toLabel;

    /**
     * sw $from toOffset($to)
     */
    public SwInst(Reg from, int toOffset, Reg to) {
        this.type = 0;
        this.opcode = Opcode.sw;
        this.from = from;
        this.toOffset = toOffset;
        this.to = to;
    }

    /**
     * sw $from toLabel
     */
    public SwInst(Reg from, String toLabel) {
        this.type = 1;
        this.opcode = Opcode.sw;
        this.from = from;
        this.toLabel = toLabel;
    }

    @Override
    public String toString() {
        if (type == 0) {
            return opcode + " " + from + " " + toOffset + "(" + to + ")";
        } else {
            return opcode + " " + from + " " + toLabel;
        }
    }
}
