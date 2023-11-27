package backend.mips;

public enum Reg {
    zero,
    at,
    v0, v1,
    a0, a1, a2, a3,
    t0, t1, t2, t3, t4, t5, t6, t7,
    s0, s1, s2, s3, s4, s5, s6, s7,
    t8, t9,
    k0, k1,
    gp, sp, fp,
    ra,
    NOREG;  // 若变量不分配寄存器，则value.reg设置为此值

    @Override
    public String toString() {
        return "$" + name();
    }
}
