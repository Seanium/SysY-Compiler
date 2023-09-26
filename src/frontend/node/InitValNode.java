package frontend.node;

import frontend.Token;

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
}
