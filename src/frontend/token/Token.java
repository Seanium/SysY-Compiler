package frontend.token;

public class Token {
    private final TokenType type;
    private final String value;
    private final int lineNum;

    public Token(TokenType type, String value, int lineNum) {
        this.type = type;
        this.value = value;
        this.lineNum = lineNum;
//            System.out.println(this);
    }

    @Override
    public String toString() {
        return String.format("%s %s\n", type, value);
    }

    public TokenType getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public int getLineNum() {
        return lineNum;
    }
}
