package frontend.node;

import frontend.symbol.Param;
import frontend.token.Token;

import java.util.ArrayList;

public class FuncRParamsNode extends Node {
    private final ArrayList<ExpNode> expNodes;
    private final ArrayList<Token> commas;


    public FuncRParamsNode(ArrayList<ExpNode> expNodes, ArrayList<Token> commas) {
        this.expNodes = expNodes;
        this.commas = commas;
    }

    public ArrayList<Param> toParams() {
        ArrayList<Param> params = new ArrayList<>();
        for (ExpNode expNode : expNodes) {
            params.add(new Param("", expNode.calDim())); // 实参无需名字, 只需类型（维数）, 用于与形参类型比较，检测错误类型e【函数参数类型不匹配】
        }
        return params;
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

    public ArrayList<ExpNode> getExpNodes() {
        return expNodes;
    }
}
