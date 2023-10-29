package frontend.node;

import frontend.symbol.ArraySymbol;
import frontend.symbol.Symbol;
import frontend.symbol.SymbolTables;
import frontend.token.Token;

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

    public Token getIdent() {
        return ident;
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

    public int calDim() {
        SymbolTables symbolTables = SymbolTables.getInstance();
        Symbol symbol = symbolTables.findSymbol(ident.getValue());
        if (symbol instanceof ArraySymbol) {
            // 左值的实际维数是变量实际维数 - 左值中括号数量
            // 比如 int a[2][2]; 则左值a[0]的维数为 2 - 1 = 1
            return ((ArraySymbol) symbol).getDim() - expNodes.size();
        } else {
            return -1;  // 找不到变量声明，返回-1
        }
    }

    public ArrayList<ExpNode> getExpNodes() {
        return expNodes;
    }
}
