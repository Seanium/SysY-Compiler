package frontend;

import java.util.ArrayList;
import java.util.HashMap;

public class Lexer {
    private final String input;   // 输入的字符串
    private int pos;    // 当前读到的字符的位置

    // 定义保留字表 (4.main ~ 15.void)
    private static final HashMap<String, TokenType> reservedWords = new HashMap<>() {{
        put("main", TokenType.MAINTK);          // 4.main
        put("const", TokenType.CONSTTK);        // 5.const
        put("int", TokenType.INTTK);            // 6.int
        put("break", TokenType.BREAKTK);        // 7.break
        put("continue", TokenType.CONTINUETK);  // 8.continue
        put("if", TokenType.IFTK);              // 9.if
        put("else", TokenType.ELSETK);          // 10.else
        put("for", TokenType.FORTK);            // 11.for
        put("getint", TokenType.GETINTTK);      // 12.getint
        put("printf", TokenType.PRINTFTK);      // 13.printf
        put("return", TokenType.RETURNTK);      // 14.return
        put("void", TokenType.VOIDTK);          // 15.void
    }};

    public Lexer(String input) {
        this.input = input;
        this.pos = 0;
    }

    public ArrayList<Token> tokenize() {
        int lineNum = 1;
        ArrayList<Token> tokens = new ArrayList<>();
        while (pos < input.length()) {
            char c = input.charAt(pos); // 当前字符
            char next = pos + 1 < input.length() ? input.charAt(pos + 1) : '\0';    // 下一个字符
            if (c == ' ' || c == '\t' || c == '\r') {   // 空白字符
                pos++;
            } else if (c == '\n') { // 换行符
                lineNum++;
                pos++;
            } else if (Character.isDigit(c) && c != '0') { // 1.整型常量(非0)
                tokens.add(getNumber());
            } else if (c == '0') {  // 1.整型常量(0)
                tokens.add(new Token(TokenType.INTCON, "0"));
                pos++;
            } else if (c == '_' || Character.isLetter(c)) { // 4~15.保留字 或 3.标识符
                tokens.add(getWord());
            } else if (c == '\"') { // 2.格式字符串
                tokens.add(getFormatString());
            } else if (c == '!') {   // 16.! 或 29.!=
                if (next == '=') {
                    tokens.add(new Token(TokenType.NEQ, "!="));
                    pos += 2;
                } else {
                    tokens.add(new Token(TokenType.NOT, "!"));
                    pos++;
                }
            } else if (c == '&') {  // 17.&&
                if (next == '&') {
                    tokens.add(new Token(TokenType.AND, "&&"));
                    pos += 2;
                } else {
                    // TODO 词法错误的处理
                    throw new RuntimeException("line " + lineNum + ": 词法错误");
                }
            } else if (c == '|') {  // 18.||
                if (next == '|') {
                    tokens.add(new Token(TokenType.OR, "||"));
                    pos += 2;
                } else {
                    // TODO 词法错误的处理
                    throw new RuntimeException("line " + lineNum + ": 词法错误");
                }
            } else if (c == '+') {  // 19.+
                tokens.add(new Token(TokenType.PLUS, "+"));
                pos++;
            } else if (c == '-') {  // 20.-
                tokens.add(new Token(TokenType.MINU, "-"));
                pos++;
            } else if (c == '*') {  // 21.*
                tokens.add(new Token(TokenType.MULT, "*"));
                pos++;
            } else if (c == '/') {  // 22./ 或 注释//... 或 注释/*...*/
                if (next == '/') {  // 注释//...
                    pos += 2;
                    while (pos < input.length() && input.charAt(pos) != '\n') {
                        pos++;
                    }
                } else if (next == '*') {   // 注释/*...*/
                    pos += 2;
                    while (pos < input.length()) {
                        if (input.charAt(pos) == '*' && pos + 1 < input.length() && input.charAt(pos + 1) == '/') {
                            pos += 2;
                            break;
                        } else {
                            pos++;
                        }
                    }
                } else {
                    tokens.add(new Token(TokenType.DIV, "/"));
                    pos++;
                }
            } else if (c == '%') {  // 23.%
                tokens.add(new Token(TokenType.MOD, "%"));
                pos++;
            } else if (c == '<') {  // 24.< 或 25.<=
                if (next == '=') {
                    tokens.add(new Token(TokenType.LEQ, "<="));
                    pos += 2;
                } else {
                    tokens.add(new Token(TokenType.LSS, "<"));
                    pos++;
                }
            } else if (c == '>') {  // 26.> 或 27.>=
                if (next == '=') {
                    tokens.add(new Token(TokenType.GEQ, ">="));
                    pos += 2;
                } else {
                    tokens.add(new Token(TokenType.GRE, ">"));
                    pos++;
                }
            } else if (c == '=') {  // 28.== 或 30.=
                if (next == '=') {
                    tokens.add(new Token(TokenType.EQL, "=="));
                    pos += 2;
                } else {
                    tokens.add(new Token(TokenType.ASSIGN, "="));
                    pos++;
                }
            } else if (c == ';') {  // 31.;
                tokens.add(new Token(TokenType.SEMICN, ";"));
                pos++;
            } else if (c == ',') {  // 32.,
                tokens.add(new Token(TokenType.COMMA, ","));
                pos++;
            } else if (c == '(') {  // 33.(
                tokens.add(new Token(TokenType.LPARENT, "("));
                pos++;
            } else if (c == ')') {  // 34.)
                tokens.add(new Token(TokenType.RPARENT, ")"));
                pos++;
            } else if (c == '[') {  // 35.[
                tokens.add(new Token(TokenType.LBRACK, "["));
                pos++;
            } else if (c == ']') {  // 36.]
                tokens.add(new Token(TokenType.RBRACK, "]"));
                pos++;
            } else if (c == '{') {  // 37.{
                tokens.add(new Token(TokenType.LBRACE, "{"));
                pos++;
            } else if (c == '}') {  // 38.}
                tokens.add(new Token(TokenType.RBRACE, "}"));
                pos++;
            } else {
                // TODO 词法错误的处理
                throw new RuntimeException("line " + lineNum + ": 词法错误");
            }
        }
        return tokens;
    }

