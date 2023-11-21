package midend.ir;

import midend.ir.type.Type;

import java.util.ArrayList;

public class Value {
    protected final Type type;
    protected String name;
    /***
     * 记录谁使用了该value。
     */
    protected final ArrayList<User> userList;

    public Value(Type type, String name) {
        this.type = type;
        this.name = name;
        this.userList = new ArrayList<>();
    }

    public Type getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public ArrayList<User> getUserList() {
        return userList;
    }

    /***
     * 将该value的全部user的operand，从该value替换为newOperand。
     */
    public void replaceUserOperandWith(Value newOperand) {
        ArrayList<User> users = new ArrayList<>(userList); // 涉及到遍历删除，需要先把原user列表存起来
        for (User user : users) {
            user.replaceOperand(this, newOperand);
        }
    }

    public void removeUser(User user) {
        userList.removeIf(user::equals);
    }

    public void addUser(User user) {
        userList.add(user);
    }
}
