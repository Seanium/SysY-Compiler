package frontend.node;

import frontend.Token;

public class NumberNode extends Node {
    private final Token token;

    public NumberNode(Token token) {
        this.token = token;
    }
}
