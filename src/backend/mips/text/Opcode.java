package backend.mips.text;

public enum Opcode {
    j, jr, jal,
    beqz,
    addiu, addu, subu, mult, madd, div, sll, sra, srl,
    mfhi, mflo, mthi,
    sw,
    li, lw, la,
    seq, sne, sgt, sge, slt, sle,
    syscall,
    move
}
