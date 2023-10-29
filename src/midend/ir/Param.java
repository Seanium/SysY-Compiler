package midend.ir;

import midend.ir.type.Type;

public class Param extends Value {
    public Param(Type type, String name) {
        super(type, name);
    }

    @Override
    public String toString() {
        return ""; //TODO
    }
}
