package frontend.node;

import frontend.token.Token;
import frontend.token.TokenType;

public class FuncTypeNode extends Node {
    private final Token token;

    public FuncTypeNode(Token token) {
        this.token = token;
    }

    // 函数返回值类型为void则返回true，数返回值类型为int则返回false
    public Boolean isVoid() {
        return token.getType() == TokenType.VOIDTK;
    }

    // 12.函数类型 FuncType → 'void' | 'int' // 覆盖两种类型的函数
    @Override
    public String toString() {
        return token.toString() +
                "<FuncType>\n";
    }
}
