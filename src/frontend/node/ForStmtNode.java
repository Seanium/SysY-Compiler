package frontend.node;

import frontend.token.Token;

public class ForStmtNode extends Node {
    private final LValNode lValNode;
    private final Token assign;
    private final ExpNode expNode;

    public ForStmtNode(LValNode lValNode, Token assign, ExpNode expNode) {
        this.lValNode = lValNode;
        this.assign = assign;
        this.expNode = expNode;
    }

    // 18.语句 ForStmt → LVal '=' Exp // 存在即可
    @Override
    public String toString() {
        String sb = lValNode.toString() +
                assign.toString() +
                expNode.toString() +
                "<ForStmt>\n";
        return sb;
    }
}
