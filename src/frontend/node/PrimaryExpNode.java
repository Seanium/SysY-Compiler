package frontend.node;

import frontend.token.Token;
import midend.IRBuilder;
import midend.ir.ArrayInitValue;
import midend.ir.Constant;
import midend.ir.Value;
import midend.ir.symbol.IRSymbolManager;
import midend.ir.type.ArrayType;
import midend.ir.type.PointerType;

import java.util.ArrayList;

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
            String ident = lValNode.getIdent().getValue();
            if (!lValNode.getExpNodes().isEmpty()) {    // 如果是数组元素
                // 虽然初始化元素【数组元素】不是llvm中编译时常量，但本次实验需要实现有初值数组元素的初值获取
                ArrayInitValue arrayInitValue = (ArrayInitValue) IRSymbolManager.getInstance().findSymbol(ident).getInitValue();
                if (arrayInitValue == null) {    // 如果没有初值
                    throw new RuntimeException("error: 初始化元素不是编译时常量");
                } else {    // 如果有初值
                    // 获取数组维数列表
                    ArrayType arrayType = (ArrayType) ((PointerType) IRSymbolManager.getInstance().findSymbol(ident).getSymbol().getType()).getTargetType();
                    ArrayList<Integer> dims = arrayType.getDims();
                    // 获取左值下标列表
                    ArrayList<Integer> indexes = new ArrayList<>();
                    for (ExpNode expNode1 : lValNode.getExpNodes()) {
                        indexes.add(expNode1.getAddExpNode().calVal());     // 此处的下标一定是可计算的
                    }
                    // 计算偏移量
                    int offset = IRBuilder.getInstance().calOffsetForCalVal(indexes, dims);
                    // 返回初值
                    return arrayInitValue.getIntByOffset(offset);
                }
            } else {    // 如果不是数组元素
                Value initValue = IRSymbolManager.getInstance().findSymbol(ident).getInitValue();
                if (initValue == null) {    // 如果没有初值
                    throw new RuntimeException("error: 初始化元素不是编译时常量");
                } else {    // 如果有初值
                    return ((Constant) initValue).getValue();
                }
            }
        } else {    // PrimaryExp → Number
            return Integer.parseInt(numberNode.getIntConst().getValue());
        }
    }
}
