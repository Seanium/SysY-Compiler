package frontend.node;

import frontend.token.Token;

import java.util.ArrayList;

public class InitValNode extends Node {
    private final Token leftBrace;
    private final ArrayList<InitValNode> initValNodes;
    private final ArrayList<Token> commas;
    private final Token rightBrace;
    private final ExpNode expNode;

    public InitValNode(Token leftBrace, ArrayList<InitValNode> initValNodes, ArrayList<Token> commas, Token rightBrace) {
        this.leftBrace = leftBrace;
        this.initValNodes = initValNodes;
        this.commas = commas;
        this.rightBrace = rightBrace;
        this.expNode = null;
    }

    public InitValNode(ExpNode expNode) {
        this.expNode = expNode;
        this.leftBrace = null;
        this.initValNodes = null;
        this.commas = null;
        this.rightBrace = null;
    }

    public ArrayList<InitValNode> getInitValNodes() {
        return initValNodes;
    }

    // 9.变量初值 InitVal → Exp | '{' [ InitVal { ',' InitVal } ] '}'// 1.表达式初值 2.一维数组初值 3.二维数组初值
    // FIRST(InitVal) = FIRST(Exp) + {‘{’} = {‘(’,Ident,Number,'+','−','!', ‘{’}
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (expNode != null) {
            sb.append(expNode);
        } else {
            sb.append(leftBrace.toString());
            for (int i = 0; i < initValNodes.size(); i++) {
                sb.append(initValNodes.get(i).toString());
                if (i < commas.size()) {
                    sb.append(commas.get(i).toString());
                }
            }
            sb.append(rightBrace.toString());
        }
        sb.append("<InitVal>\n");
        return sb.toString();
    }

    public ExpNode getExpNode() {
        return expNode;
    }
}
