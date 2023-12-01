package backend.mips.text;

import backend.mips.Asm;
import backend.mips.Reg;

public class MfHiloInst extends Asm {
    private final Opcode opcode;
    private final Reg to;

    /**
     * opcode $to
     * 例如 mfhi $t0
     */
    public MfHiloInst(Opcode opcode, Reg to) {
        this.opcode = opcode;
        this.to = to;
    }

    @Override
    public String toString() {
        return opcode + " " + to;
    }
}
