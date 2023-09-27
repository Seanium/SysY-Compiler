package frontend.node;

import frontend.Token;

public class RelExpNode extends Node {

    private final RelExpNode relExpNode;
    private final Token op;
    private final AddExpNode addExpNode;

    public RelExpNode(RelExpNode relExpNode, Token op, AddExpNode addExpNode) {
        this.relExpNode = relExpNode;
        this.op = op;
        this.addExpNode = addExpNode;
    }

    public RelExpNode(AddExpNode addExpNode) {
        this.relExpNode = null;
        this.op = null;
        this.addExpNode = addExpNode;
    }

    // 29.关系表达式 RelExp → AddExp | RelExp ('<' | '>' | '<=' | '>=') AddExp // 1.AddExp 2.< 3.> 4.<= 5.>= 均需覆盖
    // 【消除左递归】 RelExp → AddExp { ('<' | '>' | '<=' | '>=') AddExp}
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (relExpNode == null) {
            sb.append(addExpNode.toString());
        } else {
            sb.append(relExpNode);
            sb.append(op.toString());
            sb.append(addExpNode.toString());
        }
        sb.append("<RelExp>\n");
        return sb.toString();
    }
}
