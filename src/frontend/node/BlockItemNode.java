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

    // 16.语句块项 BlockItem → Decl | Stmt // 覆盖两种语句块项
    // FIRST(Decl) = FIRST(ConstDecl ) + FIRST(VarDecl) = {‘const’, ‘int’}
    @Override
    public String toString() {
        if (declNode != null) {
            return declNode.toString();
        } else {
            return stmtNode.toString();
        }
    }
}
