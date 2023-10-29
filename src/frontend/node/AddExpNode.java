package frontend.node;

import frontend.token.Token;
import frontend.token.TokenType;

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

    public int calDim() {
        int mulExpDim = mulExpNode.calDim();
        if (addExpNode == null) {   // AddExp → MulExp
            return mulExpDim;
        } else {    // AddExp → AddExp ('+' | '−') MulExp
            int addExpDim = addExpNode.calDim();
            if (mulExpDim != addExpDim) {   // 两部分维度不一致, 则返回-1
                return -1;
            } else {
                return mulExpDim;
            }
        }
    }

    public AddExpNode getAddExpNode() {
        return addExpNode;
    }

    public Token getOp() {
        return op;
    }

    /***
     * 计算表达式的值。
     * @return int 型的计算结果。
     */
    public int calVal() {
        if (addExpNode == null) {   // AddExp → MulExp
            return mulExpNode.calVal();
        } else {    // AddExp → AddExp ('+' | '−') MulExp
            if (op.getType() == TokenType.PLUS) {
                return addExpNode.calVal() + mulExpNode.calVal();
            } else {
                return addExpNode.calVal() - mulExpNode.calVal();
            }
        }
    }
}
