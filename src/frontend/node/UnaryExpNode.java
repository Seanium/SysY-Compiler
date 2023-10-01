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

    // 24.一元表达式 UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')' // 3种情况均需覆盖,函数调用也需要覆盖FuncRParams的不同情况
    // | UnaryOp UnaryExp // 存在即可
    //
    // FIRST(PrimaryExp) = {‘(’,Ident,Number}
    // FIRST(Ident '(' [FuncRParams] ')') = {Ident}     //预读'('进行判断
    // FIRST(UnaryOp UnaryExp) = {'+','−','!'}
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (primaryExpNode != null) {
            sb.append(primaryExpNode);
        } else if (unaryOpNode != null) {
            sb.append(unaryOpNode);
            sb.append(unaryExpNode.toString());
        } else {
            sb.append(ident.toString());
            sb.append(leftParen.toString());
            if (funcRParams != null) {
                sb.append(funcRParams);
            }
            sb.append(rightParen.toString());
        }
        sb.append("<UnaryExp>\n");
        return sb.toString();
    }
}