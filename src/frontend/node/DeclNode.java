package frontend.node;

public class DeclNode extends Node {
    private final ConstDeclNode constDeclNode;
    private final VarDeclNode varDeclNode;

    public DeclNode(ConstDeclNode constDeclNode) {
        this.constDeclNode = constDeclNode;
        this.varDeclNode = null;
    }

    public DeclNode(VarDeclNode varDeclNode) {
        this.varDeclNode = varDeclNode;
        this.constDeclNode = null;
    }

    // 2.声明 Decl → ConstDecl | VarDecl // 覆盖两种声明
    @Override
    public String toString() {
        if (constDeclNode != null) {
            return constDeclNode.toString();
        } else {
            return varDeclNode.toString();
        }
    }
}
