package frontend.node;

import frontend.Token;

public class MulExpNode extends Node {

    private final MulExpNode mulExpNode;
    private final Token op;
    private final UnaryExpNode unaryExpNode;


    public MulExpNode(MulExpNode mulExpNode, Token op, UnaryExpNode unaryExpNode) {
        this.mulExpNode = mulExpNode;
        this.op = op;
        this.unaryExpNode = unaryExpNode;
    }

    public MulExpNode(UnaryExpNode unaryExpNode) {
        this.mulExpNode = null;
        this.op = null;
        this.unaryExpNode = unaryExpNode;
    }

    public UnaryExpNode getUnaryExpNode() {
        return unaryExpNode;
    }

    // 27.乘除模表达式 MulExp → UnaryExp | MulExp ('*' | '/' | '%') UnaryExp // 1.UnaryExp 2.* 3./ 4.% 均需覆盖
    //【消除左递归】 AddExp → UnaryExp  {('*' | '/' | '%') UnaryExp}
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (mulExpNode == null) {
            sb.append(unaryExpNode.toString());
        } else {
            sb.append(mulExpNode);
            sb.append(op.toString());
            sb.append(unaryExpNode.toString());
        }
        sb.append("<MulExp>\n");
        return sb.toString();
    }
}