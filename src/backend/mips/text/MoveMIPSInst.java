package backend.mips.text;

import backend.mips.Asm;
import backend.mips.Reg;

public class MoveMIPSInst extends Asm {
    private final Opcode opcode;
    private final Reg to;
    private final Reg from;

    /***
     * move $to $from
     */
    public MoveMIPSInst(Reg to, Reg from) {
        this.opcode = Opcode.move;
        this.to = to;
        this.from = from;
    }

    @Override
    public String toString() {
        return opcode + " " + to + " " + from;
    }
}
