package backend.mips.text;

import backend.mips.Asm;
import backend.mips.Reg;

public class SubuInst extends Asm {
    private final Opcode opcode;
    private final Reg res;
    private final Reg op1;
    private final Reg op2;

    /***
     * subu $res $operand1 $ operand2
     */
    public SubuInst(Reg res, Reg op1, Reg op2) {
        this.opcode = Opcode.subu;
        this.res = res;
        this.op1 = op1;
        this.op2 = op2;
    }

    @Override
    public String toString() {
        return opcode + " " + res + " " + op1 + " " + op2;
    }
}
