package frontend.node;

import frontend.Token;

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
}
