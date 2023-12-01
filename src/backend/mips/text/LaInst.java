package backend.mips.text;

import backend.mips.Asm;
import backend.mips.Reg;

public class LaInst extends Asm {
    private final int type;
    private final Opcode opcode;
    private final Reg to;
    private final String fromLabel;
    private final Reg offset;
    private final int immOffset;

    /**
     * la $to fromLabel($offset)
     */
    public LaInst(Reg to, String fromLabel, Reg offset) {
        this.type = 0;
        this.opcode = Opcode.la;
        this.to = to;
        this.fromLabel = fromLabel;
        this.offset = offset;
        this.immOffset = 0;
    }

    /**
     * la $to fromLabel+immOffset
     */
    public LaInst(Reg to, String fromLabel, int immOffset) {
        this.type = 1;
        this.opcode = Opcode.la;
        this.to = to;
        this.fromLabel = fromLabel;
        this.immOffset = immOffset;
        this.offset = null;
    }

    @Override
    public String toString() {
        if (type == 0) {
            return opcode + " " + to + " " + fromLabel + "(" + offset + ")";
        } else {
            return opcode + " " + to + " " + fromLabel + "+" + immOffset;
        }
    }
}
