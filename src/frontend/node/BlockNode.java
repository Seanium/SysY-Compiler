package frontend.node;

import frontend.token.Token;

import java.util.ArrayList;

public class BlockNode extends Node {
    private final Token leftBrace;
    private final ArrayList<BlockItemNode> blockItemNodes;
    private final Token rightBrace;

    public BlockNode(Token leftBrace, ArrayList<BlockItemNode> blockItemNodes, Token rightBrace) {
        this.leftBrace = leftBrace;
        this.blockItemNodes = blockItemNodes;
        this.rightBrace = rightBrace;
    }

    // 是否在最后有return语句
    private Boolean isLastBlockItemNodeReturn() {
        if (blockItemNodes.isEmpty()) {
            return false;
        }
        if (blockItemNodes.get(blockItemNodes.size() - 1).getStmtNode() == null) {
            return false;
        }
        return blockItemNodes.get(blockItemNodes.size() - 1).getStmtNode().getReturnToken() != null;
    }

    // 有return值的语句, 返回true;
    // 没有return语句或有返回空的语句, 返回false
    public Boolean hasReturnInt() {
        if (!isLastBlockItemNodeReturn()) {
            return false;
        } else {
            return blockItemNodes.get(blockItemNodes.size() - 1).getStmtNode().getExpNode() != null;
        }
    }

    public Token getRETURNTK() {
        if (!isLastBlockItemNodeReturn()) {
            return null;
        }
        return blockItemNodes.get(blockItemNodes.size() - 1).getStmtNode().getReturnToken();
    }

    // 15.语句块 Block → '{' { BlockItem } '}' // 1.花括号内重复0次 2.花括号内重复多次
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(leftBrace.toString());
        for (BlockItemNode blockItemNode : blockItemNodes) {
            sb.append(blockItemNode.toString());
        }
        sb.append(rightBrace.toString());
        sb.append("<Block>\n");
        return sb.toString();
    }
}
