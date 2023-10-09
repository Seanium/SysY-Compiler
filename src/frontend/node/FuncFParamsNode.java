package frontend.node;

import frontend.token.Token;

import java.util.ArrayList;

public class FuncFParamsNode extends Node {
    private final ArrayList<FuncFParamNode> funcFParamNodes;
    private final ArrayList<Token> commas;

    public FuncFParamsNode(ArrayList<FuncFParamNode> funcFParamNodes, ArrayList<Token> commas) {
        this.funcFParamNodes = funcFParamNodes;
        this.commas = commas;
    }

    // 13.函数形参表 FuncFParams → FuncFParam { ',' FuncFParam } // 1.花括号内重复0次 2.花括号内重复多次
    //（与AddExp 等不同，这里直接贪婪匹配，所有的FuncFParam 都在根结点FuncFParams 的下一层）

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < funcFParamNodes.size(); i++) {
            sb.append(funcFParamNodes.get(i).toString());
            if (i < commas.size()) {
                sb.append(commas.get(i).toString());
            }
        }
        sb.append("<FuncFParams>\n");
        return sb.toString();
    }
}
