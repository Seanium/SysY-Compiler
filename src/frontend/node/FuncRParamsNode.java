package frontend.node;

import frontend.Token;

import java.util.ArrayList;

public class FuncRParamsNode extends Node {
    private final ArrayList<ExpNode> expNodes;
    private final ArrayList<Token> commas;


    public FuncRParamsNode(ArrayList<ExpNode> expNodes, ArrayList<Token> commas) {
        this.expNodes = expNodes;
        this.commas = commas;
    }
}
