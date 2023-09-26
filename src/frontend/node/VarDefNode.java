package frontend.node;

import frontend.Token;

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
}
