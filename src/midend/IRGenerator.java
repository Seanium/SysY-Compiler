package midend;

import frontend.node.*;
import frontend.token.Token;
import frontend.token.TokenType;
import midend.ir.*;
import midend.ir.inst.*;
import midend.ir.symbol.IRSymbol;
import midend.ir.symbol.IRSymbolManager;
import midend.ir.type.IntegerType;
import midend.ir.type.PointerType;
import midend.ir.type.Type;
import midend.ir.type.VoidType;

import java.util.ArrayList;

public class IRGenerator {
    private static IRGenerator instance;

    public static IRGenerator getInstance() {
        if (instance == null) {
            instance = new IRGenerator();
        }
        return instance;
    }

    private final IRBuilder irBuilder;
    private final IRSymbolManager irSymbolManager;

    private IRGenerator() {
        this.irBuilder = IRBuilder.getInstance();
        this.irSymbolManager = IRSymbolManager.getInstance();
    }

    // 1.编译单元 CompUnit → {Decl} {FuncDef} MainFuncDef // 1.是否存在Decl 2.是否存在FuncDef
    public void visitCompUnitNode(CompUnitNode compUnitNode) {
        irSymbolManager.enterBlock();       // 进入块级作用域
        irSymbolManager.setGlobal(true);    // 全局变量声明开始

        // 创建库函数声明 i32 @getint()
        Function getintFunc = new Function("getint", IntegerType.i32);
        getintFunc.setIsLib();
        // 添加函数到符号表
        irSymbolManager.addSymbol("getint", new IRSymbol(getintFunc, null));
        // 添加函数到module
        irBuilder.addFunctionToModule(getintFunc);

        // 创建库函数声明 void @putint(i32)
        ArrayList<Param> putintParams = new ArrayList<>();
        putintParams.add(new Param(IntegerType.i32, ""));
        Function putintFunc = new Function("putint", VoidType.voidType);
        putintFunc.setParams(putintParams);
        putintFunc.setIsLib();
        // 添加函数到符号表
        irSymbolManager.addSymbol("putint", new IRSymbol(putintFunc, null));
        // 添加函数到module
        irBuilder.addFunctionToModule(putintFunc);

        // 创建库函数声明 void @putch(i32)
        ArrayList<Param> putchParams = new ArrayList<>();
        putchParams.add(new Param(IntegerType.i32, ""));
        Function putchFunc = new Function("putch", VoidType.voidType);
        putchFunc.setParams(putchParams);
        putchFunc.setIsLib();
        // 添加函数到符号表
        irSymbolManager.addSymbol("putch", new IRSymbol(putchFunc, null));
        // 添加函数到module
        irBuilder.addFunctionToModule(putchFunc);

        // 创建库函数声明 void @putstr(i8*)
        ArrayList<Param> putstrParams = new ArrayList<>();
        putstrParams.add(new Param(new PointerType(IntegerType.i8), ""));
        Function putstrFunc = new Function("putstr", VoidType.voidType);
        putstrFunc.setParams(putstrParams);
        putstrFunc.setIsLib();
        // 添加函数到符号表
        irSymbolManager.addSymbol("putstr", new IRSymbol(putstrFunc, null));
        // 添加函数到module
        irBuilder.addFunctionToModule(putstrFunc);

        for (DeclNode declNode : compUnitNode.getDeclNodes()) {
            visitDeclNode(declNode);
        }
        irSymbolManager.setGlobal(false);   // 全局变量声明结束
        for (FuncDefNode funcDefNode : compUnitNode.getFuncDefNodes()) {
            visitFuncDefNode(funcDefNode);
        }
        visitMainFuncDefNode(compUnitNode.getMainFuncDefNode());
        irSymbolManager.leaveBlock();       // 离开块级作用域
    }


    // 2.声明 Decl → ConstDecl | VarDecl // 覆盖两种声明
    private void visitDeclNode(DeclNode declNode) {
        if (declNode.getConstDeclNode() != null) {  // ConstDecl
            visitConstDeclNode(declNode.getConstDeclNode());
        } else {    // VarDecl
            visitVarDeclNode(declNode.getVarDeclNode());
        }
    }

