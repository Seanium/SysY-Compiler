package backend.mips.text;

import backend.mips.Asm;
import backend.mips.Reg;

public class SraInst extends Asm {
    private final Opcode opcode;
    private final Reg res;
    private final Reg op1;
    private final int op2;

    /**
     * sra $res $op1 op2
     */
    public SraInst(Reg res, Reg op1, int op2) {
        this.opcode = Opcode.sra;
        this.res = res;
        this.op1 = op1;
        this.op2 = op2;
    }

    @Override
    public String toString() {
        return opcode + " " + res + " " + op1 + " " + op2;
    }
}
