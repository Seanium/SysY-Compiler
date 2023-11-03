package frontend.node;

import frontend.token.Token;

public class LAndExpNode extends Node {
    private final LAndExpNode lAndExpNode;
    private final Token op;
    private final EqExpNode eqExpNode;

    public LAndExpNode(LAndExpNode lAndExpNode, Token op, EqExpNode eqExpNode) {
        this.lAndExpNode = lAndExpNode;
        this.op = op;
        this.eqExpNode = eqExpNode;
    }

    public LAndExpNode(EqExpNode eqExpNode) {
        this.lAndExpNode = null;
        this.op = null;
        this.eqExpNode = eqExpNode;
    }

    public LAndExpNode getlAndExpNode() {
        return lAndExpNode;
    }

    public EqExpNode getEqExpNode() {
        return eqExpNode;
    }

    // 31.逻辑与表达式 LAndExp → EqExp | LAndExp '&&' EqExp // 1.EqExp 2.&& 均需覆盖
    // 【消除左递归】 LAndExp → EqExp {'&&' EqExp}
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (lAndExpNode == null) {
            sb.append(eqExpNode.toString());
        } else {
            sb.append(lAndExpNode);
            sb.append(op.toString());
            sb.append(eqExpNode.toString());
        }
        sb.append("<LAndExp>\n");
        return sb.toString();
    }
}
