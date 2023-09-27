package frontend.node;

import frontend.Token;

public class EqExpNode extends Node {
    private final EqExpNode eqExpNode;
    private final Token op;
    private final RelExpNode relExpNode;

    public EqExpNode(EqExpNode eqExpNode, Token op, RelExpNode relExpNode) {
        this.eqExpNode = eqExpNode;
        this.op = op;
        this.relExpNode = relExpNode;
    }

    public EqExpNode(RelExpNode relExpNode) {
        this.eqExpNode = null;
        this.op = null;
        this.relExpNode = relExpNode;
    }

    // 30.相等性表达式 EqExp → RelExp | EqExp ('==' | '!=') RelExp // 1.RelExp 2.== 3.!= 均需覆盖
    // 【消除左递归】 EqExp → RelExp { ('==' | '!=') RelExp}
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (eqExpNode == null) {
            sb.append(relExpNode.toString());
        } else {
            sb.append(eqExpNode);
            sb.append(op.toString());
            sb.append(relExpNode.toString());
        }
        sb.append("<EqExp>\n");
        return sb.toString();
    }
}
