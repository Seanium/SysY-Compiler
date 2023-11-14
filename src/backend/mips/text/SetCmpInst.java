package backend.mips.text;

import backend.mips.Asm;
import backend.mips.Reg;

public class SetCmpInst extends Asm {
    private final Opcode opcode;
    private final Reg res;
    private final Reg op1;
    private final Reg op2;

    /***
     * opcode $target $op1 $op2
     * 例如 seq $t0 $t0 $t1
     */
    public SetCmpInst(Opcode opcode, Reg res, Reg op1, Reg op2) {
        this.opcode = opcode;
        this.res = res;
        this.op1 = op1;
        this.op2 = op2;
    }

    @Override
    public String toString() {
        return opcode + " " + res + " " + op1 + " " + op2;
    }
}
