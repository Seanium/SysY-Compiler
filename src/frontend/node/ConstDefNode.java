package frontend.node;

import frontend.token.Token;

import java.util.ArrayList;

public class ConstDefNode extends Node {
    private final Token ident;
    private final ArrayList<Token> leftBrackets;
    private final ArrayList<ConstExpNode> constExpNodes;
    private final ArrayList<Token> rightBrackets;
    private final Token assign;
    private final ConstInitValNode constInitValNode;


    public ConstDefNode(Token ident, ArrayList<Token> leftBrackets, ArrayList<ConstExpNode> constExpNodes, ArrayList<Token> rightBrackets, Token assign, ConstInitValNode constInitValNode) {
        this.ident = ident;
        this.leftBrackets = leftBrackets;
        this.constExpNodes = constExpNodes;
        this.rightBrackets = rightBrackets;
        this.assign = assign;
        this.constInitValNode = constInitValNode;
    }

    // 5.常数定义 ConstDef → Ident { '[' ConstExp ']' } '=' ConstInitVal // 包含普通变量、一维数组、二维数组共三种情况
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(ident.toString());
        for (int i = 0; i < leftBrackets.size(); i++) {
            sb.append(leftBrackets.get(i).toString());
            sb.append(constExpNodes.get(i).toString());
            sb.append(rightBrackets.get(i).toString());
        }
        sb.append(assign.toString());
        sb.append(constInitValNode.toString());
        sb.append("<ConstDef>\n");
        return sb.toString();
    }
}
