package frontend;

import frontend.error.Error;
import frontend.error.ErrorList;
import frontend.error.ErrorType;
import frontend.token.Token;
import frontend.token.TokenType;

import java.util.ArrayList;
import java.util.HashMap;

public class Lexer {
    private static Lexer instance;

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
    private final String input;   // 输入的字符串
    private int pos;    // 当前读到的字符的位置
    private int lineNum;  // 当前读到的字符所在的行号
    private Token curToken;
    private Token lastToken;    // 前一个Token
    private final ErrorList errorList;

    private Lexer(String input) {
        this.input = input;
        this.pos = 0;
        this.lineNum = 1;
        this.next();
        this.errorList = ErrorList.getInstance();
    }

    public static Lexer getInstance(String input) {
        if (instance == null) {
            instance = new Lexer(input);
        }
        return instance;
    }

    // curToken在文末为null, 其他时候均为有效值 (不包括空白字符和换行符)
    public void next() {
        lastToken = curToken;
        curToken = null;    // 重置 curToken
        while (curToken == null) {
            if (pos == input.length()) {
                return;
            }
            char c = input.charAt(pos); // 当前字符
            char nextC = pos + 1 < input.length() ? input.charAt(pos + 1) : '\0';    // 下一个字符
            if (c == ' ' || c == '\t' || c == '\r') {   // 空白字符
                pos++;
            } else if (c == '\n') { // 换行符
                lineNum++;
                pos++;
            } else if (Character.isDigit(c)) { // 1.整型常量    //暂时允许0开头的整数
                curToken = getNumber();
            } else if (c == '_' || Character.isLetter(c)) { // 4~15.保留字 或 3.标识符
                curToken = getWord();
            } else if (c == '\"') { // 2.格式字符串
                curToken = getFormatString();
            } else if (c == '!') {   // 16.! 或 29.!=
                if (nextC == '=') {
                    curToken = new Token(TokenType.NEQ, "!=", lineNum);
                    pos += 2;
                } else {
                    curToken = new Token(TokenType.NOT, "!", lineNum);
                    pos++;
                }
            } else if (c == '&') {  // 17.&&
                if (nextC == '&') {
                    curToken = new Token(TokenType.AND, "&&", lineNum);
                    pos += 2;
                } else {
                    throw new RuntimeException("line " + lineNum + ": 词法错误(非考察项) - \"&&\"不完整");
                }
            } else if (c == '|') {  // 18.||
                if (nextC == '|') {
                    curToken = new Token(TokenType.OR, "||", lineNum);
                    pos += 2;
                } else {
                    throw new RuntimeException("line " + lineNum + ": 词法错误(非考察项) - \"||\"不完整");
                }
            } else if (c == '+') {  // 19.+
                curToken = new Token(TokenType.PLUS, "+", lineNum);
                pos++;
            } else if (c == '-') {  // 20.-
                curToken = new Token(TokenType.MINU, "-", lineNum);
                pos++;
            } else if (c == '*') {  // 21.*
                curToken = new Token(TokenType.MULT, "*", lineNum);
                pos++;
            } else if (c == '/') {  // 22./ 或 注释//... 或 注释/*...*/
                if (nextC == '/') {  // 注释//...
                    pos += 2;
                    while (pos < input.length() && input.charAt(pos) != '\n') {
                        pos++;
                    }
                } else if (nextC == '*') {   // 注释/*...*/
                    pos += 2;
                    while (pos < input.length()) {
                        if (input.charAt(pos) == '*' && pos + 1 < input.length() && input.charAt(pos + 1) == '/') {
                            pos += 2;
                            break;
                        } else {
                            if (input.charAt(pos) == '\n') {
                                lineNum++;
                            }
                            pos++;
                        }
                    }
                } else {
                    curToken = new Token(TokenType.DIV, "/", lineNum);
                    pos++;
                }
            } else if (c == '%') {  // 23.%
                curToken = new Token(TokenType.MOD, "%", lineNum);
                pos++;
            } else if (c == '<') {  // 24.< 或 25.<=
                if (nextC == '=') {
                    curToken = new Token(TokenType.LEQ, "<=", lineNum);
                    pos += 2;
                } else {
                    curToken = new Token(TokenType.LSS, "<", lineNum);
                    pos++;
                }
            } else if (c == '>') {  // 26.> 或 27.>=
                if (nextC == '=') {
                    curToken = new Token(TokenType.GEQ, ">=", lineNum);
                    pos += 2;
                } else {
                    curToken = new Token(TokenType.GRE, ">", lineNum);
                    pos++;
                }
            } else if (c == '=') {  // 28.== 或 30.=
                if (nextC == '=') {
                    curToken = new Token(TokenType.EQL, "==", lineNum);
                    pos += 2;
                } else {
                    curToken = new Token(TokenType.ASSIGN, "=", lineNum);
                    pos++;
                }
            } else if (c == ';') {  // 31.;
                curToken = new Token(TokenType.SEMICN, ";", lineNum);
                pos++;
            } else if (c == ',') {  // 32.,
                curToken = new Token(TokenType.COMMA, ",", lineNum);
                pos++;
            } else if (c == '(') {  // 33.(
                curToken = new Token(TokenType.LPARENT, "(", lineNum);
                pos++;
            } else if (c == ')') {  // 34.)
                curToken = new Token(TokenType.RPARENT, ")", lineNum);
                pos++;
            } else if (c == '[') {  // 35.[
                curToken = new Token(TokenType.LBRACK, "[", lineNum);
                pos++;
            } else if (c == ']') {  // 36.]
                curToken = new Token(TokenType.RBRACK, "]", lineNum);
                pos++;
            } else if (c == '{') {  // 37.{
                curToken = new Token(TokenType.LBRACE, "{", lineNum);
                pos++;
            } else if (c == '}') {  // 38.}
                curToken = new Token(TokenType.RBRACE, "}", lineNum);
                pos++;
            } else {
                throw new RuntimeException("line " + lineNum + ": 词法错误(非考察项) - 字符 '" + c + "' 不在单词表中");
            }
        }
    }

