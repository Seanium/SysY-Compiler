package frontend.node;

public class BlockItemNode extends Node {
    private DeclNode declNode = null;
    private StmtNode stmtNode = null;

    public BlockItemNode(DeclNode declNode) {
        this.declNode = declNode;
    }

    public BlockItemNode(StmtNode stmtNode) {
        this.stmtNode = stmtNode;
    }
}
