package frontend.node;

public class CondNode extends Node {
    private final LOrExpNode lOrExpNode;

    public CondNode(LOrExpNode lOrExpNode) {
        this.lOrExpNode = lOrExpNode;
    }

    // 20.条件表达式 Cond → LOrExp // 存在即可
    @Override
    public String toString() {
        return lOrExpNode.toString() + "<Cond>\n";
    }
}
