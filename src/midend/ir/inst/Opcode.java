package midend.ir.inst;

public enum Opcode {
    // Terminator Instructions
    ret,
    branch, // 条件跳转 br i1 <cond>, label <iftrue>, label <iffalse>
    jump,   // 无条件跳转 br label <dest>


    // Binary Operations
    add, sub, mul, sdiv, srem,


    // Memory Access and Addressing Operations
    alloca, load, store, gep,


    // Conversion Operations
    zext,   // <result> = zext <ty> <value> to <ty2>	将 ty的value的type扩充为ty2
    //trunc,  // <result> = trunc <ty> <value> to <ty2>	将 ty的value的type缩减为ty2


    // Other Operations
    call, icmp
}
