package frontend.node;

import frontend.token.Token;

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

    // 11.主函数定义 MainFuncDef → 'int' 'main' '(' ')' Block // 存在main函数
    @Override
    public String toString() {
        return intToken.toString() +
                mainToken.toString() +
                leftParen.toString() +
                rightParen.toString() +
                blockNode.toString() +
                "<MainFuncDef>\n";
    }

    public BlockNode getBlockNode() {
        return blockNode;
    }
}