    // 3.常量声明 ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';' // 1.花括号内重复0次 2.花括号内重复多次
    private void visitConstDeclNode(ConstDeclNode constDeclNode) {
        for (ConstDefNode constDefNode : constDeclNode.getConstDefNodes()) {
            visitConstDefNode(constDefNode);
        }
    }

    // 5.常数定义 ConstDef → Ident { '[' ConstExp ']' } '=' ConstInitVal // 包含普通变量、一维数组、二维数组共三种情况
    private void visitConstDefNode(ConstDefNode constDefNode) {
        String ident = constDefNode.getIdent().getValue();
        if (irSymbolManager.isGlobal()) {   // 如果是全局常量
            if (constDefNode.getConstExpNodes().isEmpty()) {    // 如果不是数组
                // 创建全局常量
                int val = constDefNode.getConstInitValNode().getConstExpNode().getAddExpNode().calVal();   // 计算等号右侧初值
                Constant initValue = new Constant(IntegerType.i32, val);
                GlobalVar globalVar = new GlobalVar(IntegerType.i32, ident, initValue, true);
                // 添加全局常量到模块
                irBuilder.addGlobalVar(globalVar);
                // 添加全局常量到符号表
                irSymbolManager.addSymbol(ident, new IRSymbol(globalVar, initValue));
            } else {    // 如果是数组
                //todo
            }
        } else {    // 如果是局部常量
            if (constDefNode.getConstExpNodes().isEmpty()) {    // 如果不是数组
                // 创建alloca指令
                AllocaInst allocaInst = new AllocaInst(irBuilder.genLocalVarName(), IntegerType.i32);
                // 添加指令到当前基本块
                irBuilder.addInstToCurBasicBlock(allocaInst);
                // 创建store指令
                int val = constDefNode.getConstInitValNode().getConstExpNode().getAddExpNode().calVal();   // 计算等号右侧初值
                Constant initValue = new Constant(IntegerType.i32, val);
                StoreInst storeInst = new StoreInst(initValue, allocaInst);
                // 添加指令到当前基本块
                irBuilder.addInstToCurBasicBlock(storeInst);
                // 添加局部常量到符号表
                irSymbolManager.addSymbol(ident, new IRSymbol(allocaInst, initValue));
            } else {    // 如果是数组

            }
        }
    }


    // 7.变量声明 VarDecl → BType VarDef { ',' VarDef } ';' // 1.花括号内重复0次 2.花括号内重复多次
    private void visitVarDeclNode(VarDeclNode varDeclNode) {
        for (VarDefNode varDefNode : varDeclNode.getVarDefNodes()) {
            visitVarDefNode(varDefNode);
        }
    }

