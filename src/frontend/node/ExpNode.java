package frontend.node;

public class ExpNode extends Node {
    private final AddExpNode addExpNode;

    public ExpNode(AddExpNode addExpNode) {
        this.addExpNode = addExpNode;
    }

    public AddExpNode getAddExpNode() {
        return addExpNode;
    }

    // 19.表达式 Exp → AddExp 注：SysY 表达式是int 型表达式 // 存在即可
    @Override
    public String toString() {
        return addExpNode.toString() + "<Exp>\n";
    }

    // void函数调用 或 标识符未定义 或 addExp两部分维度不一致 返回-1,
    // int返回0, int[]返回1, int[][]返回2
    public int calDim() {
        return addExpNode.calDim();
    }
}
