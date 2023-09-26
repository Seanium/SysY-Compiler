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
}
