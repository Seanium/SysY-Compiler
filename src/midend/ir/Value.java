package midend.ir;

import midend.ir.type.Type;

import java.util.ArrayList;

public class Value {
    protected final Type type;
    protected final String name;
    protected final ArrayList<Use> useList;

    public Value(Type type, String name) {
        this.type = type;
        this.name = name;
        this.useList = new ArrayList<>();
    }

    public Type getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public void addUse(Use use) {
        useList.add(use);
    }
}
