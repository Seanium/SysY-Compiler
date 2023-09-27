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
        return addExpNode.toString() +
                "<Exp>\n";
    }
}
