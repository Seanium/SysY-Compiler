package backend.mips.text;

import backend.mips.Asm;

public class JInst extends Asm {
    private final Opcode opcode;
    private final String label;

    /**
     * j label
     */
    public JInst(String label) {
        this.opcode = Opcode.j;
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return opcode + " " + label;
    }
}
