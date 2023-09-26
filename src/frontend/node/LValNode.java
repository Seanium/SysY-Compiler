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
}
