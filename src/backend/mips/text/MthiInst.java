package backend.mips.text;

import backend.mips.Asm;
import backend.mips.Reg;

public class MthiInst extends Asm {
    private final Opcode opcode;
    private final Reg from;

    /**
     * mthi $from
     */
    public MthiInst(Reg from) {
        this.opcode = Opcode.mthi;
        this.from = from;
    }

    @Override
    public String toString() {
        return opcode + " " + from;
    }
}
