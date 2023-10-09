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
        StringBuilder sb = new StringBuilder();
        sb.append(intToken.toString());
        sb.append(mainToken.toString());
        sb.append(leftParen.toString());
        sb.append(rightParen.toString());
        sb.append(blockNode.toString());
        sb.append("<MainFuncDef>\n");
        return sb.toString();
    }
}