    // 预读一个token
    public Token preRead() {
        int prePos = pos;
        int preLineNum = lineNum;
        Token preToken = curToken;
        next();
        Token token = curToken;
        pos = prePos;
        lineNum = preLineNum;
        curToken = preToken;
        return token;
    }

    // 预读n个token
    public Token preRead(int n) {
        int prePos = pos;
        int preLineNum = lineNum;
        Token preToken = curToken;
        Token token = null;
        for (int i = 0; i < n; i++) {
            next();
            token = curToken;
        }
        pos = prePos;
        lineNum = preLineNum;
        curToken = preToken;
        return token;
    }

    // 子程序 识别数字
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
        return new Token(TokenType.INTCON, number, lineNum);
    }

    // 子程序 识别格式字符串
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
                    // 错误类型a【非法符号】
                    errorList.addError(new Error(ErrorType.a, lineNum));
                    pos++;
                } else {
                    sb.append(c);
                    pos++;
                }
            } else if (c == '%') {
                if (pos + 1 < input.length() && input.charAt(pos + 1) != 'd') {   // '%'必须连着'd'
                    // 错误类型a【非法符号】
                    errorList.addError(new Error(ErrorType.a, lineNum));
                    pos++;
                } else {
                    sb.append(c);
                    pos++;
                }
            } else {
                // 错误类型a【非法符号】
                errorList.addError(new Error(ErrorType.a, lineNum));
                pos++;
            }
        }
        String formatString = sb.toString();
        return new Token(TokenType.STRCON, formatString, lineNum);
    }

    // 子程序 识别保留字和标识符
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
        return new Token(reservedWords.getOrDefault(word, TokenType.IDENFR), word, lineNum);
    }

    // 单独进行一遍完整的词法分析
    public ArrayList<Token> tokenize() {
        ArrayList<Token> tokens = new ArrayList<>();
        while (pos < input.length()) {
            next();
            if (curToken != null) {
                tokens.add(curToken);
            }
        }
        return tokens;
    }

    // tokens数组转字符串，用于输出
    public String tokensToString(ArrayList<Token> tokens) {
        StringBuilder sb = new StringBuilder();
        for (Token token : tokens) {
            sb.append(token);
        }
        return String.valueOf(sb);
    }

    public Token getLastToken() {
        return lastToken;
    }

    public Token getCurToken() {
        return curToken;
    }

    public TokenType getType() {
        return curToken.getType();
    }

}