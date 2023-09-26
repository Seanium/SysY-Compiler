package frontend.node;

import frontend.Token;

public class LAndExpNode extends Node {
    private final LAndExpNode lAndExpNode;
    private final Token op;
    private final EqExpNode eqExpNode;

    public LAndExpNode(LAndExpNode lAndExpNode, Token op, EqExpNode eqExpNode) {
        this.lAndExpNode = lAndExpNode;
        this.op = op;
        this.eqExpNode = eqExpNode;
    }

    public LAndExpNode(EqExpNode eqExpNode) {
        this.lAndExpNode = null;
        this.op = null;
        this.eqExpNode = eqExpNode;
    }
}
