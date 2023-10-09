package frontend.node;

import frontend.token.Token;

public class FuncDefNode extends Node {
    private final FuncTypeNode funcTypeNode;
    private final Token ident;
    private final Token leftParen;
    private final FuncFParamsNode funcFParamsNode;
    private final Token rightParen;
    private final BlockNode blockNode;

    public FuncDefNode(FuncTypeNode funcTypeNode, Token ident, Token leftParen, FuncFParamsNode funcFParamsNode, Token rightParen, BlockNode blockNode) {
        this.funcTypeNode = funcTypeNode;
        this.ident = ident;
        this.leftParen = leftParen;
        this.funcFParamsNode = funcFParamsNode;
        this.rightParen = rightParen;
        this.blockNode = blockNode;
    }

    // 10.函数定义 FuncDef → FuncType Ident '(' [FuncFParams] ')' Block // 1.无形参 2.有形参
    // FIRST(FuncFParams ) = FIRST(FuncFParam) = {‘int’}
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(funcTypeNode.toString());
        sb.append(ident.toString());
        sb.append(leftParen.toString());
        if (funcFParamsNode != null) {
            sb.append(funcFParamsNode);
        }
        sb.append(rightParen.toString());
        sb.append(blockNode.toString());
        sb.append("<FuncDef>\n");
        return sb.toString();
    }
}
