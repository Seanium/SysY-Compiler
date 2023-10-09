package frontend;

import frontend.node.*;
import frontend.token.Token;
import frontend.token.TokenType;

import java.util.ArrayList;

public class Parser {
    private final Lexer lexer;

    public Parser(Lexer lexer) {
        this.lexer = lexer;
    }

    // 1.编译单元 CompUnit → {Decl} {FuncDef} MainFuncDef // 1.是否存在Decl 2.是否存在FuncDef
    public CompUnitNode parseCompUnit() {
        ArrayList<DeclNode> declNodes = new ArrayList<>();
        ArrayList<FuncDefNode> funcDefNodes = new ArrayList<>();
        MainFuncDefNode mainFuncDefNode = null;
        while (lexer.getType() == TokenType.CONSTTK || lexer.getType() == TokenType.INTTK || lexer.getType() == TokenType.VOIDTK) {
            if (lexer.getType() == TokenType.INTTK && lexer.preRead().getType() == TokenType.MAINTK) { // MainFuncDef
                mainFuncDefNode = parseMainFuncDef();
                break;
            } else if (lexer.preRead(2).getType() == TokenType.LPARENT) { // FuncDef
                funcDefNodes.add(parseFuncDef());
            } else { // Decl
                declNodes.add(parseDecl());
            }
        }
        return new CompUnitNode(declNodes, funcDefNodes, mainFuncDefNode);
    }

    // 2.声明 Decl → ConstDecl | VarDecl // 覆盖两种声明
    private DeclNode parseDecl() {
        if (lexer.getType() == TokenType.CONSTTK) {
            return new DeclNode(parseConstDecl());
        } else if (lexer.getType() == TokenType.INTTK) {
            return new DeclNode(parseVarDecl());
        } else {
            return null;
        }
    }

    // 3.常量声明 ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';' // 1.花括号内重复0次 2.花括号内重复多次
    private ConstDeclNode parseConstDecl() {
        if (lexer.getType() == TokenType.CONSTTK) {
            Token constToken = lexer.getCurToken();
            lexer.next();
            BTypeNode bTypeNode = parseBType();
            ArrayList<ConstDefNode> constDefNodes = new ArrayList<>();
            ArrayList<Token> commas = new ArrayList<>();
            constDefNodes.add(parseConstDef());
            while (lexer.getType() == TokenType.COMMA) {
                commas.add(lexer.getCurToken());
                lexer.next();
                constDefNodes.add(parseConstDef());
            }
            if (lexer.getType() == TokenType.SEMICN) {
                Token semicn = lexer.getCurToken();
                lexer.next();
                return new ConstDeclNode(constToken, bTypeNode, constDefNodes, commas, semicn);
            } else {
                throw new RuntimeException("错误类别码i: 缺少分号';'");    //TODO 错误处理行号
            }
        } else {
            return null;
        }
    }

    // 4.基本类型 BType → 'int' // 存在即可
    private BTypeNode parseBType() {
        if (lexer.getType() == TokenType.INTTK) {
            Token token = lexer.getCurToken();
            lexer.next();
            return new BTypeNode(token);
        } else {
            return null;
        }
    }

