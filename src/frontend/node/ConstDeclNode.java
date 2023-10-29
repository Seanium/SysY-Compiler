package frontend.node;

import frontend.token.Token;

import java.util.ArrayList;

public class ConstDeclNode extends Node {
    private final Token constToken;
    private final BTypeNode bTypeNode;
    private final ArrayList<ConstDefNode> constDefNodes;
    private final ArrayList<Token> commas;
    private final Token semicn;

    public ConstDeclNode(Token constToken, BTypeNode bTypeNode, ArrayList<ConstDefNode> constDefNodes, ArrayList<Token> commas, Token semicn) {
        this.constToken = constToken;
        this.bTypeNode = bTypeNode;
        this.constDefNodes = constDefNodes;
        this.commas = commas;
        this.semicn = semicn;
    }

    // 3.常量声明 ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';' // 1.花括号内重复0次 2.花括号内重复多次
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(constToken.toString()).append(bTypeNode.toString());
        for (int i = 0; i < constDefNodes.size(); i++) {
            sb.append(constDefNodes.get(i).toString());
            if (i < commas.size()) {
                sb.append(commas.get(i).toString());
            }
        }
        sb.append(semicn.toString());
        sb.append("<ConstDecl>\n");
        return sb.toString();
    }

    public ArrayList<ConstDefNode> getConstDefNodes() {
        return constDefNodes;
    }
}
