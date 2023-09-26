package frontend.node;

import frontend.Token;

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
}
