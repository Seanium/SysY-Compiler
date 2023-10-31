package frontend.node;

import frontend.symbol.ArraySymbol;
import frontend.symbol.Param;
import frontend.token.Token;
import midend.IRBuilder;
import midend.ir.type.IntegerType;
import midend.ir.type.Type;

import java.util.ArrayList;

public class FuncFParamNode extends Node {
    private final BTypeNode bTypeNode;
    private final Token ident;
    private final ArrayList<Token> leftBrackets;
    private final ArrayList<ConstExpNode> constExpNodes;
    private final ArrayList<Token> rightBrackets;
    private midend.ir.Param IRParam;

    public FuncFParamNode(BTypeNode bTypeNode, Token ident, ArrayList<Token> leftBrackets, ArrayList<ConstExpNode> constExpNodes, ArrayList<Token> rightBrackets) {
        this.bTypeNode = bTypeNode;
        this.ident = ident;
        this.leftBrackets = leftBrackets;
        this.constExpNodes = constExpNodes;
        this.rightBrackets = rightBrackets;
        this.IRParam = null;
    }

    public Token getIdent() {
        return ident;
    }

    public Param toParam() {
        return new Param(ident.getValue(), leftBrackets.size());
    }

    // 14.函数形参 FuncFParam → BType Ident ['[' ']' { '[' ConstExp ']' }] // 1.普通变量2.一维数组变量 3.二维数组变量
    public ArraySymbol toArraySymbol() {
        return new ArraySymbol(ident.getValue(), false, leftBrackets.size());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(bTypeNode.toString());
        sb.append(ident.toString());
        if (!leftBrackets.isEmpty()) {
            sb.append(leftBrackets.get(0).toString());
            sb.append(rightBrackets.get(0).toString());
            if (leftBrackets.size() > 1) {
                for (int i = 1; i < leftBrackets.size(); i++) {
                    sb.append(leftBrackets.get(i).toString());
                    sb.append(constExpNodes.get(i - 1).toString());
                    sb.append(rightBrackets.get(i).toString());
                }
            }
        }
        sb.append("<FuncFParam>\n");
        return sb.toString();
    }

    public midend.ir.Param toIRParam() {
        // todo 考虑数组，计算形参实际维数
        if (IRParam == null) {  // 如果未创建过，则new (因为每次函数定义会访问两次形参结点，第一次是填充符号表，第二次是创建形参相关语句)
            Type type = IntegerType.i32;
            IRParam = new midend.ir.Param(type, IRBuilder.getInstance().genLocalVarName());
        }
        return IRParam;
    }
}
