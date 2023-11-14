package backend.mips.text;

public enum Opcode {
    j, jr, jal,
    beqz,
    addiu, addu, subu, mult, div, sll,
    mfhi, mflo,
    sw,
    li, lw, la,
    seq, sne, sgt, sge, slt, sle,
    syscall
}
