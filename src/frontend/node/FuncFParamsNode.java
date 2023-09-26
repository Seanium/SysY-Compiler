package frontend.node;

import frontend.Token;

import java.util.ArrayList;

public class FuncFParamsNode extends Node {
    private final ArrayList<FuncFParamNode> funcFParamNodes;
    private final ArrayList<Token> commas;

    public FuncFParamsNode(ArrayList<FuncFParamNode> funcFParamNodes, ArrayList<Token> commas) {
        this.funcFParamNodes = funcFParamNodes;
        this.commas = commas;
    }
}
