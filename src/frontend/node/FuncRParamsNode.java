package frontend.node;

import frontend.token.Token;

import java.util.ArrayList;

public class FuncRParamsNode extends Node {
    private final ArrayList<ExpNode> expNodes;
    private final ArrayList<Token> commas;


    public FuncRParamsNode(ArrayList<ExpNode> expNodes, ArrayList<Token> commas) {
        this.expNodes = expNodes;
        this.commas = commas;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < expNodes.size(); i++) {
            sb.append(expNodes.get(i).toString());
            if (i < commas.size()) {
                sb.append(commas.get(i).toString());
            }
        }
        sb.append("<FuncRParams>\n");
        return sb.toString();
    }
}
