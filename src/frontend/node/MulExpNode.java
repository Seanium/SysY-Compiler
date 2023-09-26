package frontend.node;

import frontend.Token;

public class MulExpNode extends Node {

    private final MulExpNode mulExpNode;
    private final Token op;
    private final UnaryExpNode unaryExpNode;


    public MulExpNode(MulExpNode mulExpNode, Token op, UnaryExpNode unaryExpNode) {
        this.mulExpNode = mulExpNode;
        this.op = op;
        this.unaryExpNode = unaryExpNode;
    }

    public MulExpNode(UnaryExpNode unaryExpNode) {
        this.mulExpNode = null;
        this.op = null;
        this.unaryExpNode = unaryExpNode;
    }

    public UnaryExpNode getUnaryExpNode() {
        return unaryExpNode;
    }
}
