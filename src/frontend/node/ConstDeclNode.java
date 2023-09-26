package frontend.node;

import frontend.Token;

import java.util.ArrayList;

public class ConstDeclNode extends Node {
    private final Token constToken;
    private final BTypeNode bTypeNode;
    private final ArrayList<ConstDefNode> constDefNodes;
    private final ArrayList<Token> commas;
    private final Token semicn;

    public ConstDeclNode(Token constToken, BTypeNode bTypeNode, ArrayList<ConstDefNode> constDefNodes, ArrayList<Token> commas, Token semicn) {
        this.constToken = constToken;
        this.bTypeNode = bTypeNode;
        this.constDefNodes = constDefNodes;
        this.commas = commas;
        this.semicn = semicn;
    }
}
