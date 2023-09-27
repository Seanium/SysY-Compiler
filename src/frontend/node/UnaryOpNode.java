package frontend.node;

import frontend.Token;

public class UnaryOpNode extends Node {
    private final Token token;

    public UnaryOpNode(Token token) {
        this.token = token;
    }

    // 25.单目运算符 UnaryOp → '+' | '−' | '!' //注：'!'仅出现在条件表达式中 // 三种均需覆盖
    @Override
    public String toString() {
        return token.toString() + "<UnaryOp>\n";
    }
}
