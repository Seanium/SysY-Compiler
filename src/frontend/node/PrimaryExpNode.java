package frontend.node;

import frontend.Token;

public class PrimaryExpNode extends Node {

    private final Token leftParen;
    private final ExpNode expNode;
    private final Token rightParen;
    private final LValNode lValNode;
    private final NumberNode numberNode;


    public PrimaryExpNode(Token leftParen, ExpNode expNode, Token rightParen) {
        this.leftParen = leftParen;
        this.expNode = expNode;
        this.rightParen = rightParen;
        this.lValNode = null;
        this.numberNode = null;
    }

    public PrimaryExpNode(LValNode lValNode) {
        this.lValNode = lValNode;
        this.leftParen = null;
        this.expNode = null;
        this.rightParen = null;
        this.numberNode = null;
    }

    public PrimaryExpNode(NumberNode numberNode) {
        this.numberNode = numberNode;
        this.leftParen = null;
        this.expNode = null;
        this.rightParen = null;
        this.lValNode = null;
    }

    public LValNode getlValNode() {
        return lValNode;
    }

    // 22.基本表达式 PrimaryExp → '(' Exp ')' | LVal | Number // 三种情况均需覆盖
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (leftParen != null) {
            sb.append(leftParen);
            sb.append(expNode.toString());
            sb.append(rightParen.toString());
        } else if (lValNode != null) {
            sb.append(lValNode);
        } else if (numberNode != null) {
            sb.append(numberNode);
        }
        sb.append("<PrimaryExp>\n");
        return sb.toString();
    }
}
