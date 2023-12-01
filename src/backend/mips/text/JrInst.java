package backend.mips.text;

import backend.mips.Asm;
import backend.mips.Reg;

public class JrInst extends Asm {
    private final Opcode opcode;
    private final Reg reg;

    /**
     * jr $reg
     */
    public JrInst(Reg reg) {
        this.opcode = Opcode.jr;
        this.reg = reg;
    }

    @Override
    public String toString() {
        return opcode + " " + reg;
    }
}
