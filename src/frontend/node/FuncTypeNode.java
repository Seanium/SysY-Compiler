package frontend.node;

import frontend.Token;

public class FuncTypeNode extends Node {
    private final Token token;

    public FuncTypeNode(Token token) {
        this.token = token;
    }

    // 12.函数类型 FuncType → 'void' | 'int' // 覆盖两种类型的函数
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(token.toString());
        sb.append("<FuncType>\n");
        return sb.toString();
    }
}
