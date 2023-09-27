package frontend.node;

import frontend.Token;

// 4.基本类型 BType
public class BTypeNode extends Node {
    private final Token token;

    public BTypeNode(Token token) {
        this.token = token;
    }

    // 4.基本类型 BType → 'int' // 存在即可
    @Override
    public String toString() {
        return token.toString();
    }
}
