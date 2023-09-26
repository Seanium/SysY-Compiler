package frontend.node;

import frontend.Token;

public class EqExpNode extends Node {
    private final EqExpNode eqExpNode;
    private final Token op;
    private final RelExpNode relExpNode;

    public EqExpNode(EqExpNode eqExpNode, Token op, RelExpNode relExpNode) {
        this.eqExpNode = eqExpNode;
        this.op = op;
        this.relExpNode = relExpNode;
    }

    public EqExpNode(RelExpNode relExpNode) {
        this.eqExpNode = null;
        this.op = null;
        this.relExpNode = relExpNode;
    }
}
