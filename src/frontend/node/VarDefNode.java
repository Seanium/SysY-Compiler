package frontend.node;

import frontend.symbol.ArraySymbol;
import frontend.token.Token;

import java.util.ArrayList;

public class VarDefNode extends Node {
    private final Token ident;
    private final ArrayList<Token> leftBrackets;
    private final ArrayList<ConstExpNode> constExpNodes;
    private final ArrayList<Token> rightBrackets;
    private final Token assign;
    private final InitValNode initValNode;

    public VarDefNode(Token ident, ArrayList<Token> leftBrackets, ArrayList<ConstExpNode> constExpNodes, ArrayList<Token> rightBrackets, Token assign, InitValNode initValNode) {
        this.ident = ident;
        this.leftBrackets = leftBrackets;
        this.constExpNodes = constExpNodes;
        this.rightBrackets = rightBrackets;
        this.assign = assign;
        this.initValNode = initValNode;
    }

    public VarDefNode(Token ident, ArrayList<Token> leftBrackets, ArrayList<ConstExpNode> constExpNodes, ArrayList<Token> rightBrackets) {
        this.ident = ident;
        this.leftBrackets = leftBrackets;
        this.constExpNodes = constExpNodes;
        this.rightBrackets = rightBrackets;
        this.assign = null;
        this.initValNode = null;
    }

    public Token getIdent() {
        return ident;
    }

    public ArraySymbol toArraySymbol() {
        return new ArraySymbol(ident.getValue(), false, constExpNodes.size());
    }

    // 8.变量定义 VarDef → Ident { '[' ConstExp ']' } // 包含普通变量、一维数组、二维数组定义
    // | Ident { '[' ConstExp ']' } '=' InitVal
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(ident.toString());
        for (int i = 0; i < leftBrackets.size(); i++) {
            sb.append(leftBrackets.get(i).toString());
            sb.append(constExpNodes.get(i).toString());
            sb.append(rightBrackets.get(i).toString());
        }
        if (assign != null) {
            sb.append(assign);
            sb.append(initValNode.toString());
        }
        sb.append("<VarDef>\n");
        return sb.toString();
    }
}
