package frontend.node;

import frontend.token.Token;
import midend.ir.Constant;
import midend.ir.Value;
import midend.ir.symbol.IRSymbolManager;

public class PrimaryExpNode extends Node {

    private final Token leftParen;
    private final ExpNode expNode;
    private final Token rightParen;
    private final LValNode lValNode;
    private final NumberNode numberNode;


    public PrimaryExpNode(Token leftParen, ExpNode expNode, Token rightParen) {
        this.leftParen = leftParen;
        this.expNode = expNode;
        this.rightParen = rightParen;
        this.lValNode = null;
        this.numberNode = null;
    }

    public PrimaryExpNode(LValNode lValNode) {
        this.lValNode = lValNode;
        this.leftParen = null;
        this.expNode = null;
        this.rightParen = null;
        this.numberNode = null;
    }

    public PrimaryExpNode(NumberNode numberNode) {
        this.numberNode = numberNode;
        this.leftParen = null;
        this.expNode = null;
        this.rightParen = null;
        this.lValNode = null;
    }

    public LValNode getlValNode() {
        return lValNode;
    }

    // 22.基本表达式 PrimaryExp → '(' Exp ')' | LVal | Number // 三种情况均需覆盖
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (leftParen != null) {
            sb.append(leftParen);
            sb.append(expNode.toString());
            sb.append(rightParen.toString());
        } else if (lValNode != null) {
            sb.append(lValNode);
        } else if (numberNode != null) {
            sb.append(numberNode);
        }
        sb.append("<PrimaryExp>\n");
        return sb.toString();
    }

    public int calDim() {
        if (expNode != null) {  // PrimaryExp → '(' Exp ')'
            return expNode.calDim();
        } else if (lValNode != null) {  // PrimaryExp → LVal
            return lValNode.calDim();
        } else {    // PrimaryExp → Number
            return 0;
        }
    }

    public ExpNode getExpNode() {
        return expNode;
    }

    public NumberNode getNumberNode() {
        return numberNode;
    }

    public int calVal() {
        if (expNode != null) {  // PrimaryExp → '(' Exp ')'
            return expNode.getAddExpNode().calVal();
        } else if (lValNode != null) {  // PrimaryExp → LVal
            if (!lValNode.getExpNodes().isEmpty()) {
                throw new RuntimeException("error: 初始化元素[数组元素]不是编译时常量");
            }
            Value initValue = IRSymbolManager.getInstance().findSymbol(lValNode.getIdent().getValue()).getInitValue();
            if (initValue == null) {    // 如果没有初值
                throw new RuntimeException("error: 初始化元素不是编译时常量");
            } else {    // 如果有初值
                return ((Constant) initValue).getValue();
            }
        } else {    // PrimaryExp → Number
            return Integer.parseInt(numberNode.getIntConst().getValue());
        }
    }
}
