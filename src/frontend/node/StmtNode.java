package frontend.node;

import frontend.token.Token;
import frontend.token.TokenType;

import java.util.ArrayList;

public class StmtNode extends Node {
    private BlockNode blockNode = null;
    private Token ifToken = null;
    private Token leftParen = null;
    private CondNode condNode = null;
    private Token rightParen = null;
    private StmtNode stmtNode1 = null;
    private Token elseToken = null;
    private StmtNode stmtNode2 = null;
    private Token forToken = null;
    private ForStmtNode forStmtNode1 = null;
    private Token semicn1 = null;
    private Token semicn2 = null;
    private ForStmtNode forStmtNode2 = null;
    private StmtNode stmtNode = null;
    private Token breakToken = null;
    private Token continueToken = null;
    private Token semicn = null;
    private Token returnToken = null;
    private ExpNode expNode = null;
    private Token printfToken = null;
    private Token formatString = null;
    private ArrayList<Token> commas = null;
    private ArrayList<ExpNode> expNodes = null;
    private LValNode lValNode = null;
    private Token assign = null;
    private Token getintToken = null;

    // Block
    public StmtNode(BlockNode blockNode) {
        this.blockNode = blockNode;
    }

    // 'if' '(' Cond ')' Stmt [ 'else' Stmt ] // 1.有else 2.无else
    public StmtNode(Token ifToken, Token leftParen, CondNode condNode, Token rightParen, StmtNode stmtNode1, Token elseToken, StmtNode stmtNode2) {
        this.ifToken = ifToken;
        this.leftParen = leftParen;
        this.condNode = condNode;
        this.rightParen = rightParen;
        this.stmtNode1 = stmtNode1;
        this.elseToken = elseToken;
        this.stmtNode2 = stmtNode2;
    }

    // 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt // 1. 无缺省 2. 缺省第一个 ForStmt 3. 缺省Cond 4. 缺省第二个ForStmt
    public StmtNode(Token forToken, Token leftParen, ForStmtNode forStmtNode1, Token semicn1, CondNode condNode, Token semicn2, ForStmtNode forStmtNode2, Token rightParen, StmtNode stmtNode) {
        this.forToken = forToken;
        this.leftParen = leftParen;
        this.forStmtNode1 = forStmtNode1;
        this.semicn1 = semicn1;
        this.condNode = condNode;
        this.semicn2 = semicn2;
        this.forStmtNode2 = forStmtNode2;
        this.rightParen = rightParen;
        this.stmtNode = stmtNode;
    }

    // 'break' ';'
    // 'continue' ';'
    public StmtNode(Token breakOrContinueToken, Token semicn) {
        if (breakOrContinueToken.getType() == TokenType.CONTINUETK) {
            this.continueToken = breakOrContinueToken;
        } else {
            this.breakToken = breakOrContinueToken;
        }
        this.semicn = semicn;
    }

    // 'return' [Exp] ';' // 1.有Exp 2.无Exp
    public StmtNode(Token returnToken, ExpNode expNode, Token semicn) {
        this.returnToken = returnToken;
        this.expNode = expNode;
        this.semicn = semicn;
    }

    // 'printf''('FormatString{','Exp}')'';' // 1.有Exp 2.无Exp
    public StmtNode(Token printfToken, Token leftParen, Token formatString, ArrayList<Token> commas, ArrayList<ExpNode> expNodes, Token rightParen, Token semicn) {
        this.printfToken = printfToken;
        this.leftParen = leftParen;
        this.formatString = formatString;
        this.commas = commas;
        this.expNodes = expNodes;
        this.rightParen = rightParen;
        this.semicn = semicn;
    }

    // [Exp] ';'
    public StmtNode(ExpNode expNode, Token semicn) {
        this.expNode = expNode;
        this.semicn = semicn;
    }

    // LVal '=' 'getint''('')'';'
    public StmtNode(LValNode lValNode, Token assign, Token getintToken, Token leftParen, Token rightParen, Token semicn) {
        this.lValNode = lValNode;
        this.assign = assign;
        this.getintToken = getintToken;
        this.leftParen = leftParen;
        this.rightParen = rightParen;
        this.semicn = semicn;
    }

    // Stmt → LVal '=' Exp ';'
    public StmtNode(LValNode lValNode, Token assign, ExpNode expNode, Token semicn) {
        this.lValNode = lValNode;
        this.assign = assign;
        this.expNode = expNode;
        this.semicn = semicn;
    }

    public Token getReturnToken() {
        return returnToken;
    }

    public ExpNode getExpNode() {
        return expNode;
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
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (blockNode != null) {
            sb.append(blockNode);
        } else if (ifToken != null) {
            sb.append(ifToken);
            sb.append(leftParen.toString());
            sb.append(condNode.toString());
            sb.append(rightParen.toString());
            sb.append(stmtNode1.toString());
            if (elseToken != null) {
                sb.append(elseToken);
                sb.append(stmtNode2.toString());
            }
        } else if (forToken != null) {
            sb.append(forToken);
            sb.append(leftParen.toString());
            if (forStmtNode1 != null) {
                sb.append(forStmtNode1);
            }
            sb.append(semicn1.toString());
            if (condNode != null) {
                sb.append(condNode);
            }
            sb.append(semicn2.toString());
            if (forStmtNode2 != null) {
                sb.append(forStmtNode2);
            }
            sb.append(rightParen.toString());
            sb.append(stmtNode.toString());
        } else if (breakToken != null) {
            sb.append(breakToken);
            sb.append(semicn.toString());
        } else if (continueToken != null) {
            sb.append(continueToken);
            sb.append(semicn.toString());
        } else if (returnToken != null) {
            sb.append(returnToken);
            if (expNode != null) {
                sb.append(expNode);
            }
            sb.append(semicn.toString());
        } else if (printfToken != null) {
            sb.append(printfToken);
            sb.append(leftParen.toString());
            sb.append(formatString.toString());
            for (int i = 0; i < commas.size(); i++) {
                sb.append(commas.get(i).toString());
                sb.append(expNodes.get(i).toString());
            }
            sb.append(rightParen.toString());
            sb.append(semicn.toString());
        } else if (lValNode != null && expNode != null) {
            sb.append(lValNode);
            sb.append(assign.toString());
            sb.append(expNode.toString());
            sb.append(semicn.toString());
        } else if (lValNode != null && getintToken != null) {
            sb.append(lValNode);
            sb.append(assign.toString());
            sb.append(getintToken.toString());
            sb.append(leftParen.toString());
            sb.append(rightParen.toString());
            sb.append(semicn.toString());
        } else if (expNode != null) {
            sb.append(expNode);
            sb.append(semicn.toString());
        } else if (semicn != null) {
            sb.append(semicn);
        }
        sb.append("<Stmt>\n");
        return sb.toString();
    }
}
