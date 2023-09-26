package frontend.node;

import frontend.Token;

public class MainFuncDefNode extends Node {
    private final Token intToken;
    private final Token mainToken;
    private final Token leftParen;
    private final Token rightParen;
    private final BlockNode blockNode;

    public MainFuncDefNode(Token intToken, Token mainToken, Token leftParen, Token rightParen, BlockNode blockNode) {
        this.intToken = intToken;
        this.mainToken = mainToken;
        this.leftParen = leftParen;
        this.rightParen = rightParen;
        this.blockNode = blockNode;
    }
}
