package frontend.node;

import frontend.token.Token;

import java.util.ArrayList;

public class VarDeclNode extends Node {
    private final BTypeNode bTypeNode;
    private final ArrayList<VarDefNode> varDefNodes;
    private final ArrayList<Token> commas;
    private final Token semicn;

    public VarDeclNode(BTypeNode bTypeNode, ArrayList<VarDefNode> varDefNodes, ArrayList<Token> commas, Token semicn) {
        this.bTypeNode = bTypeNode;
        this.varDefNodes = varDefNodes;
        this.commas = commas;
        this.semicn = semicn;
    }

    // 7.变量声明 VarDecl → BType VarDef { ',' VarDef } ';' // 1.花括号内重复0次 2.花括号内重复多次
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(bTypeNode.toString());
        for (int i = 0; i < varDefNodes.size(); i++) {
            sb.append(varDefNodes.get(i).toString());
            if (i < commas.size()) {
                sb.append(commas.get(i).toString());
            }
        }
        sb.append(semicn.toString());
        sb.append("<VarDecl>\n");
        return sb.toString();
    }
}
