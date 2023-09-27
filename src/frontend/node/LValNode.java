package frontend.node;

import frontend.Token;

import java.util.ArrayList;

public class LValNode extends Node {

    private final Token ident;
    private final ArrayList<Token> leftBrackets;
    private final ArrayList<ExpNode> expNodes;
    private final ArrayList<Token> rightBrackets;

    public LValNode(Token ident, ArrayList<Token> leftBrackets, ArrayList<ExpNode> expNodes, ArrayList<Token> rightBrackets) {
        this.ident = ident;
        this.leftBrackets = leftBrackets;
        this.expNodes = expNodes;
        this.rightBrackets = rightBrackets;
    }

    // 21.左值表达式 LVal → Ident {'[' Exp ']'} //1.普通变量 2.一维数组 3.二维数组
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(ident.toString());
        for (int i = 0; i < leftBrackets.size(); i++) {
            sb.append(leftBrackets.get(i).toString());
            sb.append(expNodes.get(i).toString());
            sb.append(rightBrackets.get(i).toString());
        }
        sb.append("<LVal>\n");
        return sb.toString();
    }
}
