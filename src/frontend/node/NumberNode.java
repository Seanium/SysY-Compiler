package frontend.node;

import frontend.token.Token;

public class NumberNode extends Node {
    private final Token intConst;

    public NumberNode(Token intConst) {
        this.intConst = intConst;
    }

    // 23.数值 Number → IntConst // 存在即可
    @Override
    public String toString() {
        return intConst.toString() + "<Number>\n";
    }
}
