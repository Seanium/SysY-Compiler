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

    // 1.编译单元 CompUnit → {Decl} {FuncDef} MainFuncDef // 1.是否存在Decl 2.是否存在FuncDef
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (DeclNode declNode : declNodes) {
            sb.append(declNode.toString());
        }
        for (FuncDefNode funcDefNode : funcDefNodes) {
            sb.append(funcDefNode.toString());
        }
        sb.append(mainFuncDefNode.toString());
        sb.append("<CompUnit>\n");
        return sb.toString();
    }

    public ArrayList<DeclNode> getDeclNodes() {
        return declNodes;
    }

    public ArrayList<FuncDefNode> getFuncDefNodes() {
        return funcDefNodes;
    }

    public MainFuncDefNode getMainFuncDefNode() {
        return mainFuncDefNode;
    }
}
