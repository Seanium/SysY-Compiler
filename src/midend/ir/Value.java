package midend.ir;

import backend.mips.Reg;
import midend.ir.inst.Inst;
import midend.ir.type.Type;

import java.util.ArrayList;
import java.util.LinkedHashSet;

public class Value {
    protected final Type type;
    protected String name;
    private final LinkedHashSet<Inst> liveRange;
    /***
     * 该变量分配到的寄存器。
     * 为null表示尚未分配，为Reg.NOREG表示已经决定不分配寄存器，为Reg.寄存器表示分配对应寄存器。
     */
    private Reg reg;
    /***
     * 记录谁使用了该value。
     */
    protected final ArrayList<User> userList;

    public Value(Type type, String name) {
        this.type = type;
        this.name = name;
        this.userList = new ArrayList<>();
        this.liveRange = new LinkedHashSet<>();
        this.reg = null;
    }

    public Reg getReg() {
        return reg;
    }

    public void setReg(Reg reg) {
        this.reg = reg;
    }

    /***
     * 若尚未分配或决定不分配寄存器，返回真。
     */
    public boolean notInReg() {
        return this.reg == null || this.reg == Reg.NOREG;
    }

    /***
     * 若已分配到寄存器，返回真。
     */
    public boolean inReg() {
        return !notInReg();
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
     * 也就是说，将后续对该value的使用，全部改为对newOperand的使用。
     */
    public void replaceOperandOfAllUser(Value newOperand) {
        ArrayList<User> users = new ArrayList<>(userList); // 涉及到遍历删除，需要先把原user列表存起来
        for (User user : users) {
            user.replaceOperand(this, newOperand);
        }
    }

    /***
     * 删除该value的指定user。
     */
    public void removeUser(User user) {
        userList.removeIf(user::equals);
    }

    public LinkedHashSet<Inst> getLiveRange() {
        return liveRange;
    }

    public void addUser(User user) {
        userList.add(user);
    }
}
