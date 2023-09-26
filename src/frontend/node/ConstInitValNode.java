package frontend.node;

import frontend.Token;

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
}
