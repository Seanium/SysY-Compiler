package backend.mips.text;

import backend.mips.Asm;
import backend.mips.Reg;

public class AdduInst extends Asm {
    private final Opcode opcode;
    private final Reg res;
    private final Reg op1;
    private final Reg op2;

    /**
     * addu $res $operand1 $ operand2
     */
    public AdduInst(Reg res, Reg op1, Reg op2) {
        this.opcode = Opcode.addu;
        this.res = res;
        this.op1 = op1;
        this.op2 = op2;
    }

    @Override
    public String toString() {
        return opcode + " " + res + " " + op1 + " " + op2;
    }
}
