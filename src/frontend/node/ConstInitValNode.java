package frontend.node;

import frontend.token.Token;

import java.util.ArrayList;

public class ConstInitValNode extends Node {
    private final Token leftBrace;
    private final ArrayList<ConstInitValNode> constInitValNodes;
    private final ArrayList<Token> commas;
    private final Token rightBrace;
    private final ConstExpNode constExpNode;

    public ConstInitValNode(Token leftBrace, ArrayList<ConstInitValNode> constInitValNodes, ArrayList<Token> commas, Token rightBrace) {
        this.leftBrace = leftBrace;
        this.constInitValNodes = constInitValNodes;
        this.commas = commas;
        this.rightBrace = rightBrace;
        this.constExpNode = null;
    }

    public ConstInitValNode(ConstExpNode constExpNode) {
        this.constExpNode = constExpNode;
        this.leftBrace = null;
        this.constInitValNodes = null;
        this.commas = null;
        this.rightBrace = null;
    }

    public ArrayList<ConstInitValNode> getConstInitValNodes() {
        return constInitValNodes;
    }

    // 6.常量初值 ConstInitVal → ConstExp
    // | '{' [ ConstInitVal { ',' ConstInitVal } ] '}' // 1.常表达式初值 2.一维数组初值 3.二维数组初值
    // FIRST(ConstInitVal) = FIRST(ConstExp) + {‘{’} = {‘(’,Ident,Number,'+','−','!', ‘{’}
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (constExpNode != null) {
            sb.append(constExpNode);
        } else {
            sb.append(leftBrace.toString());
            for (int i = 0; i < constInitValNodes.size(); i++) {
                sb.append(constInitValNodes.get(i).toString());
                if (i < commas.size()) {
                    sb.append(commas.get(i).toString());
                }
            }
            sb.append(rightBrace.toString());
        }
        sb.append("<ConstInitVal>\n");
        return sb.toString();
    }

    public ConstExpNode getConstExpNode() {
        return constExpNode;
    }
}
