package frontend.node;

import frontend.Token;

public class RelExpNode extends Node {

    private final RelExpNode relExpNode;
    private final Token op;
    private final AddExpNode addExpNode;

    public RelExpNode(RelExpNode relExpNode, Token op, AddExpNode addExpNode) {
        this.relExpNode = relExpNode;
        this.op = op;
        this.addExpNode = addExpNode;
    }

    public RelExpNode(AddExpNode addExpNode) {
        this.relExpNode = null;
        this.op = null;
        this.addExpNode = addExpNode;
    }
}
