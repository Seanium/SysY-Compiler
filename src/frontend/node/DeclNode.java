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
}
