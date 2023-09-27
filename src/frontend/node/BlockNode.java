package frontend.node;

import frontend.Token;

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
