package midend.ir;

import midend.ir.type.Type;

import java.util.ArrayList;

public class User extends Value {
    /***
     * 记录该value使用了谁。
     */
    protected final ArrayList<Value> operandList;

    public User(Type type, String name) {
        super(type, name);
        this.operandList = new ArrayList<>();
    }

    public void addOperand(Value value) {
        operandList.add(value);
        if (value != null) {
            value.addUser(this);
        }
    }

    public ArrayList<Value> getOperandList() {
        return operandList;
    }

    /***
     * 将该value的operand中的oldValue，替换为newValue。
     */
    public void replaceOperand(Value oldOperand, Value newOperand) {
        if (!operandList.contains(oldOperand)) {
            return;
        }
        for (int i = 0; i < operandList.size(); i++) {
            Value operand = operandList.get(i);
            if (operand.equals(oldOperand)) {
                operandList.set(i, newOperand);
                oldOperand.removeUser(this);
                newOperand.addUser(this);
            }
        }
    }

    /***
     * 删除该user作为其operand的user的信息。
     * 即在user的所有operand的user列表中，去除该user。
     */
    public void delOperandThisUser() {
        for (Value operand : operandList) {
            operand.userList.removeIf(this::equals);
        }
    }
}
