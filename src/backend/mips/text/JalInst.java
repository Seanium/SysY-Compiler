package backend.mips.text;

import backend.mips.Asm;

public class JalInst extends Asm {
    private final Opcode opcode;
    private final String label;

    /**
     * jal label
     */
    public JalInst(String label) {
        this.opcode = Opcode.jal;
        this.label = label;
    }

    @Override
    public String toString() {
        return opcode + " " + label;
    }
}
