package midend.ir;

import midend.ir.type.Type;

import java.util.ArrayList;

public class User extends Value {
    protected final ArrayList<Value> operandList;

    public User(Type type, String name) {
        super(type, name);
        this.operandList = new ArrayList<>();
    }

    public void addOperand(Value value) {
        operandList.add(value);
        if (value != null) {
            value.addUse(new Use(this, value));
        }
    }
}
