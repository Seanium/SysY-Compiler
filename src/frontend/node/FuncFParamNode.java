package frontend.node;

import frontend.symbol.ArraySymbol;
import frontend.symbol.Param;
import frontend.token.Token;

import java.util.ArrayList;

public class FuncFParamNode extends Node {
    private final BTypeNode bTypeNode;
    private final Token ident;
    private final ArrayList<Token> leftBrackets;
    private final ArrayList<ConstExpNode> constExpNodes;
    private final ArrayList<Token> rightBrackets;

    public FuncFParamNode(BTypeNode bTypeNode, Token ident, ArrayList<Token> leftBrackets, ArrayList<ConstExpNode> constExpNodes, ArrayList<Token> rightBrackets) {
        this.bTypeNode = bTypeNode;
        this.ident = ident;
        this.leftBrackets = leftBrackets;
        this.constExpNodes = constExpNodes;
        this.rightBrackets = rightBrackets;
    }

    public Token getIdent() {
        return ident;
    }

    public ArrayList<Token> getLeftBrackets() {
        return leftBrackets;
    }

    public ArrayList<ConstExpNode> getConstExpNodes() {
        return constExpNodes;
    }

    public Param toParam() {
        return new Param(ident.getValue(), leftBrackets.size());
    }

    // 14.函数形参 FuncFParam → BType Ident ['[' ']' { '[' ConstExp ']' }] // 1.普通变量2.一维数组变量 3.二维数组变量
    public ArraySymbol toArraySymbol() {
        return new ArraySymbol(ident.getValue(), false, leftBrackets.size());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(bTypeNode.toString());
        sb.append(ident.toString());
        if (!leftBrackets.isEmpty()) {
            sb.append(leftBrackets.get(0).toString());
            sb.append(rightBrackets.get(0).toString());
            if (leftBrackets.size() > 1) {
                for (int i = 1; i < leftBrackets.size(); i++) {
                    sb.append(leftBrackets.get(i).toString());
                    sb.append(constExpNodes.get(i - 1).toString());
                    sb.append(rightBrackets.get(i).toString());
                }
            }
        }
        sb.append("<FuncFParam>\n");
        return sb.toString();
    }
}