    // 5.常数定义 ConstDef → Ident { '[' ConstExp ']' } '=' ConstInitVal // 包含普通变量、一维数组、二维数组共三种情况
    private ConstDefNode parseConstDef() {
        if (lexer.getType() == TokenType.IDENFR) {
            Token ident = lexer.getCurToken();
            lexer.next();
            ArrayList<Token> leftBrackets = new ArrayList<>();
            ArrayList<ConstExpNode> constExpNodes = new ArrayList<>();
            ArrayList<Token> rightBrackets = new ArrayList<>();
            while (lexer.getType() == TokenType.LBRACK) {
                leftBrackets.add(lexer.getCurToken());
                lexer.next();
                constExpNodes.add(parseConstExp());
                if (lexer.getType() == TokenType.RBRACK) {
                    rightBrackets.add(lexer.getCurToken());
                    lexer.next();
                } else {
                    throw new RuntimeException("错误类别码k: 缺少右中括号']'");    //TODO 错误处理行号
                }
            }
            if (lexer.getType() == TokenType.ASSIGN) {
                Token assign = lexer.getCurToken();
                lexer.next();
                ConstInitValNode constInitValNode = parseConstInitVal();
                return new ConstDefNode(ident, leftBrackets, constExpNodes, rightBrackets, assign, constInitValNode);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    // 6.常量初值 ConstInitVal → ConstExp
    // | '{' [ ConstInitVal { ',' ConstInitVal } ] '}' // 1.常表达式初值 2.一维数组初值 3.二维数组初值
    // FIRST(ConstInitVal) = FIRST(ConstExp) + {‘{’} = {‘(’,Ident,Number,'+','−','!', ‘{’}
    private ConstInitValNode parseConstInitVal() {
        if (lexer.getType() == TokenType.LBRACE) {
            Token leftBrace = lexer.getCurToken();
            lexer.next();
            ArrayList<ConstInitValNode> constInitValNodes = new ArrayList<>();
            ArrayList<Token> commas = new ArrayList<>();
            if (isFirstOfConstInitVal()) {
                constInitValNodes.add(parseConstInitVal());
                while (lexer.getType() == TokenType.COMMA) {
                    commas.add(lexer.getCurToken());
                    lexer.next();
                    constInitValNodes.add(parseConstInitVal());
                }
            }
            if (lexer.getType() == TokenType.RBRACE) {
                Token rightBrace = lexer.getCurToken();
                lexer.next();
                return new ConstInitValNode(leftBrace, constInitValNodes, commas, rightBrace);
            } else {
                return null;
            }
        } else if (isFirstOfConstExp()) {
            return new ConstInitValNode(parseConstExp());
        } else {
            return null;
        }
    }

    // 7.变量声明 VarDecl → BType VarDef { ',' VarDef } ';' // 1.花括号内重复0次 2.花括号内重复多次
    private VarDeclNode parseVarDecl() {
        BTypeNode bTypeNode = parseBType();
        ArrayList<VarDefNode> varDefNodes = new ArrayList<>();
        ArrayList<Token> commas = new ArrayList<>();
        varDefNodes.add(parseVarDef());
        while (lexer.getType() == TokenType.COMMA) {
            commas.add(lexer.getCurToken());
            lexer.next();
            varDefNodes.add(parseVarDef());
        }
        if (lexer.getType() == TokenType.SEMICN) {
            Token semicn = lexer.getCurToken();
            lexer.next();
            return new VarDeclNode(bTypeNode, varDefNodes, commas, semicn);
        } else {
            throw new RuntimeException("错误类别码i: 缺少分号';'");    //TODO 错误处理行号
        }
    }

    // 8.变量定义 VarDef → Ident { '[' ConstExp ']' } // 包含普通变量、一维数组、二维数组定义
    // | Ident { '[' ConstExp ']' } '=' InitVal
    private VarDefNode parseVarDef() {
        if (lexer.getType() == TokenType.IDENFR) {
            Token ident = lexer.getCurToken();
            lexer.next();
            ArrayList<Token> leftBrackets = new ArrayList<>();
            ArrayList<ConstExpNode> constExpNodes = new ArrayList<>();
            ArrayList<Token> rightBrackets = new ArrayList<>();
            while (lexer.getType() == TokenType.LBRACK) {
                leftBrackets.add(lexer.getCurToken());
                lexer.next();
                constExpNodes.add(parseConstExp());
                if (lexer.getType() == TokenType.RBRACK) {
                    rightBrackets.add(lexer.getCurToken());
                    lexer.next();
                } else {
                    throw new RuntimeException("错误类别码k: 缺少右中括号']'");    //TODO 错误处理行号
                }
            }
            if (lexer.getType() == TokenType.ASSIGN) {
                Token assign = lexer.getCurToken();
                lexer.next();
                InitValNode initValNode = parseInitVal();
                return new VarDefNode(ident, leftBrackets, constExpNodes, rightBrackets, assign, initValNode);
            } else {
                return new VarDefNode(ident, leftBrackets, constExpNodes, rightBrackets);
            }
        } else {
            return null;
        }
    }

    // 9.变量初值 InitVal → Exp | '{' [ InitVal { ',' InitVal } ] '}'// 1.表达式初值 2.一维数组初值 3.二维数组初值
    // FIRST(InitVal) = FIRST(Exp) + {‘{’} = {‘(’,Ident,Number,'+','−','!', ‘{’}
    private InitValNode parseInitVal() {
        if (lexer.getType() == TokenType.LBRACE) {
            Token leftBrace = lexer.getCurToken();
            lexer.next();
            ArrayList<InitValNode> initValNodes = new ArrayList<>();
            ArrayList<Token> commas = new ArrayList<>();
            if (isFirstOfInitVal()) {
                initValNodes.add(parseInitVal());
                while (lexer.getType() == TokenType.COMMA) {
                    commas.add(lexer.getCurToken());
                    lexer.next();
                    initValNodes.add(parseInitVal());
                }
            }
            if (lexer.getType() == TokenType.RBRACE) {
                Token rightBrace = lexer.getCurToken();
                lexer.next();
                return new InitValNode(leftBrace, initValNodes, commas, rightBrace);
            } else {
                return null;
            }
        } else if (isFirstOfExp()) {
            return new InitValNode(parseExp());
        } else {
            return null;
        }
    }

    // 10.函数定义 FuncDef → FuncType Ident '(' [FuncFParams] ')' Block // 1.无形参 2.有形参
    // FIRST(FuncFParams ) = FIRST(FuncFParam) = {‘int’}
    private FuncDefNode parseFuncDef() {
        FuncTypeNode funcTypeNode = parseFuncType();
        Token ident = lexer.getCurToken();
        lexer.next();
        Token leftParen = lexer.getCurToken();
        lexer.next();
        FuncFParamsNode funcFParamsNode = lexer.getType() == TokenType.INTTK ? parseFuncFParams() : null;
        if (lexer.getType() == TokenType.RPARENT) {
            Token rightParen = lexer.getCurToken();
            lexer.next();
            BlockNode blockNode = parseBlock();
            return new FuncDefNode(funcTypeNode, ident, leftParen, funcFParamsNode, rightParen, blockNode);
        } else {
            throw new RuntimeException("错误类别码j: 缺少右小括号')'");    //TODO 错误处理行号
        }
    }

    // 11.主函数定义 MainFuncDef → 'int' 'main' '(' ')' Block // 存在main函数
    private MainFuncDefNode parseMainFuncDef() {
        Token intToken = lexer.getCurToken();
        lexer.next();
        Token mainToken = lexer.getCurToken();
        lexer.next();
        Token leftParen = lexer.getCurToken();
        lexer.next();
        if (lexer.getType() == TokenType.RPARENT) {
            Token rightParen = lexer.getCurToken();
            lexer.next();
            BlockNode blockNode = parseBlock();
            return new MainFuncDefNode(intToken, mainToken, leftParen, rightParen, blockNode);
        } else {
            throw new RuntimeException("错误类别码j: 缺少右小括号')'");    //TODO 错误处理行号
        }
    }

    // 12.函数类型 FuncType → 'void' | 'int' // 覆盖两种类型的函数
    private FuncTypeNode parseFuncType() {
        if (lexer.getType() == TokenType.VOIDTK) {
            Token token = lexer.getCurToken();
            lexer.next();
            return new FuncTypeNode(token);
        } else if (lexer.getType() == TokenType.INTTK) {
            Token token = lexer.getCurToken();
            lexer.next();
            return new FuncTypeNode(token);
        } else {
            return null;
        }
    }

    // 13.函数形参表 FuncFParams → FuncFParam { ',' FuncFParam } // 1.花括号内重复0次 2.花括号内重复多次
    //（与AddExp 等不同，这里直接贪婪匹配，所有的FuncFParam 都在根结点FuncFParams 的下一层）
    private FuncFParamsNode parseFuncFParams() {
        ArrayList<FuncFParamNode> funcFParamNodes = new ArrayList<>();
        ArrayList<Token> commas = new ArrayList<>();
        funcFParamNodes.add(parseFuncFParam());
        while (lexer.getType() == TokenType.COMMA) {
            commas.add(lexer.getCurToken());
            lexer.next();
            funcFParamNodes.add(parseFuncFParam());
        }
        return new FuncFParamsNode(funcFParamNodes, commas);
    }

    // 14.函数形参 FuncFParam → BType Ident ['[' ']' { '[' ConstExp ']' }] // 1.普通变量2.一维数组变量 3.二维数组变量
    private FuncFParamNode parseFuncFParam() {
        BTypeNode bTypeNode = parseBType();
        if (lexer.getType() == TokenType.IDENFR) {
            Token ident = lexer.getCurToken();
            lexer.next();
            ArrayList<Token> leftBrackets = new ArrayList<>();
            ArrayList<ConstExpNode> constExpNodes = new ArrayList<>();
            ArrayList<Token> rightBrackets = new ArrayList<>();
            if (lexer.getType() == TokenType.LBRACK) {
                leftBrackets.add(lexer.getCurToken());
                lexer.next();
                if (lexer.getType() == TokenType.RBRACK) {
                    rightBrackets.add(lexer.getCurToken());
                    lexer.next();
                } else {
                    throw new RuntimeException("错误类别码k: 缺少右中括号']'");    //TODO 错误处理行号
                }
                while (lexer.getType() == TokenType.LBRACK) {
                    leftBrackets.add(lexer.getCurToken());
                    lexer.next();
                    constExpNodes.add(parseConstExp());
                    if (lexer.getType() == TokenType.RBRACK) {
                        rightBrackets.add(lexer.getCurToken());
                        lexer.next();
                    } else {
                        throw new RuntimeException("错误类别码k: 缺少右中括号']'");    //TODO 错误处理行号
                    }
                }
            }
            return new FuncFParamNode(bTypeNode, ident, leftBrackets, constExpNodes, rightBrackets);
        } else {
            return null;
        }
    }

    // 15.语句块 Block → '{' { BlockItem } '}' // 1.花括号内重复0次 2.花括号内重复多次
    private BlockNode parseBlock() {
        Token leftBrace = lexer.getCurToken();
        lexer.next();
        ArrayList<BlockItemNode> blockItemNodes = new ArrayList<>();
        while (lexer.getType() != TokenType.RBRACE) {
            blockItemNodes.add(parseBlockItem());
        }
        Token rightBrace = lexer.getCurToken();
        lexer.next();
        return new BlockNode(leftBrace, blockItemNodes, rightBrace);
    }

    // 16.语句块项 BlockItem → Decl | Stmt // 覆盖两种语句块项
    // FIRST(Decl) = FIRST(ConstDecl ) + FIRST(VarDecl) = {‘const’, ‘int’}
    private BlockItemNode parseBlockItem() {
        if (lexer.getType() == TokenType.CONSTTK || lexer.getType() == TokenType.INTTK) {
            return new BlockItemNode(parseDecl());
        } else {
            return new BlockItemNode(parseStmt());
        }
    }

    // 17.语句 Stmt → LVal '=' Exp ';' // 每种类型的语句都要覆盖
    //| [Exp] ';' //有无Exp两种情况
    //| Block
    //| 'if' '(' Cond ')' Stmt [ 'else' Stmt ] // 1.有else 2.无else
    //| 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt // 1. 无缺省 2. 缺省第一个 ForStmt 3. 缺省Cond 4. 缺省第二个ForStmt
    //| 'break' ';'
    //| 'continue' ';'
    //| 'return' [Exp] ';' // 1.有Exp 2.无Exp
    //| LVal '=' 'getint''('')'';'
    //| 'printf''('FormatString{','Exp}')'';' // 1.有Exp 2.无Exp
    private StmtNode parseStmt() {
        if (lexer.getType() == TokenType.LBRACE) {  // Block
            BlockNode blockNode = parseBlock();
            return new StmtNode(blockNode);
        } else if (lexer.getType() == TokenType.IFTK) { // 'if' '(' Cond ')' Stmt [ 'else' Stmt ] // 1.有else 2.无else
            Token ifToken = lexer.getCurToken();
            lexer.next();
            Token leftParen = lexer.getCurToken();
            lexer.next();
            CondNode condNode = parseCond();
            if (lexer.getType() == TokenType.RPARENT) {
                Token rightParen = lexer.getCurToken();
                lexer.next();
                StmtNode stmtNode1 = parseStmt();
                if (lexer.getType() == TokenType.ELSETK) {
                    Token elseToken = lexer.getCurToken();
                    lexer.next();
                    StmtNode stmtNode2 = parseStmt();
                    return new StmtNode(ifToken, leftParen, condNode, rightParen, stmtNode1, elseToken, stmtNode2); // 有else
                } else {
                    return new StmtNode(ifToken, leftParen, condNode, rightParen, stmtNode1, null, null);   // 无else
                }
            } else {
                throw new RuntimeException("错误类别码j: 缺少右小括号')'");    //TODO 错误处理行号
            }
        } else if (lexer.getType() == TokenType.FORTK) {    // 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt // 1. 无缺省 2. 缺省第一个 ForStmt 3. 缺省Cond 4. 缺省第二个ForStmt
            Token forToken = lexer.getCurToken();
            lexer.next();
            Token leftParen = lexer.getCurToken();
            lexer.next();
            ForStmtNode forStmtNode1 = isFirstOfForStmt() ? parseForStmt() : null;
            if (lexer.getType() == TokenType.SEMICN) {
                Token semicn1 = lexer.getCurToken();
                lexer.next();
                CondNode condNode = isFirstOfCond() ? parseCond() : null;
                if (lexer.getType() == TokenType.SEMICN) {
                    Token semicn2 = lexer.getCurToken();
                    lexer.next();
                    ForStmtNode forStmtNode2 = isFirstOfForStmt() ? parseForStmt() : null;
                    if (lexer.getType() == TokenType.RPARENT) {
                        Token rightParen = lexer.getCurToken();
                        lexer.next();
                        StmtNode stmtNode = parseStmt();
                        return new StmtNode(forToken, leftParen, forStmtNode1, semicn1, condNode, semicn2, forStmtNode2, rightParen, stmtNode);
                    } else {
                        throw new RuntimeException("错误类别码j: 缺少右小括号')'");    //TODO 错误处理行号
                    }
                } else {
                    throw new RuntimeException("错误类别码i: 缺少分号';'");    //TODO 错误处理行号
                }
            } else {
                throw new RuntimeException("错误类别码i: 缺少分号';'");    //TODO 错误处理行号
            }
        } else if (lexer.getType() == TokenType.BREAKTK) {  // 'break' ';'
            Token breakToken = lexer.getCurToken();
            lexer.next();
            if (lexer.getType() == TokenType.SEMICN) {
                Token semicn = lexer.getCurToken();
                lexer.next();
                return new StmtNode(breakToken, semicn);
            } else {
                throw new RuntimeException("错误类别码i: 缺少分号';'");    //TODO 错误处理行号
            }
        } else if (lexer.getType() == TokenType.CONTINUETK) {   // 'continue' ';'
            Token continueToken = lexer.getCurToken();
            lexer.next();
            if (lexer.getType() == TokenType.SEMICN) {
                Token semicn = lexer.getCurToken();
                lexer.next();
                return new StmtNode(continueToken, semicn);
            } else {
                throw new RuntimeException("错误类别码i: 缺少分号';'");    //TODO 错误处理行号
            }
        } else if (lexer.getType() == TokenType.RETURNTK) { // 'return' [Exp] ';' // 1.有Exp 2.无Exp
            Token returnToken = lexer.getCurToken();
            lexer.next();
            ExpNode expNode = isFirstOfExp() ? parseExp() : null;
            if (lexer.getType() == TokenType.SEMICN) {
                Token semicn = lexer.getCurToken();
                lexer.next();
                return new StmtNode(returnToken, expNode, semicn);
            } else {
                throw new RuntimeException("错误类别码i: 缺少分号';'");    //TODO 错误处理行号
            }
        } else if (lexer.getType() == TokenType.PRINTFTK) { // 'printf''('FormatString{','Exp}')'';' // 1.有Exp 2.无Exp
            Token printfToken = lexer.getCurToken();
            lexer.next();
            Token leftParen = lexer.getCurToken();
            lexer.next();
            Token formatString = lexer.getCurToken();
            lexer.next();
            ArrayList<Token> commas = new ArrayList<>();
            ArrayList<ExpNode> expNodes = new ArrayList<>();
            while (lexer.getType() == TokenType.COMMA) {
                commas.add(lexer.getCurToken());
                lexer.next();
                expNodes.add(parseExp());
            }
            Token rightParen = lexer.getCurToken();
            lexer.next();
            if (lexer.getType() == TokenType.SEMICN) {
                Token semicn = lexer.getCurToken();
                lexer.next();
                return new StmtNode(printfToken, leftParen, formatString, commas, expNodes, rightParen, semicn);
            } else {
                throw new RuntimeException("错误类别码i: 缺少分号';'");    //TODO 错误处理行号
            }
        } else if (lexer.getType() == TokenType.SEMICN) {   // [Exp] ';' //无Exp的情况，即空语句';'
            Token semicn = lexer.getCurToken();
            lexer.next();
            return new StmtNode((ExpNode) null, semicn);
        } else if (isFirstOfExp()) {
            // Stmt → LVal '=' Exp ';'			//FIRST={Ident}
            //| [Exp] ';'						//FIRST={‘(’,Ident,Number,'+','−','!'}
            //| LVal '=' 'getint''('')'';' 	//FIRST={Ident}
            ExpNode expNode = parseExp();
            if (lexer.getType() == TokenType.SEMICN) {  // [Exp] ';' //有Exp的情况
                Token semicn = lexer.getCurToken();
                lexer.next();
                return new StmtNode(expNode, semicn);
            } else if (lexer.getType() == TokenType.ASSIGN) {
                LValNode lValNode = getLValNodeFromExpNode(expNode); // 从ExpNode中提取LValNode
                Token assign = lexer.getCurToken();
                lexer.next();
                if (lexer.getType() == TokenType.GETINTTK) {    // LVal '=' 'getint''('')'';'
                    Token getintToken = lexer.getCurToken();
                    lexer.next();
                    Token leftParen = lexer.getCurToken();
                    lexer.next();
                    if (lexer.getType() == TokenType.RPARENT) {
                        Token rightParen = lexer.getCurToken();
                        lexer.next();
                        if (lexer.getType() == TokenType.SEMICN) {
                            Token semicn = lexer.getCurToken();
                            lexer.next();
                            return new StmtNode(lValNode, assign, getintToken, leftParen, rightParen, semicn);
                        } else {
                            throw new RuntimeException("错误类别码i: 缺少分号';'");    //TODO 错误处理行号
                        }
                    } else {
                        throw new RuntimeException("错误类别码j: 缺少右小括号')'");    //TODO 错误处理行号
                    }
                } else {
                    expNode = parseExp();
                    if (lexer.getType() == TokenType.SEMICN) {  // Stmt → LVal '=' Exp ';'
                        Token semicn = lexer.getCurToken();
                        lexer.next();
                        return new StmtNode(lValNode, assign, expNode, semicn);
                    } else {
                        throw new RuntimeException("错误类别码i: 缺少分号';'");    //TODO 错误处理行号
                    }
                }
            } else {
                throw new RuntimeException("错误类别码i: 缺少分号';'");    //TODO 错误处理行号
            }
        } else {
            return null;
        }
    }

    // 18.语句 ForStmt → LVal '=' Exp // 存在即可
    private ForStmtNode parseForStmt() {
        if (lexer.getType() == TokenType.IDENFR) {
            LValNode lValNode = parseLVal();
            if (lexer.getType() == TokenType.ASSIGN) {
                Token assign = lexer.getCurToken();
                lexer.next();
                ExpNode expNode = parseExp();
                return new ForStmtNode(lValNode, assign, expNode);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    // 19.表达式 Exp → AddExp 注：SysY 表达式是int 型表达式 // 存在即可
    private ExpNode parseExp() {
        return new ExpNode(parseAddExp());
    }

    // 20.条件表达式 Cond → LOrExp // 存在即可
    private CondNode parseCond() {
        return new CondNode(parseLOrExp());
    }

    // 21.左值表达式 LVal → Ident {'[' Exp ']'} //1.普通变量 2.一维数组 3.二维数组
    private LValNode parseLVal() {
        if (lexer.getType() == TokenType.IDENFR) {
            Token ident = lexer.getCurToken();
            lexer.next();
            ArrayList<Token> leftBrackets = new ArrayList<>();
            ArrayList<ExpNode> expNodes = new ArrayList<>();
            ArrayList<Token> rightBrackets = new ArrayList<>();
            while (lexer.getType() == TokenType.LBRACK) {
                leftBrackets.add(lexer.getCurToken());
                lexer.next();
                expNodes.add(parseExp());
                if (lexer.getType() == TokenType.RBRACK) {
                    rightBrackets.add(lexer.getCurToken());
                    lexer.next();
                } else {
                    throw new RuntimeException("错误类别码k: 缺少右中括号']'");    //TODO 错误处理行号
                }
            }
            return new LValNode(ident, leftBrackets, expNodes, rightBrackets);
        } else {
            return null;
        }
    }

    // 22.基本表达式 PrimaryExp → '(' Exp ')' | LVal | Number // 三种情况均需覆盖
    private PrimaryExpNode parsePrimaryExp() {
        if (lexer.getType() == TokenType.LPARENT) {
            Token leftParen = lexer.getCurToken();
            lexer.next();
            ExpNode expNode = parseExp();
            if (lexer.getType() == TokenType.RPARENT) {
                Token rightParen = lexer.getCurToken();
                lexer.next();
                return new PrimaryExpNode(leftParen, expNode, rightParen);
            } else {
                throw new RuntimeException("错误类别码j: 缺少右小括号')'");    //TODO 这里的报错没有在表格中出现？？错误处理行号
            }
        } else if (lexer.getType() == TokenType.IDENFR) {
            return new PrimaryExpNode(parseLVal());
        } else if (lexer.getType() == TokenType.INTCON) {
            return new PrimaryExpNode(parseNumber());
        } else {
            return null;
        }
    }

    // 23.数值 Number → IntConst // 存在即可
    private NumberNode parseNumber() {
        if (lexer.getType() == TokenType.INTCON) {
            Token intConst = lexer.getCurToken();
            lexer.next();
            return new NumberNode(intConst);
        } else {
            return null;
        }
    }

    // 24.一元表达式 UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')' // 3种情况均需覆盖,函数调用也需要覆盖FuncRParams的不同情况
    // | UnaryOp UnaryExp // 存在即可
    //
    // FIRST(PrimaryExp) = {‘(’,Ident,Number}
    // FIRST(Ident '(' [FuncRParams] ')') = {Ident}     //预读'('进行判断
    // FIRST(UnaryOp UnaryExp) = {'+','−','!'}
    private UnaryExpNode parseUnaryExp() {
        if (lexer.getType() == TokenType.IDENFR && lexer.preRead().getType() == TokenType.LPARENT) { // Ident '(' [FuncRParams] ')'
            Token ident = lexer.getCurToken();
            lexer.next();
            Token leftParen = lexer.getCurToken();
            lexer.next();
            if (isFirstOfFuncRParams()) {
                FuncRParamsNode funcRParamsNode = parseFuncRParams();
                if (lexer.getType() == TokenType.RPARENT) {
                    Token rightParen = lexer.getCurToken();
                    lexer.next();
                    return new UnaryExpNode(ident, leftParen, funcRParamsNode, rightParen);
                } else {
                    throw new RuntimeException("错误类别码j: 缺少右小括号')'");    //TODO 错误处理行号
                }
            } else if (lexer.getType() == TokenType.RPARENT) {
                Token rightParen = lexer.getCurToken();
                lexer.next();
                return new UnaryExpNode(ident, leftParen, null, rightParen);
            } else {
                throw new RuntimeException("错误类别码j: 缺少右小括号')'");    //TODO 错误处理行号
            }
        } else if (lexer.getType() == TokenType.PLUS || lexer.getType() == TokenType.MINU || lexer.getType() == TokenType.NOT) {
            UnaryOpNode unaryOpNode = parseUnaryOp();
            UnaryExpNode unaryExpNode = parseUnaryExp();
            return new UnaryExpNode(unaryOpNode, unaryExpNode);
        } else {
            return new UnaryExpNode(parsePrimaryExp());
        }
    }

    // 25.单目运算符 UnaryOp → '+' | '−' | '!' //注：'!'仅出现在条件表达式中 // 三种均需覆盖
    private UnaryOpNode parseUnaryOp() {
        if (lexer.getType() == TokenType.PLUS) {
            Token token = lexer.getCurToken();
            lexer.next();
            return new UnaryOpNode(token);
        } else if (lexer.getType() == TokenType.MINU) {
            Token token = lexer.getCurToken();
            lexer.next();
            return new UnaryOpNode(token);
        } else if (lexer.getType() == TokenType.NOT) {
            Token token = lexer.getCurToken();
            lexer.next();
            return new UnaryOpNode(token);
        } else {
            return null;
        }
    }

    // 26.函数实参表 FuncRParams → Exp { ',' Exp } // 1.花括号内重复0次 2.花括号内重复多次 3.Exp需要覆盖数组传参和部分数组传参
    private FuncRParamsNode parseFuncRParams() {
        ArrayList<ExpNode> expNodes = new ArrayList<>();
        ArrayList<Token> commas = new ArrayList<>();
        expNodes.add(parseExp());
        while (lexer.getType() == TokenType.COMMA) {
            commas.add(lexer.getCurToken());
            lexer.next();
            expNodes.add(parseExp());
        }
        return new FuncRParamsNode(expNodes, commas);
    }

    // 27.乘除模表达式 MulExp → UnaryExp | MulExp ('*' | '/' | '%') UnaryExp // 1.UnaryExp 2.* 3./ 4.% 均需覆盖
    //【消除左递归】 AddExp → UnaryExp  {('*' | '/' | '%') UnaryExp}
    private MulExpNode parseMulExp() {
        MulExpNode mulExpNode = new MulExpNode(parseUnaryExp());
        while (lexer.getType() == TokenType.MULT || lexer.getType() == TokenType.DIV || lexer.getType() == TokenType.MOD) {
            Token op = lexer.getCurToken();
            lexer.next();
            UnaryExpNode unaryExpNode = parseUnaryExp();
            mulExpNode = new MulExpNode(mulExpNode, op, unaryExpNode);
        }
        return mulExpNode;
    }

    // 28.加减表达式 AddExp → MulExp | AddExp ('+' | '−') MulExp // 1.MulExp 2.+ 需覆盖 3.- 需覆盖
    // 【消除左递归】 AddExp → MulExp {('+' | '−') MulExp}
    private AddExpNode parseAddExp() {
        AddExpNode addExpNode = new AddExpNode(parseMulExp());
        while (lexer.getType() == TokenType.PLUS || lexer.getType() == TokenType.MINU) {
            Token op = lexer.getCurToken();
            lexer.next();
            MulExpNode mulExpNode = parseMulExp();
            addExpNode = new AddExpNode(addExpNode, op, mulExpNode);
        }
        return addExpNode;
    }

    // 29.关系表达式 RelExp → AddExp | RelExp ('<' | '>' | '<=' | '>=') AddExp // 1.AddExp 2.< 3.> 4.<= 5.>= 均需覆盖
    // 【消除左递归】 RelExp → AddExp { ('<' | '>' | '<=' | '>=') AddExp}
    private RelExpNode parseRelExp() {
        RelExpNode relExpNode = new RelExpNode(parseAddExp());
        while (lexer.getType() == TokenType.LSS || lexer.getType() == TokenType.LEQ || lexer.getType() == TokenType.GRE || lexer.getType() == TokenType.GEQ) {
            Token op = lexer.getCurToken();
            lexer.next();
            AddExpNode addExpNode = parseAddExp();
            relExpNode = new RelExpNode(relExpNode, op, addExpNode);
        }
        return relExpNode;
    }

    // 30.相等性表达式 EqExp → RelExp | EqExp ('==' | '!=') RelExp // 1.RelExp 2.== 3.!= 均需覆盖
    // 【消除左递归】 EqExp → RelExp { ('==' | '!=') RelExp}
    private EqExpNode parseEqExp() {
        EqExpNode eqExpNode = new EqExpNode(parseRelExp());
        while (lexer.getType() == TokenType.EQL || lexer.getType() == TokenType.NEQ) {
            Token op = lexer.getCurToken();
            lexer.next();
            RelExpNode relExpNode = parseRelExp();
            eqExpNode = new EqExpNode(eqExpNode, op, relExpNode);
        }
        return eqExpNode;
    }

    // 31.逻辑与表达式 LAndExp → EqExp | LAndExp '&&' EqExp // 1.EqExp 2.&& 均需覆盖
    // 【消除左递归】 LAndExp → EqExp {'&&' EqExp}
    private LAndExpNode parseLAndExp() {
        LAndExpNode lAndExpNode = new LAndExpNode(parseEqExp());
        while (lexer.getType() == TokenType.AND) {
            Token op = lexer.getCurToken();
            lexer.next();
            EqExpNode eqExpNode = parseEqExp();
            lAndExpNode = new LAndExpNode(lAndExpNode, op, eqExpNode);
        }
        return lAndExpNode;
    }

    // 32.逻辑或表达式 LOrExp → LAndExp | LOrExp '||' LAndExp // 1.LAndExp 2.|| 均需覆盖
    // 【消除左递归】 LOrExp → LAndExp {'||' LAndExp}
    private LOrExpNode parseLOrExp() {
        LOrExpNode lOrExpNode = new LOrExpNode(parseLAndExp());
        while (lexer.getType() == TokenType.OR) {
            Token op = lexer.getCurToken();
            lexer.next();
            LAndExpNode lAndExpNode = parseLAndExp();
            lOrExpNode = new LOrExpNode(lOrExpNode, op, lAndExpNode);
        }
        return lOrExpNode;
    }

    // 33.常量表达式 ConstExp → AddExp 注：使用的Ident 必须是常量 // 存在即可
    private ConstExpNode parseConstExp() {
        return new ConstExpNode(parseAddExp());
    }


    // FIRST(FuncRParams)=FIRST(Exp) = FIRST(AddExp) = FIRST(MulExp) = FIRST(UnaryExp)
    // = FIRST(PrimaryExp) + FIRST(Ident '(' [FuncRParams] ')') + FIRST(UnaryOp UnaryExp)
    // = {‘(’,Ident,Number,'+','−','!'}

    // 判断当前token是否在AddExp的First集中
    private boolean isFirstOfAddExp() {
        return lexer.getType() == TokenType.LPARENT || lexer.getType() == TokenType.IDENFR || lexer.getType() == TokenType.INTCON || lexer.getType() == TokenType.PLUS || lexer.getType() == TokenType.MINU || lexer.getType() == TokenType.NOT;
    }

    private boolean isFirstOfConstExp() {
        return isFirstOfAddExp();
    }

    private boolean isFirstOfExp() {
        return isFirstOfAddExp();
    }

    private boolean isFirstOfFuncRParams() {
        return isFirstOfAddExp();
    }


    private boolean isFirstOfConstInitVal() {
        return isFirstOfConstExp() || lexer.getType() == TokenType.LBRACE;
    }

    private boolean isFirstOfInitVal() {
        return isFirstOfExp() || lexer.getType() == TokenType.LBRACE;
    }

    private boolean isFirstOfForStmt() {
        return lexer.getType() == TokenType.IDENFR;
    }

    private boolean isFirstOfCond() {
        return isFirstOfAddExp();
    }

    // 用于Stmt处理多产生式，从ExpNode中提取出LValNode
    private LValNode getLValNodeFromExpNode(ExpNode expNode) {
        return expNode.getAddExpNode().getMulExpNode().getUnaryExpNode().getPrimaryExpNode().getlValNode();
    }
}
