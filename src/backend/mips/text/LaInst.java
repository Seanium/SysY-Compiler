package backend.mips.text;

import backend.mips.Asm;
import backend.mips.Reg;

public class LaInst extends Asm {
    private final Opcode opcode;
    private final Reg to;
    private final String fromLabel;
    private final Reg offset;

    /***
     * la $to fromLabel($offset)
     */
    public LaInst(Reg to, String fromLabel, Reg offset) {
        this.opcode = Opcode.la;
        this.to = to;
        this.fromLabel = fromLabel;
        this.offset = offset;
    }

    @Override
    public String toString() {
        return opcode + " " + to + " " + fromLabel + "(" + offset + ")";
    }
}
