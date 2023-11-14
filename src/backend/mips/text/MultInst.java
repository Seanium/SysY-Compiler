package backend.mips.text;

import backend.mips.Asm;
import backend.mips.Reg;

public class MultInst extends Asm {
    private final Opcode opcode;
    private final Reg op1;
    private final Reg op2;

    /***
     * mult $op1 $op2
     */
    public MultInst(Reg op1, Reg op2) {
        this.opcode = Opcode.mult;
        this.op1 = op1;
        this.op2 = op2;
    }

    @Override
    public String toString() {
        return opcode + " " + op1 + " " + op2;
    }
}
