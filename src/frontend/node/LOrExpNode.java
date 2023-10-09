package frontend.node;

import frontend.token.Token;

public class LOrExpNode extends Node {
    private final LOrExpNode lOrExpNode;
    private final Token op;
    private final LAndExpNode lAndExpNode;

    public LOrExpNode(LOrExpNode lOrExpNode, Token op, LAndExpNode lAndExpNode) {
        this.lOrExpNode = lOrExpNode;
        this.op = op;
        this.lAndExpNode = lAndExpNode;
    }

    public LOrExpNode(LAndExpNode lAndExpNode) {
        this.lOrExpNode = null;
        this.op = null;
        this.lAndExpNode = lAndExpNode;
    }

    // 32.逻辑或表达式 LOrExp → LAndExp | LOrExp '||' LAndExp // 1.LAndExp 2.|| 均需覆盖
    // 【消除左递归】 LOrExp → LAndExp {'||' LAndExp}
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (lOrExpNode == null) {
            sb.append(lAndExpNode.toString());
        } else {
            sb.append(lOrExpNode);
            sb.append(op.toString());
            sb.append(lAndExpNode.toString());
        }
        sb.append("<LOrExp>\n");
        return sb.toString();
    }
}
