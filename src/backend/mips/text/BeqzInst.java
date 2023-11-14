package backend.mips.text;

import backend.mips.Asm;
import backend.mips.Reg;

public class BeqzInst extends Asm {
    private final Opcode opcode;
    private final Reg cond;
    private final String label;

    /***
     * beqz $cond label
     */
    public BeqzInst(Reg cond, String label) {
        this.opcode = Opcode.beqz;
        this.cond = cond;
        this.label = label;
    }

    @Override
    public String toString() {
        return opcode + " " + cond + " " + label;
    }
}
