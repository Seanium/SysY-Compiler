package backend.mips.text;

import backend.mips.Asm;
import backend.mips.Reg;

public class LiInst extends Asm {
    private final Opcode opcode;
    private final Reg to;
    private final int imm;

    /**
     * li $to imm
     */
    public LiInst(Reg to, int imm) {
        this.opcode = Opcode.li;
        this.to = to;
        this.imm = imm;
    }

    @Override
    public String toString() {
        return opcode + " " + to + " " + imm;
    }
}
