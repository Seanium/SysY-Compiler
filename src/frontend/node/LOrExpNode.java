package frontend.node;

import frontend.Token;

public class LOrExpNode extends Node {
    private final LOrExpNode lOrExpNode;
    private final Token op;
    private final LAndExpNode lAndExpNode;

    public LOrExpNode(LOrExpNode lOrExpNode, Token op, LAndExpNode lAndExpNode) {
        this.lOrExpNode = lOrExpNode;
        this.op = op;
        this.lAndExpNode = lAndExpNode;
    }

    public LOrExpNode(LAndExpNode lAndExpNode) {
        this.lOrExpNode = null;
        this.op = null;
        this.lAndExpNode = lAndExpNode;
    }
}