    // 8.变量定义 VarDef → Ident { '[' ConstExp ']' } // 包含普通变量、一维数组、二维数组定义
    // | Ident { '[' ConstExp ']' } '=' InitVal
    private void visitVarDefNode(VarDefNode varDefNode) {
        String ident = varDefNode.getIdent().getValue();
        if (irSymbolManager.isGlobal()) {   // 如果是全局变量
            if (varDefNode.getConstExpNodes().isEmpty()) {  // 如果不是数组
                // 创建全局变量
                Constant initValue;
                if (varDefNode.getInitValNode() == null) {  // 如果没有初值
                    initValue = new Constant(IntegerType.i32, 0);   // 文法规定中，初始值应为未定义，此处设为0
                } else {    // 如果有初值
                    int val = varDefNode.getInitValNode().getExpNode().getAddExpNode().calVal();    // 计算等号右侧初值
                    initValue = new Constant(IntegerType.i32, val);
                }
                GlobalVar globalVar = new GlobalVar(IntegerType.i32, ident, initValue, false);
                // 添加全局变量到模块
                irBuilder.addGlobalVar(globalVar);
                // 添加全局变量到符号表
                irSymbolManager.addSymbol(ident, new IRSymbol(globalVar, initValue));
            } else {    // 如果是数组
                //todo
            }
        } else {  // 如果局部变量
            if (varDefNode.getConstExpNodes().isEmpty()) {  // 如果不是数组
                if (varDefNode.getInitValNode() == null) {  // 如果没有初值 只创建alloca指令, 不创建store指令
                    // 创建alloca指令
                    AllocaInst allocaInst = new AllocaInst(irBuilder.genLocalVarName(), IntegerType.i32);
                    // 添加指令到当前基本块
                    irBuilder.addInstToCurBasicBlock(allocaInst);
                    // 添加局部变量到符号表(未初始化的局部变量，存储alloca指令)
                    irSymbolManager.addSymbol(ident, new IRSymbol(allocaInst, null));
                } else {    // 如果有初值
                    // 创建alloca指令
                    AllocaInst allocaInst = new AllocaInst(irBuilder.genLocalVarName(), IntegerType.i32);
                    // 添加指令到当前基本块
                    irBuilder.addInstToCurBasicBlock(allocaInst);
                    // 解析初值
                    Value initValue = visitInitValNode(varDefNode.getInitValNode());
                    // 创建store指令
                    StoreInst storeInst = new StoreInst(initValue, allocaInst);
                    // 添加指令到当前基本块
                    irBuilder.addInstToCurBasicBlock(storeInst);
                    // 添加局部变量到符号表
                    irSymbolManager.addSymbol(ident, new IRSymbol(allocaInst, initValue));
                }
            } else {    // 如果是数组
                //todo
            }
        }
    }

    // 9.变量初值 InitVal → Exp | '{' [ InitVal { ',' InitVal } ] '}'// 1.表达式初值 2.一维数组初值 3.二维数组初值
    private Value visitInitValNode(InitValNode initValNode) {
        if (initValNode.getExpNode() != null) { // 如果是表达式初值
            return visitExpNode(initValNode.getExpNode());
        } else {
            //todo
            return null;
        }
    }

    // 10.函数定义 FuncDef → FuncType Ident '(' [FuncFParams] ')' Block // 1.无形参 2.有形参
    // FIRST(FuncFParams ) = FIRST(FuncFParam) = {‘int’}
    private void visitFuncDefNode(FuncDefNode funcDefNode) {
        // 创建函数
        String ident = funcDefNode.getIdent().getValue();
        Type type = funcDefNode.getFuncTypeNode().isVoid() ? VoidType.voidType : IntegerType.i32;
        Function func = new Function(ident, type);
        // 添加函数到符号表
        irSymbolManager.addSymbol(ident, new IRSymbol(func, null));
        // 添加函数到module
        irBuilder.addFunctionToModule(func);
        // 设置curFunc
        irBuilder.setCurFunction(func);
        // 进入函数作用域
        irSymbolManager.enterFunction();

        // 向函数IR补充形参信息(形参位于函数内层作用域)
        func.setParams(funcDefNode.getIRParams());

        // 创建基本块
        BasicBlock basicBlock = new BasicBlock(irBuilder.genBasicBlockLabel(), func);
        // 添加基本块到函数
        irBuilder.addBasicBlockToCurFunction(basicBlock);
        // 设置curBasicBlock
        irBuilder.setCurBasicBlock(basicBlock);

        // 解析形参
        visitFuncFParamsNode(funcDefNode.getFuncFParamsNode());

        // 解析函数体
        visitBlockNode(funcDefNode.getBlockNode());
    }

    // 11.主函数定义 MainFuncDef → 'int' 'main' '(' ')' Block // 存在main函数
    private void visitMainFuncDefNode(MainFuncDefNode mainFuncDefNode) {
        // 创建函数
        Function mainFunc = new Function("main", IntegerType.i32);
        // 添加函数到符号表
        irSymbolManager.addSymbol("main", new IRSymbol(mainFunc, null));
        // 添加函数到module
        irBuilder.addFunctionToModule(mainFunc);
        // 设置curFunc
        irBuilder.setCurFunction(mainFunc);

        // 创建基本块    // todo 是否应该在visitBlockNode中处理基本块相关部分
        BasicBlock basicBlock = new BasicBlock(irBuilder.genBasicBlockLabel(), mainFunc);
        // 添加基本块到函数
        irBuilder.addBasicBlockToCurFunction(basicBlock);
        //设置curBasicBlock
        irBuilder.setCurBasicBlock(basicBlock);

        // 解析函数体
        visitBlockNode(mainFuncDefNode.getBlockNode());
    }

