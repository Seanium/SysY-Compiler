package frontend;

public class Parser {
    private final Lexer lexer;

    public Parser(Lexer lexer) {
        this.lexer = lexer;
    }

    private boolean match(TokenType tokenType) {
        if (lexer.getType() == tokenType) {
            lexer.next();
            return true;
        } else {
            return false;
        }
    }


    // 1.编译单元 CompUnit → {Decl} {FuncDef} MainFuncDef // 1.是否存在Decl 2.是否存在FuncDef
    private void parseCompUnit() {

    }

    // 2.声明 Decl → ConstDecl | VarDecl // 覆盖两种声明
    private void parseDecl() {

    }

    // 3.常量声明 ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';' // 1.花括号内重复0次 2.花括号内重复多次
    private void parseConstDecl() {

    }

    // 4.基本类型 BType → 'int' // 存在即可
    private Node parseBType() {
        match(TokenType.INTTK);
        return new BTypeNode(lexer.getCurToken());
    }

    class Node {

    }

    // 4.基本类型 BType
    class BTypeNode extends Node {
        private final Token token;

        public BTypeNode(Token token) {
            this.token = token;
        }
    }
}
