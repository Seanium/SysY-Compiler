package midend.ir;

import midend.ir.type.Type;

import java.util.ArrayList;

public class Value {
    protected final Type type;
    protected String name;
    /***
     * 记录谁使用了该value。
     */
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

    public ArrayList<Use> getUseList() {
        return useList;
    }

    /***
     * 将该value的全部user的operand，从该value替换为newOperand。
     */
    public void replaceUserOperandWith(Value newOperand) {
        ArrayList<Use> uses = new ArrayList<>(useList); // 涉及到遍历删除，需要先把原use列表存起来
        for (Use use : uses) {
            User user = use.getUser();
            user.replaceOperand(this, newOperand);
        }
    }

    public void removeUser(User user) {
        useList.removeIf(use -> use.getUser().equals(user));
    }

    public void addUse(Use use) {
        useList.add(use);
    }
}