    // 13.函数形参表 FuncFParams → FuncFParam { ',' FuncFParam } // 1.花括号内重复0次 2.花括号内重复多次
    private void visitFuncFParamsNode(FuncFParamsNode funcFParamsNode) {
        if (funcFParamsNode == null) {  // 如果无形参
            return;
        }
        // 如果有形参
        for (FuncFParamNode funcFParamNode : funcFParamsNode.getFuncFParamNodes()) {
            visitFuncFParamNode(funcFParamNode);
        }
    }

    // 14.函数形参 FuncFParam → BType Ident ['[' ']' { '[' ConstExp ']' }] // 1.普通变量2.一维数组变量 3.二维数组变量
    private void visitFuncFParamNode(FuncFParamNode funcFParamNode) {
        String ident = funcFParamNode.getIdent().getValue();

        // todo 考虑数组 计算形参实际的维度

        // 如果是0维
        // 创建alloca指令
        AllocaInst allocaInst = new AllocaInst(irBuilder.genLocalVarName(), IntegerType.i32);
        // 添加指令到当前基本块
        irBuilder.addInstToCurBasicBlock(allocaInst);
        // 创建store指令
        StoreInst storeInst = new StoreInst(funcFParamNode.toIRParam(), allocaInst);
        // 添加指令到当前基本块
        irBuilder.addInstToCurBasicBlock(storeInst);
        // 添加局部变量（此处为形参）到符号表
        irSymbolManager.addSymbol(ident, new IRSymbol(allocaInst, null));

        // 如果是>0维
        // todo
    }

    // 15.语句块 Block → '{' { BlockItem } '}' // 1.花括号内重复0次 2.花括号内重复多次
    private void visitBlockNode(BlockNode blockNode) {
        irSymbolManager.enterBlock();   // 进入块级作用域
        for (BlockItemNode blockItemNode : blockNode.getBlockItemNodes()) {
            visitBlockItemNode(blockItemNode);
        }
        irSymbolManager.leaveBlock();   // 离开块级作用域
    }

