package midend.ir.inst;

public enum Opcode {
    // Terminator Instructions
    ret,
    // Binary Operations
    add, sub, mul, sdiv, srem,
    // Memory Access and Addressing Operations
    alloca, load, store,
    // Other Operations
    call
}
