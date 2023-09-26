package frontend.node;

import frontend.Token;

public class UnaryExpNode extends Node {
    private Token ident = null;
    private Token leftParen = null;
    private FuncRParamsNode funcRParams = null;
    private Token rightParen = null;
    private UnaryOpNode unaryOpNode = null;
    private UnaryExpNode unaryExpNode = null;
    private PrimaryExpNode primaryExpNode = null;

    public UnaryExpNode(Token ident, Token leftParen, FuncRParamsNode funcRParams, Token rightParen) {
        this.ident = ident;
        this.leftParen = leftParen;
        this.funcRParams = funcRParams;
        this.rightParen = rightParen;
    }

    public UnaryExpNode(UnaryOpNode unaryOpNode, UnaryExpNode unaryExpNode) {
        this.unaryOpNode = unaryOpNode;
        this.unaryExpNode = unaryExpNode;
    }

    public UnaryExpNode(PrimaryExpNode primaryExpNode) {
        this.primaryExpNode = primaryExpNode;
    }

    public PrimaryExpNode getPrimaryExpNode() {
        return primaryExpNode;
    }
}