    // 16.语句块项 BlockItem → Decl | Stmt // 覆盖两种语句块项
    private void visitBlockItemNode(BlockItemNode blockItemNode) {
        if (blockItemNode.getDeclNode() != null) {  // BlockItem → Decl
            visitDeclNode(blockItemNode.getDeclNode());
        } else {    // BlockItem → Stmt
            visitStmtNode(blockItemNode.getStmtNode());
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
    private void visitStmtNode(StmtNode stmtNode) {
        if (stmtNode.getBlockNode() != null) {  // Block
            // todo
        } else if (stmtNode.getIfToken() != null) { // 'if' '(' Cond ')' Stmt [ 'else' Stmt ] // 1.有else 2.无else
            // todo
        } else if (stmtNode.getForToken() != null) {    // 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt // 1. 无缺省 2. 缺省第一个 ForStmt 3. 缺省Cond 4. 缺省第二个ForStmt
            // todo
        } else if (stmtNode.getBreakToken() != null) {  // 'break' ';'
            // todo
        } else if (stmtNode.getContinueToken() != null) {   // 'continue' ';'
            // todo
        } else if (stmtNode.getReturnToken() != null) {    // 'return' [Exp] ';' // 1.有Exp 2.无Exp
            if (stmtNode.getExpNode() == null) {    // 'return';
                if (irBuilder.getCurFunction().getType() == IntegerType.i32) {  // 若函数类型为int, 则 return; 转换为 ret i32 0
                    // 创建指令
                    ReturnInst returnInst = new ReturnInst(new Constant(IntegerType.i32, 0));
                    // 添加指令到当前基本块
                    irBuilder.addInstToCurBasicBlock(returnInst);
                } else {    // 若函数类型为void，则 return; 转换为 ret void
                    // 创建指令
                    ReturnInst returnInst = new ReturnInst(null);
                    // 添加指令到当前基本块
                    irBuilder.addInstToCurBasicBlock(returnInst);
                }
            } else {    // 'return' [Exp];
                // 创建指令
                ReturnInst returnInst = new ReturnInst(visitExpNode(stmtNode.getExpNode()));
                // 添加指令到当前基本块
                irBuilder.addInstToCurBasicBlock(returnInst);
            }
        } else if (stmtNode.getPrintfToken() != null) { // 'printf''('FormatString{','Exp}')'';' // 1.有Exp 2.无Exp
            // todo 待修改为数组形式 putstr
            String formatString = stmtNode.getFormatString().getValue();
            formatString = formatString.replace("\\n", "\n");
            int formatExpIndex = 0;   // 当前是第几个格式占位符
            for (int i = 1; i < formatString.length() - 1; i++) {
                if (formatString.charAt(i) == '%') {
                    // 创建call指令
                    Function putintFunc = (Function) irSymbolManager.findSymbol("putint").getSymbol();
                    Value arg = visitExpNode(stmtNode.getExpNodes().get(formatExpIndex));
                    ArrayList<Value> args = new ArrayList<>();
                    args.add(arg);
                    CallInst callInst = new CallInst(null, putintFunc, args);
                    // 添加指令到当前基本块
                    irBuilder.addInstToCurBasicBlock(callInst);
                    // 移动下标
                    i++;    // 跳过"%d"
                    formatExpIndex++;
                } else {
                    // 创建call指令
                    Function putchFunc = (Function) irSymbolManager.findSymbol("putch").getSymbol();
                    Value arg = new Value(IntegerType.i32, String.valueOf(Integer.valueOf(formatString.charAt(i))));    //char -> int(ascii) -> string
                    ArrayList<Value> args = new ArrayList<>();
                    args.add(arg);
                    CallInst callInst = new CallInst(null, putchFunc, args);
                    // 添加指令到当前基本块
                    irBuilder.addInstToCurBasicBlock(callInst);
                }
            }
        } else if (stmtNode.getlValNode() != null && stmtNode.getExpNode() != null) {   // LVal '=' Exp ';' // 每种类型的语句都要覆盖
            if (stmtNode.getlValNode().getExpNodes().isEmpty()) {   // 如果左边不是数组元素
                // 解析表达式
                Value expValue = visitExpNode(stmtNode.getExpNode());
                // 创建store指令
                String ident = stmtNode.getlValNode().getIdent().getValue();
                StoreInst storeInst = new StoreInst(expValue, irSymbolManager.findSymbol(ident).getSymbol());
                // 添加指令到当前基本块
                irBuilder.addInstToCurBasicBlock(storeInst);
            } else {    // 如果左边是数组元素
                // todo
            }
        } else if (stmtNode.getlValNode() != null && stmtNode.getGetintToken() != null) {   // LVal '=' 'getint''('')'';'
            // todo
            if (stmtNode.getlValNode().getExpNodes().isEmpty()) {   // 如果左边不是数组元素
                // 创建call指令
                Function getintFunc = (Function) irSymbolManager.findSymbol("getint").getSymbol();
                CallInst callInst = new CallInst(irBuilder.genLocalVarName(), getintFunc, new ArrayList<>());
                // 添加指令到当前基本块
                irBuilder.addInstToCurBasicBlock(callInst);
                // 创建store指令
                String ident = stmtNode.getlValNode().getIdent().getValue();
                StoreInst storeInst = new StoreInst(callInst, irSymbolManager.findSymbol(ident).getSymbol());
                // 添加指令到当前基本块
                irBuilder.addInstToCurBasicBlock(storeInst);
            } else {    // 如果左边是数组元素
                // todo
            }
        } else if (stmtNode.getExpNode() != null) { // Exp ';'  //即 [Exp] ';'有 Exp 的情况
            visitExpNode(stmtNode.getExpNode());
        } else if (stmtNode.getSemicn() != null) {  // ';'      //即 [Exp] ';'无 Exp 的情况
            // 什么都不做
        }
    }

    // 19.表达式 Exp → AddExp 注：SysY 表达式是int 型表达式 // 存在即可
    private Value visitExpNode(ExpNode expNode) {
        return visitAddExpNode(expNode.getAddExpNode());
    }

    // 21.左值表达式 LVal → Ident {'[' Exp ']'} //1.普通变量 2.一维数组 3.二维数组
    private Value visitLValNode(LValNode lValNode) {
        if (lValNode.getExpNodes().isEmpty()) { // 如果不是数组
            String ident = lValNode.getIdent().getValue();
            Value pointer = irSymbolManager.findSymbol(ident).getSymbol();
            // 创建指令
            LoadInst loadInst = new LoadInst(irBuilder.genLocalVarName(), pointer);
            // 添加指令到基本块
            irBuilder.addInstToCurBasicBlock(loadInst);
            return loadInst;
        } else {    // 如果是数组
            // todo
            return null;
        }
    }

    // 22.基本表达式 PrimaryExp → '(' Exp ')' | LVal | Number // 三种情况均需覆盖
    private Value visitPrimaryExpNode(PrimaryExpNode primaryExpNode) {
        if (primaryExpNode.getExpNode() != null) {  // PrimaryExp → '(' Exp ')'
            return visitExpNode(primaryExpNode.getExpNode());
        } else if (primaryExpNode.getlValNode() != null) {  // PrimaryExp → LVal
            return visitLValNode(primaryExpNode.getlValNode());
        } else {    // PrimaryExp → Number
            return visitNumberNode(primaryExpNode.getNumberNode());
        }
    }

    // 23.数值 Number → IntConst // 存在即可
    private Constant visitNumberNode(NumberNode numberNode) {
        int value = Integer.parseInt(numberNode.getIntConst().getValue());
        return new Constant(IntegerType.i32, value);
    }

    // 24.一元表达式 UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')' // 3种情况均需覆盖,函数调用也需要覆盖FuncRParams的不同情况
    // | UnaryOp UnaryExp // 存在即可
    private Value visitUnaryExpNode(UnaryExpNode unaryExpNode) {
        if (unaryExpNode.getPrimaryExpNode() != null) { // UnaryExp → PrimaryExp    // 不生成指令，只返回值
            return visitPrimaryExpNode(unaryExpNode.getPrimaryExpNode());
        } else if (unaryExpNode.getUnaryOpNode() != null) { // UnaryExp → UnaryOp UnaryExp
            // 此处解析 UnaryOp
            // 25.单目运算符 UnaryOp → '+' | '−' | '!' //注：'!'仅出现在条件表达式中 // 三种均需覆盖
            UnaryOpNode unaryOpNode = unaryExpNode.getUnaryOpNode();
            if (unaryOpNode.getToken().getType() == TokenType.PLUS) {   // 如果是正号，不生成指令，返回加号后面的值
                return visitUnaryExpNode(unaryExpNode.getUnaryExpNode());
            } else if (unaryOpNode.getToken().getType() == TokenType.MINU) {    // 如果是负号，生成指令
                // 必须先访问子结点，再分配局部寄存器编号，确保编号顺序递增
                Value operand2 = visitUnaryExpNode(unaryExpNode.getUnaryExpNode());
                // 创建指令
                BinaryInst binaryInst = new BinaryInst(Opcode.sub, irBuilder.genLocalVarName(), new Constant(IntegerType.i32, 0), operand2);
                // 添加指令到当前基本块
                irBuilder.addInstToCurBasicBlock(binaryInst);
                return binaryInst;
            } else {    // 如果是'!'
                return null;    // todo
            }
        } else {    // UnaryExp → Ident '(' [FuncRParams] ')'
            String ident = unaryExpNode.getIdent().getValue();
            ArrayList<Value> args = visitFuncRParams(unaryExpNode.getFuncRParams());
            // 创建call指令
            Function targetFunc = (Function) irSymbolManager.findSymbol(ident).getSymbol();
            String callInstName = targetFunc.getType() == VoidType.voidType ? null : irBuilder.genLocalVarName();
            CallInst callInst = new CallInst(callInstName, targetFunc, args);
            // 添加指令到当前基本块
            irBuilder.addInstToCurBasicBlock(callInst);
            return callInst;
        }
    }

    // 26.函数实参表 FuncRParams → Exp { ',' Exp } // 1.花括号内重复0次 2.花括号内重复多次 3.Exp需要覆盖数组传参和部分数组传参
    ArrayList<Value> visitFuncRParams(FuncRParamsNode funcRParamsNode) {
        ArrayList<Value> args = new ArrayList<>();
        if (funcRParamsNode != null) {  // 若有实参，才遍历子结点
            for (ExpNode expNode : funcRParamsNode.getExpNodes()) {
                args.add(visitExpNode(expNode));
            }
        }
        return args;
    }

    // 27.乘除模表达式 MulExp → UnaryExp | MulExp ('*' | '/' | '%') UnaryExp // 1.UnaryExp 2.* 3./ 4.% 均需覆盖
    //【消除左递归】 MulExp → UnaryExp  {('*' | '/' | '%') UnaryExp}
    private Value visitMulExpNode(MulExpNode mulExpNode) {
        if (mulExpNode.getMulExpNode() == null) {   // MulExp → UnaryExp
            return visitUnaryExpNode(mulExpNode.getUnaryExpNode());
        } else {    // MulExp → MulExp ('*' | '/' | '%') UnaryExp
            // 访问子结点
            // 必须按此顺序，确保寄存器编号递增
            Value operand1 = visitMulExpNode(mulExpNode.getMulExpNode());
            Value operand2 = visitUnaryExpNode(mulExpNode.getUnaryExpNode());
            // 创建指令
            BinaryInst binaryInst;
            Token op = mulExpNode.getOp();
            if (op.getType() == TokenType.MULT) {   // '*'
                binaryInst = new BinaryInst(Opcode.mul, irBuilder.genLocalVarName(), operand1, operand2);
            } else if (op.getType() == TokenType.DIV) { // '/'
                binaryInst = new BinaryInst(Opcode.sdiv, irBuilder.genLocalVarName(), operand1, operand2);
            } else {    // '%'
                binaryInst = new BinaryInst(Opcode.srem, irBuilder.genLocalVarName(), operand1, operand2);
            }
            // 添加指令到基本块
            irBuilder.addInstToCurBasicBlock(binaryInst);
            return binaryInst;
        }
    }

    // 28.加减表达式 AddExp → MulExp | AddExp ('+' | '−') MulExp // 1.MulExp 2.+ 需覆盖 3.- 需覆盖
    // 【消除左递归】 AddExp → MulExp {('+' | '−') MulExp}
    private Value visitAddExpNode(AddExpNode addExpNode) {
        if (addExpNode.getAddExpNode() == null) {   // AddExp → MulExp
            return visitMulExpNode(addExpNode.getMulExpNode());
        } else {    // AddExp → AddExp ('+' | '−') MulExp
            // 访问子结点
            // 必须按此顺序，确保寄存器编号递增
            Value operand1 = visitAddExpNode(addExpNode.getAddExpNode());
            Value operand2 = visitMulExpNode(addExpNode.getMulExpNode());
            // 创建指令
            BinaryInst binaryInst;
            Token op = addExpNode.getOp();
            if (op.getType() == TokenType.PLUS) {   // '+'
                binaryInst = new BinaryInst(Opcode.add, irBuilder.genLocalVarName(), operand1, operand2);
            } else {    // '-'
                binaryInst = new BinaryInst(Opcode.sub, irBuilder.genLocalVarName(), operand1, operand2);
            }
            // 添加指令到基本块
            irBuilder.addInstToCurBasicBlock(binaryInst);
            return binaryInst;
        }
    }
}
