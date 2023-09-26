package frontend.node;

import frontend.Token;

public class ForStmtNode extends Node {
    private final LValNode lValNode;
    private final Token assign;
    private final ExpNode expNode;

    public ForStmtNode(LValNode lValNode, Token assign, ExpNode expNode) {
        this.lValNode = lValNode;
        this.assign = assign;
        this.expNode = expNode;
    }
}
