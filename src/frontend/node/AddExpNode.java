package frontend.node;

import frontend.Token;

public class AddExpNode extends Node {
    private final AddExpNode addExpNode;
    private final Token op;
    private final MulExpNode mulExpNode;

    public AddExpNode(AddExpNode addExpNode, Token op, MulExpNode mulExpNode) {
        this.addExpNode = addExpNode;
        this.op = op;
        this.mulExpNode = mulExpNode;
    }

    public AddExpNode(MulExpNode mulExpNode) {
        this.addExpNode = null;
        this.op = null;
        this.mulExpNode = mulExpNode;
    }

    public MulExpNode getMulExpNode() {
        return mulExpNode;
    }
}
