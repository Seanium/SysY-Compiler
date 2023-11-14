package backend.mips.text;

import backend.mips.Asm;
import backend.mips.Reg;

public class AddiuInst extends Asm {
    private final Opcode opcode;
    private final Reg res;
    private final Reg op1;
    private final int op2;

    /***
     * addiu $res $op1 op2
     * @param res   结果寄存器。
     * @param op1  加数1寄存器。
     * @param op2  加数2立即数。
     */
    public AddiuInst(Reg res, Reg op1, int op2) {
        this.opcode = Opcode.addiu;
        this.res = res;
        this.op1 = op1;
        this.op2 = op2;
    }

    @Override
    public String toString() {
        return opcode + " " + res + " " + op1 + " " + op2;
    }
}
