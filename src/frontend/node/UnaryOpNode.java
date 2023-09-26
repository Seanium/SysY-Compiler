package frontend.node;

import frontend.Token;

public class UnaryOpNode extends Node {
    private final Token token;

    public UnaryOpNode(Token token) {
        this.token = token;
    }
}