    private Token getWord() {
        StringBuilder sb = new StringBuilder();
        while (pos < input.length()) {
            char c = input.charAt(pos);
            if (c == '_' || Character.isLetter(c) || Character.isDigit(c)) {
                sb.append(c);
                pos++;
            } else {
                break;
            }
        }
        String word = sb.toString();
        return new Token(reservedWords.getOrDefault(word, TokenType.IDENFR), word);
    }

    private Token getNumber() {
        StringBuilder sb = new StringBuilder();
        while (pos < input.length()) {
            char c = input.charAt(pos);
            if (Character.isDigit(c)) {
                sb.append(c);
                pos++;
            } else {
                break;
            }
        }
        String number = sb.toString();
        return new Token(TokenType.INTCON, number);
    }

    private Token getFormatString() {
        StringBuilder sb = new StringBuilder();
        sb.append('\"');
        pos++;
        while (pos < input.length()) {
            char c = input.charAt(pos);
            if (c == '\"') {
                sb.append(c);
                pos++;
                break;
            } else if (c == 32 || c == 33 || (c >= 40 && c <= 126)) {
                if (c == '\\' && pos + 1 < input.length() && input.charAt(pos + 1) != 'n') {    // '\'后面必须连着'n'
                    // TODO 词法错误的处理
                    throw new RuntimeException("line " + pos + ": 词法错误");
                } else {
                    sb.append(c);
                    pos++;
                }
            } else if (c == '%') {
                if (pos + 1 < input.length() && input.charAt(pos + 1) != 'd') {   // '%'必须连着'd'
                    // TODO 词法错误的处理
                    throw new RuntimeException("line " + pos + ": 词法错误");
                } else {
                    sb.append(c);
                    pos++;
                }
            } else {
                // TODO 词法错误的处理
                throw new RuntimeException("line " + pos + ": 词法错误");
            }
        }
        String formatString = sb.toString();
        return new Token(TokenType.STRCON, formatString);
    }

    // 输出tokens
    public String TokensToString(ArrayList<Token> tokens) {
        StringBuilder sb = new StringBuilder();
        for (Token token : tokens) {
            sb.append(token).append("\n");
        }
        return String.valueOf(sb);
    }

    enum TokenType {
        INTCON, // 整型常量
        STRCON, // 格式字符串
        IDENFR, // 标识符
        MAINTK, CONSTTK, INTTK, BREAKTK, CONTINUETK, IFTK, ELSETK, FORTK, GETINTTK, PRINTFTK, RETURNTK, VOIDTK, // 保留字
        NOT, AND, OR, PLUS, MINU, MULT, DIV, MOD, LSS, LEQ, GRE, GEQ, EQL, NEQ, ASSIGN, SEMICN, COMMA, LPARENT, RPARENT, LBRACK, RBRACK, LBRACE, RBRACE // 分界符
    }

    public static class Token {
        final TokenType type;
        final String value;

        private Token(TokenType type, String value) {
            this.type = type;
            this.value = value;
            System.out.println(this);
        }

        @Override
        public String toString() {
            return String.format("%s %s", type, value);
        }
    }
}