package frontend.node;

public class ConstExpNode extends Node {
    private final AddExpNode addExpNode;

    public ConstExpNode(AddExpNode addExpNode) {
        this.addExpNode = addExpNode;
    }

    // 33.常量表达式 ConstExp → AddExp 注：使用的Ident 必须是常量 // 存在即可
    @Override
    public String toString() {
        return addExpNode.toString() + "<ConstExp>\n";
    }

    public AddExpNode getAddExpNode() {
        return addExpNode;
    }
}
