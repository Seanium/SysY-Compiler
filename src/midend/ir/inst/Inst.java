package midend.ir.inst;

import midend.ir.User;
import midend.ir.type.Type;

public class Inst extends User {
    protected Opcode opcode;

    public Inst(Type type, String name, Opcode opcode) {
        super(type, name);
        this.opcode = opcode;
    }
}
