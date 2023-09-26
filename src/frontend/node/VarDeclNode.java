package frontend.node;

import frontend.Token;

import java.util.ArrayList;

public class VarDeclNode extends Node {
    private final BTypeNode bTypeNode;
    private final ArrayList<VarDefNode> varDefNodes;
    private final ArrayList<Token> commas;
    private final Token semicn;

    public VarDeclNode(BTypeNode bTypeNode, ArrayList<VarDefNode> varDefNodes, ArrayList<Token> commas, Token semicn) {
        this.bTypeNode = bTypeNode;
        this.varDefNodes = varDefNodes;
        this.commas = commas;
        this.semicn = semicn;
    }
}
