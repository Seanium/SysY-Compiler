package frontend.node;

import frontend.token.Token;

public class AddExpNode extends Node {
    private final AddExpNode addExpNode;
    private final Token op;
    private final MulExpNode mulExpNode;

    public AddExpNode(AddExpNode addExpNode, Token op, MulExpNode mulExpNode) {
        this.addExpNode = addExpNode;
        this.op = op;
        this.mulExpNode = mulExpNode;
    }

    public AddExpNode(MulExpNode mulExpNode) {
        this.addExpNode = null;
        this.op = null;
        this.mulExpNode = mulExpNode;
    }

    public MulExpNode getMulExpNode() {
        return mulExpNode;
    }

    // 28.加减表达式 AddExp → MulExp | AddExp ('+' | '−') MulExp // 1.MulExp 2.+ 需覆盖 3.- 需覆盖
    // 【消除左递归】 AddExp → MulExp {('+' | '−') MulExp}
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (addExpNode == null) {
            sb.append(mulExpNode.toString());
        } else {
            sb.append(addExpNode);
            sb.append(op.toString());
            sb.append(mulExpNode.toString());
        }
        sb.append("<AddExp>\n");
        return sb.toString();
    }
}
