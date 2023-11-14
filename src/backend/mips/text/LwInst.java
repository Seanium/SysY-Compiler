package backend.mips.text;

import backend.mips.Asm;
import backend.mips.Reg;

public class LwInst extends Asm {
    private final int type;
    private final Opcode opcode;
    private final Reg to;
    private int fromOffset;
    private Reg from;
    private String fromLabel;

    /***
     * lw $to fromOffset($from)
     */
    public LwInst(Reg to, int fromOffset, Reg from) {
        this.type = 0;
        this.opcode = Opcode.lw;
        this.to = to;
        this.fromOffset = fromOffset;
        this.from = from;
    }

    /***
     * lw $to fromLabel
     */
    public LwInst(Reg to, String fromLabel) {
        this.type = 1;
        this.opcode = Opcode.lw;
        this.to = to;
        this.fromLabel = fromLabel;
    }

    @Override
    public String toString() {
        if (type == 0) {
            return opcode + " " + to + " " + fromOffset + "(" + from + ")";
        } else {
            return opcode + " " + to + " " + fromLabel;
        }
    }
}
