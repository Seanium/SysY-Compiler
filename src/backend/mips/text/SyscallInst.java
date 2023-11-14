package backend.mips.text;

import backend.mips.Asm;

public class SyscallInst extends Asm {
    private final Opcode opcode;

    /***
     * syscall
     */
    public SyscallInst() {
        this.opcode = Opcode.syscall;
    }

    @Override
    public String toString() {
        return opcode.toString();
    }
}
