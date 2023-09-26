package frontend.node;

import java.util.ArrayList;

public class CompUnitNode extends Node {
    private final ArrayList<DeclNode> declNodes;
    private final ArrayList<FuncDefNode> funcDefNodes;
    private final MainFuncDefNode mainFuncDefNode;

    public CompUnitNode(ArrayList<DeclNode> declNodes, ArrayList<FuncDefNode> funcDefNodes, MainFuncDefNode mainFuncDefNode) {
        this.declNodes = declNodes;
        this.funcDefNodes = funcDefNodes;
        this.mainFuncDefNode = mainFuncDefNode;
    }
}
