package midend;

import frontend.node.*;
import frontend.token.Token;
import frontend.token.TokenType;
import midend.ir.*;
import midend.ir.inst.*;
import midend.ir.symbol.IRSymbol;
import midend.ir.symbol.IRSymbolManager;
import midend.ir.type.*;

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
        Function putintFunc = new Function("putint", VoidType.voidType);
        putintFunc.addParam(new Param(IntegerType.i32, ""));
        putintFunc.setIsLib();
        // 添加函数到符号表
        irSymbolManager.addSymbol("putint", new IRSymbol(putintFunc, null));
        // 添加函数到module
        irBuilder.addFunctionToModule(putintFunc);

        // 创建库函数声明 void @putch(i32)
        Function putchFunc = new Function("putch", VoidType.voidType);
        putchFunc.addParam(new Param(IntegerType.i32, ""));
        putchFunc.setIsLib();
        // 添加函数到符号表
        irSymbolManager.addSymbol("putch", new IRSymbol(putchFunc, null));
        // 添加函数到module
        irBuilder.addFunctionToModule(putchFunc);

        // 创建库函数声明 void @putstr(i8*)
        Function putstrFunc = new Function("putstr", VoidType.voidType);
        putstrFunc.addParam(new Param(new PointerType(IntegerType.i8), ""));
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

    // 4.基本类型 BType → 'int' // 存在即可

    // 5.常数定义 ConstDef → Ident { '[' ConstExp ']' } '=' ConstInitVal // 包含普通变量、一维数组、二维数组共三种情况
    private void visitConstDefNode(ConstDefNode constDefNode) {
        String ident = constDefNode.getIdent().getValue();
        if (irSymbolManager.isGlobal()) {   // 如果是全局常量
            if (constDefNode.getConstExpNodes().isEmpty()) {    // 如果不是数组
                // 创建全局常量
                int val = visitConstExpNode(constDefNode.getConstInitValNode().getConstExpNode());  // 计算等号右侧初值
                Constant initValue = new Constant(IntegerType.i32, val);
                GlobalVar globalVar = new GlobalVar(IntegerType.i32, ident, initValue, true);
                // 添加全局常量到模块
                irBuilder.addGlobalVar(globalVar);
                // 添加全局常量到符号表
                irSymbolManager.addSymbol(ident, new IRSymbol(globalVar, initValue));
            } else {    // 如果是数组
                // 解析数组维度
                ArrayList<Integer> dims = new ArrayList<>();
                for (ConstExpNode constExpNode : constDefNode.getConstExpNodes()) {
                    int dim = visitConstExpNode(constExpNode);
                    dims.add(dim);
                }
                int len = irBuilder.calLen(dims);
                // 创建数组初值
                ArrayInitValue arrayInitValue = new ArrayInitValue(IntegerType.i32, new ArrayList<>());
                // 解析数组初值
                if (constDefNode.getConstInitValNode().getConstInitValNodes().isEmpty()) {  // 如果是 const int a[2][3] = {}; 这种大括号内为空的形式，需要给初值0
                    arrayInitValue.addZeros(len);
                } else {    // 如果大括号内不为空
                    irBuilder.setCurArrayInitValue(arrayInitValue);
                    visitConstInitValNode(constDefNode.getConstInitValNode());
                }
                // 创建全局数组常量
                GlobalArray globalArray = new GlobalArray(ident, dims, len, arrayInitValue, true);
                // 添加全局数组常量到模块
                irBuilder.addGlobalArray(globalArray);
                // 添加全局数组常量到符号表
                irSymbolManager.addSymbol(ident, new IRSymbol(globalArray, arrayInitValue));
            }
        } else {    // 如果是局部常量
            if (constDefNode.getConstExpNodes().isEmpty()) {    // 如果不是数组
                // 创建alloca指令
                AllocaInst allocaInst = new AllocaInst(irBuilder.genLocalVarName(), IntegerType.i32);
                // 添加指令到当前基本块
                irBuilder.addInstToCurBasicBlock(allocaInst);
                // 创建store指令
                int val = visitConstExpNode(constDefNode.getConstInitValNode().getConstExpNode());  // 计算等号右侧初值
                Constant initValue = new Constant(IntegerType.i32, val);
                StoreInst storeInst = new StoreInst(initValue, allocaInst);
                // 添加指令到当前基本块
                irBuilder.addInstToCurBasicBlock(storeInst);
                // 添加局部常量到符号表
                irSymbolManager.addSymbol(ident, new IRSymbol(allocaInst, initValue));
            } else {    // 如果是数组
                // 解析数组维度
                ArrayList<Integer> dims = new ArrayList<>();
                for (ConstExpNode constExpNode : constDefNode.getConstExpNodes()) {
                    int dim = visitConstExpNode(constExpNode);
                    dims.add(dim);
                }
                int len = irBuilder.calLen(dims);
                // 创建局部数组常量
                LocalArray localArray = new LocalArray(dims, len, true);
                // 创建alloca指令
                String name = irBuilder.genLocalVarName();
                localArray.setName(name);   // 设置局部数组的右值名为：数组的alloc语句中，赋值号左侧的寄存器名。
                AllocaInst allocaInst = new AllocaInst(name, localArray.getType());
                // 添加指令到当前基本块
                irBuilder.addInstToCurBasicBlock(allocaInst);
                // 创建数组初值
                ArrayInitValue arrayInitValue = new ArrayInitValue(IntegerType.i32, new ArrayList<>());
                // 解析数组初值
                if (constDefNode.getConstInitValNode().getConstInitValNodes().isEmpty()) {    // 如果是 const int a[2][3] = {}; 这种大括号内为空的形式，需要给初值0
                    arrayInitValue.addZeros(len);
                } else {    // 如果大括号内不为空
                    irBuilder.setCurArrayInitValue(arrayInitValue);
                    visitConstInitValNode(constDefNode.getConstInitValNode());
                }
                // 为每个元素创建GEP和store指令
                int offset = 0;
                for (Constant constant : arrayInitValue.getConstants()) {
                    // 创建GEP指令
                    GEPInst gepInst = new GEPInst(new PointerType(IntegerType.i32), irBuilder.genLocalVarName(), allocaInst, new Constant(IntegerType.i32, offset));
                    // 添加指令到当前基本块
                    irBuilder.addInstToCurBasicBlock(gepInst);
                    // 创建store指令
                    StoreInst storeInst = new StoreInst(constant, gepInst);
                    // 添加指令到当前基本块
                    irBuilder.addInstToCurBasicBlock(storeInst);
                    // 更新偏移量
                    offset++;
                }
                // 添加局部数组常量到符号表
                irSymbolManager.addSymbol(ident, new IRSymbol(allocaInst, arrayInitValue));
            }
        }
    }

    // 6.常量初值 ConstInitVal → ConstExp
    // | '{' [ ConstInitVal { ',' ConstInitVal } ] '}' // 1.常表达式初值 2.一维数组初值 3.二维数组初值

    /**
     * 解析全局常量数组初值，使用前请先设置curArrayInitValue，用于递归添加元素。
     */
    private void visitConstInitValNode(ConstInitValNode constInitValNode) {
        if (constInitValNode.getConstExpNode() != null) {   // ConstInitVal → ConstExp
            // 添加一个i32初值
            irBuilder.getCurArrayInitValue().addi32(visitConstExpNode(constInitValNode.getConstExpNode()));
        } else { // ConstInitVal → '{' [ ConstInitVal { ',' ConstInitVal } ] '}'
            // 递归解析
            for (ConstInitValNode constInitValNode1 : constInitValNode.getConstInitValNodes()) {
                visitConstInitValNode(constInitValNode1);
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
                if (varDefNode.getInitValNode() == null) {  // 如果没有初值 // 未显式初始化的全局变量，其（元素）值均被初始化为 0
                    initValue = new Constant(IntegerType.i32, 0);
                } else {    // 如果有初值
                    // 全局变量的初值一定是可计算的
                    int val = varDefNode.getInitValNode().getExpNode().getAddExpNode().calVal();    // 计算等号右侧初值
                    initValue = new Constant(IntegerType.i32, val);
                }
                GlobalVar globalVar = new GlobalVar(IntegerType.i32, ident, initValue, false);
                // 添加全局变量到模块
                irBuilder.addGlobalVar(globalVar);
                // 添加全局变量到符号表
                irSymbolManager.addSymbol(ident, new IRSymbol(globalVar, initValue));
            } else {    // 如果是数组
                // 解析数组维度
                ArrayList<Integer> dims = new ArrayList<>();
                for (ConstExpNode constExpNode : varDefNode.getConstExpNodes()) {
                    int dim = visitConstExpNode(constExpNode);
                    dims.add(dim);
                }
                int len = irBuilder.calLen(dims);
                if (varDefNode.getInitValNode() == null) {  // 如果没有初值 // 未显式初始化的全局变量，其（元素）值均被初始化为 0
                    // 创建数组初值
                    ArrayInitValue arrayInitValue = new ArrayInitValue(IntegerType.i32, new ArrayList<>());
                    arrayInitValue.addZeros(len);   // 需要给初值0
                    // 创建全局数组变量
                    GlobalArray globalArray = new GlobalArray(ident, dims, len, arrayInitValue, false);
                    // 添加全局数组变量到模块
                    irBuilder.addGlobalArray(globalArray);
                    // 添加全局数组变量到符号表
                    irSymbolManager.addSymbol(ident, new IRSymbol(globalArray, arrayInitValue));
                } else {    // 如果有初值
                    // 创建数组初值
                    ArrayInitValue arrayInitValue = new ArrayInitValue(IntegerType.i32, new ArrayList<>());
                    if (varDefNode.getInitValNode().getInitValNodes().isEmpty()) {  // 如果是 int a[2][3] = {}; 这种大括号内为空的形式，需要给初值0
                        arrayInitValue.addZeros(len);
                    } else {    // 如果大括号内不为空
                        irBuilder.setCurArrayInitValue(arrayInitValue);
                        visitInitValNodeForGlobal(varDefNode.getInitValNode());
                    }
                    // 创建全局数组变量
                    GlobalArray globalArray = new GlobalArray(ident, dims, len, arrayInitValue, false);
                    // 添加全局数组变量到模块
                    irBuilder.addGlobalArray(globalArray);
                    // 添加全局数组变量到符号表
                    irSymbolManager.addSymbol(ident, new IRSymbol(globalArray, arrayInitValue));
                }
            }
        } else {  // 如果是局部变量
            if (varDefNode.getConstExpNodes().isEmpty()) {  // 如果不是数组
                if (varDefNode.getInitValNode() == null) {  // 如果没有初值 只创建alloca指令, 不创建store指令 // 未显式初始化的局部变量，其值是不确定的
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
                    Value initValue = visitExpNode(varDefNode.getInitValNode().getExpNode());
                    // 创建store指令
                    StoreInst storeInst = new StoreInst(initValue, allocaInst);
                    // 添加指令到当前基本块
                    irBuilder.addInstToCurBasicBlock(storeInst);
                    // 添加局部变量到符号表
                    irSymbolManager.addSymbol(ident, new IRSymbol(allocaInst, initValue));
                }
            } else {    // 如果是数组
                // 解析数组维度
                ArrayList<Integer> dims = new ArrayList<>();
                for (ConstExpNode constExpNode : varDefNode.getConstExpNodes()) {
                    int dim = visitConstExpNode(constExpNode);
                    dims.add(dim);
                }
                int len = irBuilder.calLen(dims);
                if (varDefNode.getInitValNode() == null) {  // 如果没有初值 // 未显式初始化的局部变量，其值是不确定的
                    // 创建局部数组变量
                    LocalArray localArray = new LocalArray(dims, len, false);
                    // 创建alloc指令
                    String name = irBuilder.genLocalVarName();
                    localArray.setName(name);   // 设置局部数组的右值名为：数组的alloc语句中，赋值号左侧的寄存器名。
                    AllocaInst allocaInst = new AllocaInst(name, localArray.getType());
                    // 添加指令到当前基本块
                    irBuilder.addInstToCurBasicBlock(allocaInst);
                    // 添加局部数组变量到符号表
                    irSymbolManager.addSymbol(ident, new IRSymbol(allocaInst, null));
                } else {    // 如果有初值
                    // 创建局部数组变量
                    LocalArray localArray = new LocalArray(dims, len, false);
                    // 创建alloca指令
                    String name = irBuilder.genLocalVarName();
                    localArray.setName(name);   // 设置局部数组的右值名为：数组的alloc语句中，赋值号左侧的寄存器名。
                    AllocaInst allocaInst = new AllocaInst(name, localArray.getType());
                    // 添加指令到当前基本块
                    irBuilder.addInstToCurBasicBlock(allocaInst);
                    // 解析数组初值
                    if (varDefNode.getInitValNode().getInitValNodes().isEmpty()) {  // 如果是 int a[2][3] = {}; // 未显式初始化的局部变量，其值是不确定的
                        // 添加局部数组变量到符号表
                        irSymbolManager.addSymbol(ident, new IRSymbol(allocaInst, null));
                    } else {    // 如果大括号内不为空
                        // 解析局部数组变量初值。依次创建GEP，解析初值，创建store指令。
                        irBuilder.setAllocaInst(allocaInst);
                        irBuilder.setOffset(0);
                        visitInitValNodeForLocal(varDefNode.getInitValNode());
                        // 添加局部数组变量到符号表
                        irSymbolManager.addSymbol(ident, new IRSymbol(allocaInst, null));
                    }
                }
            }
        }
    }

    // 9.变量初值 InitVal → Exp | '{' [ InitVal { ',' InitVal } ] '}'// 1.表达式初值 2.一维数组初值 3.二维数组初值

    /**
     * 解析全局数组变量初值，使用前请先设置curArrayInitValue，用于递归添加元素。
     */
    private void visitInitValNodeForGlobal(InitValNode initValNode) {
        if (initValNode.getExpNode() != null) { // InitVal → Exp
            // 添加一个i32初值
            irBuilder.getCurArrayInitValue().addi32(initValNode.getExpNode().getAddExpNode().calVal()); // 全局变量的初值一定是可计算的
        } else {    // InitVal → '{' [ InitVal { ',' InitVal } ] '}'
            // 递归解析
            for (InitValNode initValNode1 : initValNode.getInitValNodes()) {
                visitInitValNodeForGlobal(initValNode1);
            }
        }
    }

    /**
     * 解析局部数组变量初值。依次创建GEP，解析初值，创建store指令。
     */
    private void visitInitValNodeForLocal(InitValNode initValNode) {
        if (initValNode.getExpNode() != null) { // InitVal → Exp
            // 创建GEP指令
            GEPInst gepInst = new GEPInst(new PointerType(IntegerType.i32), irBuilder.genLocalVarName(), irBuilder.getAllocaInst(), new Constant(IntegerType.i32, irBuilder.getOffset()));
            // 添加指令到当前基本块
            irBuilder.addInstToCurBasicBlock(gepInst);
            // 解析初值
            Value value = visitExpNode(initValNode.getExpNode());
            // 创建store指令
            StoreInst storeInst = new StoreInst(value, gepInst);
            // 添加指令到当前基本块
            irBuilder.addInstToCurBasicBlock(storeInst);
            // 更新偏移量
            irBuilder.setOffset(irBuilder.getOffset() + 1);
        } else {    // InitVal → '{' [ InitVal { ',' InitVal } ] '}'
            // 递归解析
            for (InitValNode initValNode1 : initValNode.getInitValNodes()) {
                visitInitValNodeForLocal(initValNode1);
            }
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

        // 创建基本块
        BasicBlock basicBlock = new BasicBlock(irBuilder.genBasicBlockLabel(), func);
        // 添加基本块到当前函数
        irBuilder.addBasicBlockToCurFunction(basicBlock);
        // 设置curBasicBlock
        irBuilder.setCurBasicBlock(basicBlock);

        // 解析形参
        visitFuncFParamsNode(funcDefNode.getFuncFParamsNode());

        // 解析函数体
        visitBlockNode(funcDefNode.getBlockNode());

        // 如果是void函数且最后没有return;，则补充return;
        irBuilder.checkReturn(func);
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

        // 创建基本块
        BasicBlock basicBlock = new BasicBlock(irBuilder.genBasicBlockLabel(), mainFunc);
        // 添加基本块到当前函数
        irBuilder.addBasicBlockToCurFunction(basicBlock);
        //设置curBasicBlock
        irBuilder.setCurBasicBlock(basicBlock);

        // 解析函数体
        visitBlockNode(mainFuncDefNode.getBlockNode());
    }

    // 12.函数类型 FuncType → 'void' | 'int' // 覆盖两种类型的函数

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
        if (funcFParamNode.getLeftBrackets().isEmpty()) {   // 如果形参没有中括号，则类型为i32
            // 创建 param
            Param param = new Param(IntegerType.i32, irBuilder.genLocalVarName());
            // 向函数IR添加param
            irBuilder.getCurFunction().addParam(param);
            // 创建alloca指令
            AllocaInst allocaInst = new AllocaInst(irBuilder.genLocalVarName(), IntegerType.i32);
            // 添加指令到当前基本块
            irBuilder.addInstToCurBasicBlock(allocaInst);
            // 创建store指令
            StoreInst storeInst = new StoreInst(param, allocaInst);
            // 添加指令到当前基本块
            irBuilder.addInstToCurBasicBlock(storeInst);
            // 添加形参到符号表
            irSymbolManager.addSymbol(ident, new IRSymbol(allocaInst, null));
        } else {    // 如果形参有中括号，则类型为 i32 的指针类型
            // 创建 param
            Param param = new Param(new PointerType(IntegerType.i32), irBuilder.genLocalVarName());
            // 设置形参维数列表
            ArrayList<Integer> dims = new ArrayList<>();
            dims.add(1);    // 空缺的首个维度补为1。
            for (ConstExpNode constExpNode : funcFParamNode.getConstExpNodes()) {
                dims.add(visitConstExpNode(constExpNode));
            }
            param.setDims(dims);
            // 向函数IR添加param
            irBuilder.getCurFunction().addParam(param);
            // 添加形参到符号表
            irSymbolManager.addSymbol(ident, new IRSymbol(param, null));
        }
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
            visitBlockNode(stmtNode.getBlockNode());
        } else if (stmtNode.getIfToken() != null) { // 'if' '(' Cond ')' Stmt [ 'else' Stmt ] // 1.有else 2.无else
            if (stmtNode.getElseToken() != null) {  // 如果有else
                // if cond(cond属于[preBlock]) [thenBlock] else [elseBlock] [followBlock]

                // 创建thenBlock
                BasicBlock thenBlock = new BasicBlock(irBuilder.genBasicBlockLabel(), irBuilder.getCurFunction());
                // 创建elseBlock
                BasicBlock elseBlock = new BasicBlock(irBuilder.genBasicBlockLabel(), irBuilder.getCurFunction());

                // 设置当前条件真目标基本块为thenBlock
                irBuilder.setCurTrueBlock(thenBlock);
                // 设置当前条件假目标基本块为elseBlock
                irBuilder.setCurFalseBlock(elseBlock);
                // 解析条件(满足短路求值)
                visitCondNode(stmtNode.getCondNode());

                // 创建followBlock(创建时机 决定编号顺序 希望followBlock的编号 大于 短路条件块的编号)
                BasicBlock followBlock = new BasicBlock(irBuilder.genBasicBlockLabel(), irBuilder.getCurFunction());

                // 添加thenBlock到当前函数(添加时机 决定输出顺序)
                irBuilder.addBasicBlockToCurFunction(thenBlock);
                // 解析thenBlock
                irBuilder.setCurBasicBlock(thenBlock);
                visitStmtNode(stmtNode.getStmtNode1());
                //最后无条件跳转到followBlock
                JumpInst jumpInst1 = new JumpInst(followBlock);
                // 添加指令到当前基本块(curBlock在上面解析thenBlock时更新, 此时可以正确嵌套)
                irBuilder.addInstToCurBasicBlock(jumpInst1);

                // 添加elseBlock到当前函数(添加时机 决定输出顺序)
                irBuilder.addBasicBlockToCurFunction(elseBlock);
                // 解析elseBlock
                irBuilder.setCurBasicBlock(elseBlock);
                visitStmtNode(stmtNode.getStmtNode2());
                //最后无条件跳转到followBlock
                JumpInst jumpInst2 = new JumpInst(followBlock);
                // 添加指令到当前基本块(curBlock在上面解析elseBlock时更新, 此时可以正确嵌套)
                irBuilder.addInstToCurBasicBlock(jumpInst2);

                // 添加followBlock到当前函数(添加时机 决定输出顺序)
                irBuilder.addBasicBlockToCurFunction(followBlock);
                // 设置curBasicBlock为followBlock
                irBuilder.setCurBasicBlock(followBlock);
            } else {    // 如果没有else
                // if cond(cond属于[preBlock]) [thenBlock] [followBlock]

                // 创建thenBlock
                BasicBlock thenBlock = new BasicBlock(irBuilder.genBasicBlockLabel(), irBuilder.getCurFunction());
                // 创建followBlock
                BasicBlock followBlock = new BasicBlock(irBuilder.genBasicBlockLabel(), irBuilder.getCurFunction());

                // 设置当前条件真目标基本块为thenBlock
                irBuilder.setCurTrueBlock(thenBlock);
                // 设置当前条件假目标基本块为followBlock
                irBuilder.setCurFalseBlock(followBlock);
                // 解析条件(满足短路求值)
                visitCondNode(stmtNode.getCondNode());

                // 添加thenBlock到当前函数(添加时机 决定输出顺序)
                irBuilder.addBasicBlockToCurFunction(thenBlock);
                // 解析thenBlock
                irBuilder.setCurBasicBlock(thenBlock);
                visitStmtNode(stmtNode.getStmtNode1());
                // 最后无条件跳转到followBlock
                JumpInst jumpInst1 = new JumpInst(followBlock);
                // 添加指令到当前基本块(curBlock在上面解析thenBlock时更新, 此时可以正确嵌套)
                irBuilder.addInstToCurBasicBlock(jumpInst1);

                // 添加followBlock到当前函数(添加时机 决定输出顺序)
                irBuilder.addBasicBlockToCurFunction(followBlock);
                // 设置curBasicBlock为followBlock
                irBuilder.setCurBasicBlock(followBlock);
            }
        } else if (stmtNode.getForToken() != null) {    // 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt // 1. 无缺省 2. 缺省第一个 ForStmt 3. 缺省Cond 4. 缺省第二个ForStmt
            /* 第一步 处理forStmt1 */
            // 若非空，解析forStmtNode1
            if (stmtNode.getForStmtNode1() != null) {
                visitForStmtNode(stmtNode.getForStmtNode1());
            }

            /* 第二步 处理cond */
            // 创建condBlock, stmtBlock, followBlock
            BasicBlock condBlock = new BasicBlock(irBuilder.genBasicBlockLabel(), irBuilder.getCurFunction());
            BasicBlock stmtBlock = new BasicBlock(irBuilder.genBasicBlockLabel(), irBuilder.getCurFunction());
            BasicBlock followBlock = new BasicBlock(irBuilder.genBasicBlockLabel(), irBuilder.getCurFunction());
            // 添加无条件跳转指令 无条件跳转到condBlock
            JumpInst jumpInst = new JumpInst(condBlock);
            // 添加指令到当前基本块
            irBuilder.addInstToCurBasicBlock(jumpInst);

            // 添加condBlock到当前函数
            irBuilder.addBasicBlockToCurFunction(condBlock);
            // 设置当前基本块为condBlock
            irBuilder.setCurBasicBlock(condBlock);
            // 解析cond
            if (stmtNode.getCondNode() == null) {   // 如果cond为空，相当于条件永真，无条件跳转到stmtBlock
                // 创建无条件跳转指令 跳转到stmtBlock
                JumpInst jumpInst1 = new JumpInst(stmtBlock);
                // 添加指令到当前基本块
                irBuilder.addInstToCurBasicBlock(jumpInst1);
            } else {
                // 设置当前条件真目标基本块为stmtBlock
                irBuilder.setCurTrueBlock(stmtBlock);
                // 设置当前条件假目标基本块为followBlock
                irBuilder.setCurFalseBlock(followBlock);
                // 解析条件(满足短路求值)
                visitCondNode(stmtNode.getCondNode());
            }

            /* 第三步 处理stmt */
            // 创建forStmt2Block
            BasicBlock forStmt2Block = new BasicBlock(irBuilder.genBasicBlockLabel(), irBuilder.getCurFunction());
            // 设置continue和break的跳转目标基本块
            irBuilder.setContinueTargetBlock(forStmt2Block);
            irBuilder.setBreakTargetBlock(followBlock);

            // 添加stmtBlock到当前函数
            irBuilder.addBasicBlockToCurFunction(stmtBlock);
            // 设置当前基本块为stmtBlock
            irBuilder.setCurBasicBlock(stmtBlock);
            // 解析stmtBlock
            visitStmtNode(stmtNode.getStmtNode());
            // 最后无条件跳转到forStmt2Block
            JumpInst jumpInst1 = new JumpInst(forStmt2Block);
            // 添加指令到当前基本块(curBlock在上面解析stmtBlock时更新, 此时可以正确嵌套)
            irBuilder.addInstToCurBasicBlock(jumpInst1);

            /* 第四步 处理forStmt2 */
            // 添加forStmt2Block到函数
            irBuilder.addBasicBlockToCurFunction(forStmt2Block);
            // 设置当前基本块为forStmt2Block
            irBuilder.setCurBasicBlock(forStmt2Block);
            // 若非空，解析forStmtNode2
            if (stmtNode.getForStmtNode2() != null) {
                visitForStmtNode(stmtNode.getForStmtNode2());
            }
            // 创建无条件跳转指令 跳转到condBlock
            JumpInst jumpInst2 = new JumpInst(condBlock);
            // condBlock是循环头, forStmt2Block是循环尾
            condBlock.setLoopHead(true);
            forStmt2Block.setLoopTail(true);
            // 添加指令到当前基本块
            irBuilder.addInstToCurBasicBlock(jumpInst2);

            /* 第五步 进入followBlock */
            // 添加followBlock到当前函数
            irBuilder.addBasicBlockToCurFunction(followBlock);
            // 设置当前基本块为followBlock
            irBuilder.setCurBasicBlock(followBlock);
        } else if (stmtNode.getBreakToken() != null) {  // 'break' ';'
            // 创建无条件跳转指令 跳转到curFollowBlock
            JumpInst jumpInst = new JumpInst(irBuilder.getBreakTargetBlock());
            // 添加指令到当前基本块
            irBuilder.addInstToCurBasicBlock(jumpInst);
        } else if (stmtNode.getContinueToken() != null) {   // 'continue' ';'
            // 创建无条件跳转指令 跳转到curForStmt2Block
            JumpInst jumpInst = new JumpInst(irBuilder.getContinueTargetBlock());
            // 添加指令到当前基本块
            irBuilder.addInstToCurBasicBlock(jumpInst);
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
            String formatString = stmtNode.getFormatString().getValue();
            formatString = formatString.replace("\\n", "\n");
            formatString = formatString.substring(1, formatString.length() - 1);    // 去除首尾的双引号
            // 分割成%d和非%d的字符串数组
            String[] split = formatString.split("(?<=%d)|(?=%d)");  // magic
            int formatExpIndex = 0;   // 当前是第几个格式占位符
            for (String s : split) {
                if (s.equals("%d")) {   // 如果是%d
                    // 创建call指令，调用putint(i32)
                    Function putintFunc = (Function) irSymbolManager.findSymbol("putint").getSymbol();
                    Value arg = visitExpNode(stmtNode.getExpNodes().get(formatExpIndex));
                    formatExpIndex++;
                    ArrayList<Value> args = new ArrayList<>();
                    args.add(arg);
                    CallInst callInst = new CallInst(null, putintFunc, args);
                    // 添加指令到当前基本块
                    irBuilder.addInstToCurBasicBlock(callInst);
                } else {    //如果是非%d，在i8Constant类型C的全局常量数组中存储其ascii码
                    s = s.concat("\0"); // 在后面补充\0  // 对于printf中的格式字符串，符号表中name为结尾为\0的字符串（保证了不会与ident冲突）。symbol是globalArray。
                    GlobalArray globalArray;
                    if (irSymbolManager.findSymbol(s) != null) {    // 如果符号表中已存在该字符串，则不创建数组
                        globalArray = (GlobalArray) irSymbolManager.findSymbol(s).getSymbol();
                    } else {    // 如果符号表中不存在，则创建数组
                        ArrayList<Integer> dims = new ArrayList<>();
                        dims.add(s.length());
                        // 创建元素为i8类型的全局常量数组
                        ArrayList<Constant> constants = new ArrayList<>();
                        for (int i = 0; i < s.length(); i++) {
                            int ascii = s.charAt(i);
                            constants.add(new Constant(IntegerType.i8, ascii));
                        }
                        ArrayInitValue arrayInitValue = new ArrayInitValue(IntegerType.i8, constants);
                        globalArray = new GlobalArray(irBuilder.genStrArrName(), dims, s.length(), arrayInitValue, true);   // 但是在globalArray的name为“.str编号”（前面的.保证了不会和ident重名）。不用字符串内容是因为\n\0的输出不合适。
                        // 添加到模块
                        irBuilder.addGlobalArray(globalArray);
                        // 添加到符号表
                        irSymbolManager.addSymbol(s, new IRSymbol(globalArray, arrayInitValue));
                    }
                    // 创建GEP指令
                    GEPInst gepInst = new GEPInst(new PointerType(IntegerType.i8), irBuilder.genLocalVarName(), globalArray, new Constant(IntegerType.i32, 0));
                    // 添加指令到当前基本块
                    irBuilder.addInstToCurBasicBlock(gepInst);
                    // 创建call指令，调用putstr(i8*)
                    Function putstrFunc = (Function) irSymbolManager.findSymbol("putstr").getSymbol();
                    ArrayList<Value> args = new ArrayList<>();
                    args.add(gepInst);
                    CallInst callInst = new CallInst(null, putstrFunc, args);
                    // 添加指令到当前基本块
                    irBuilder.addInstToCurBasicBlock(callInst);
                }
            }
        } else if (stmtNode.getlValNode() != null && stmtNode.getExpNode() != null) {   // LVal '=' Exp ';' // 每种类型的语句都要覆盖
            if (stmtNode.getlValNode().getExpNodes().isEmpty()) {   // 如果左边不是数组元素
                // 解析表达式
                Value expValue = visitExpNode(stmtNode.getExpNode());
                // 定位左值
                String ident = stmtNode.getlValNode().getIdent().getValue();
                Value symbol = irSymbolManager.findSymbol(ident).getSymbol();
                // 创建store指令
                StoreInst storeInst = new StoreInst(expValue, symbol);
                // 添加指令到当前基本块
                irBuilder.addInstToCurBasicBlock(storeInst);
            } else {    // 如果左边是数组元素
                // 解析表达式
                Value expValue = visitExpNode(stmtNode.getExpNode());
                // 定位左值
                Value pointer = visitLValNodeForLArr(stmtNode.getlValNode());
                // 创建store指令
                StoreInst storeInst = new StoreInst(expValue, pointer);
                // 添加指令到当前基本块
                irBuilder.addInstToCurBasicBlock(storeInst);
            }
        } else if (stmtNode.getlValNode() != null && stmtNode.getGetintToken() != null) {   // LVal '=' 'getint''('')'';'
            if (stmtNode.getlValNode().getExpNodes().isEmpty()) {   // 如果左边不是数组元素
                // 创建call指令
                Function getintFunc = (Function) irSymbolManager.findSymbol("getint").getSymbol();
                CallInst callInst = new CallInst(irBuilder.genLocalVarName(), getintFunc, new ArrayList<>());
                // 添加指令到当前基本块
                irBuilder.addInstToCurBasicBlock(callInst);
                // 定位左值
                String ident = stmtNode.getlValNode().getIdent().getValue();
                Value symbol = irSymbolManager.findSymbol(ident).getSymbol();
                // 创建store指令
                StoreInst storeInst = new StoreInst(callInst, symbol);
                // 添加指令到当前基本块
                irBuilder.addInstToCurBasicBlock(storeInst);
            } else {    // 如果左边是数组元素
                // 创建call指令
                Function getintFunc = (Function) irSymbolManager.findSymbol("getint").getSymbol();
                CallInst callInst = new CallInst(irBuilder.genLocalVarName(), getintFunc, new ArrayList<>());
                // 添加指令到当前基本块
                irBuilder.addInstToCurBasicBlock(callInst);
                // 定位左值
                Value pointer = visitLValNodeForLArr(stmtNode.getlValNode());
                // 创建store指令
                StoreInst storeInst = new StoreInst(callInst, pointer);
                // 添加指令到当前基本块
                irBuilder.addInstToCurBasicBlock(storeInst);
            }
        } else if (stmtNode.getExpNode() != null) { // Exp ';'  //即 [Exp] ';'有 Exp 的情况
            visitExpNode(stmtNode.getExpNode());
        } else if (stmtNode.getSemicn() != null) {  // ';'      //即 [Exp] ';'无 Exp 的情况
            // 什么都不做
        }
    }

    // 18.语句 ForStmt → LVal '=' Exp // 存在即可
    private void visitForStmtNode(ForStmtNode forStmtNode) {
        if (forStmtNode.getlValNode().getExpNodes().isEmpty()) {    // 如果左边不是数组元素
            // 解析表达式
            Value expValue = visitExpNode(forStmtNode.getExpNode());
            // 定位左值
            String ident = forStmtNode.getlValNode().getIdent().getValue();
            Value symbol = irSymbolManager.findSymbol(ident).getSymbol();
            // 创建store指令
            StoreInst storeInst = new StoreInst(expValue, symbol);
            // 添加指令到当前基本块
            irBuilder.addInstToCurBasicBlock(storeInst);
        } else {    // 如果左边是数组元素
            // 解析表达式
            Value expValue = visitExpNode(forStmtNode.getExpNode());
            // 定位左值
            Value pointer = visitLValNodeForLArr(forStmtNode.getlValNode());
            // 创建store指令
            StoreInst storeInst = new StoreInst(expValue, pointer);
            // 添加指令到当前基本块
            irBuilder.addInstToCurBasicBlock(storeInst);
        }
    }

    // 19.表达式 Exp → AddExp 注：SysY 表达式是int 型表达式 // 存在即可
    public Value visitExpNode(ExpNode expNode) {
        return visitAddExpNode(expNode.getAddExpNode());
    }

    // 20.条件表达式 Cond → LOrExp // 存在即可
    private void visitCondNode(CondNode condNode) {
        visitLOrExpNode(condNode.getlOrExpNode());
    }

    // 21.左值表达式 LVal → Ident {'[' Exp ']'} //1.普通变量 2.一维数组 3.二维数组

    /**
     * 解析lval作为右值的情况。
     * 比如 a[0] = b[0];
     * a[0]和b[0]同样是lval，但a[0]是左值，需要取地址(GEP)；而b[0]是右值，需要取值(load)。
     */
    private Value visitLValNodeForR(LValNode lValNode) {
        String ident = lValNode.getIdent().getValue();
        Value pointer = irSymbolManager.findSymbol(ident).getSymbol();
        if (pointer instanceof Param ||
                ((PointerType) pointer.getType()).getTargetType() instanceof ArrayType) { // 如果左值是数组形参，全局数组，局部数组
            // 获取数组维数列表
            ArrayList<Integer> dims;
            if (pointer instanceof Param) { // 如果左值是数组形参，维数信息在Param中取得
                dims = ((Param) pointer).getDims();
            } else {    // 如果左值是全局数组或局部数组，维数列表在指针的目标类型（数组类型）中取得
                ArrayType arrayType = (ArrayType) ((PointerType) pointer.getType()).getTargetType();
                dims = arrayType.getDims();
            }
            // 获取左值下标列表 比如左值 a[1][2] 的下标列表为 {1, 2}
            ArrayList<Value> indexes = new ArrayList<>();
            for (ExpNode expNode : lValNode.getExpNodes()) {
                indexes.add(visitExpNode(expNode));
            }
            // 计算偏移量
            Value offset = irBuilder.calOffset(indexes, dims);
            // 解析左值
            if (dims.size() == indexes.size()) {    // 如果下标数量等于维度数量，则返回值，即先gep再load
                // 创建GEP指令
                GEPInst gepInst = new GEPInst(new PointerType(IntegerType.i32), irBuilder.genLocalVarName(), pointer, offset);
                // 添加指令到当前基本块
                irBuilder.addInstToCurBasicBlock(gepInst);
                // 创建load指令
                LoadInst loadInst = new LoadInst(irBuilder.genLocalVarName(), gepInst);
                // 添加指令到当前基本块
                irBuilder.addInstToCurBasicBlock(loadInst);
                return loadInst;
            } else {    // 如果下标数量小于维度数量，则返回指针，即只gep
                // 创建GEP指令
                GEPInst gepInst = new GEPInst(new PointerType(IntegerType.i32), irBuilder.genLocalVarName(), pointer, offset);
                // 添加指令到当前基本块
                irBuilder.addInstToCurBasicBlock(gepInst);
                return gepInst;
            }
        } else {    // 如果左值是非数组
            // 创建指令
            LoadInst loadInst = new LoadInst(irBuilder.genLocalVarName(), pointer);
            // 添加指令到基本块
            irBuilder.addInstToCurBasicBlock(loadInst);
            return loadInst;
        }
    }

    /**
     * 解析lval作为左值，且是数组元素的情况。此时的lval一定定位到元素，即dims.size() == values.size()。
     * 比如 a[0] = b[0];
     * a[0]和b[0]同样是lval，但a[0]是左值，需要取地址(GEP)；而b[0]是右值，需要取值(load)。
     */
    private Value visitLValNodeForLArr(LValNode lValNode) {
        String ident = lValNode.getIdent().getValue();
        Value pointer = irSymbolManager.findSymbol(ident).getSymbol();
        // 获取数组维数列表
        ArrayList<Integer> dims;
        if (pointer instanceof Param) { // 如果左值是数组形参，维数信息在Param中取得
            dims = ((Param) pointer).getDims();
        } else {    // 如果左值是全局数组或局部数组，维数列表在指针的目标类型（数组类型）中取得
            ArrayType arrayType = (ArrayType) ((PointerType) pointer.getType()).getTargetType();
            dims = arrayType.getDims();
        }
        // 获取左值下标列表 比如左值 a[1][2] 的下标列表为 {1, 2}
        ArrayList<Value> indexes = new ArrayList<>();
        for (ExpNode expNode : lValNode.getExpNodes()) {
            indexes.add(visitExpNode(expNode));
        }
        // 计算偏移量
        Value offset = irBuilder.calOffset(indexes, dims);
        // 创建GEP指令
        GEPInst gepInst = new GEPInst(new PointerType(IntegerType.i32), irBuilder.genLocalVarName(), pointer, offset);
        // 添加指令到当前基本块
        irBuilder.addInstToCurBasicBlock(gepInst);
        // 返回数组元素i32*指针
        return gepInst;
    }

    // 22.基本表达式 PrimaryExp → '(' Exp ')' | LVal | Number // 三种情况均需覆盖
    private Value visitPrimaryExpNode(PrimaryExpNode primaryExpNode) {
        if (primaryExpNode.getExpNode() != null) {  // PrimaryExp → '(' Exp ')'
            return visitExpNode(primaryExpNode.getExpNode());
        } else if (primaryExpNode.getlValNode() != null) {  // PrimaryExp → LVal
            return visitLValNodeForR(primaryExpNode.getlValNode());
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

    /**
     * @return 返回值type可能为i1或i32，调用该方法后请注意类型转换
     */
    private Value visitUnaryExpNode(UnaryExpNode unaryExpNode) {
        if (unaryExpNode.getPrimaryExpNode() != null) { // UnaryExp → PrimaryExp    // 不生成指令，只返回值 // 返回值类型为i1或i32
            return visitPrimaryExpNode(unaryExpNode.getPrimaryExpNode());
        } else if (unaryExpNode.getUnaryOpNode() != null) { // UnaryExp → UnaryOp UnaryExp
            // 此处解析 UnaryOp
            // 25.单目运算符 UnaryOp → '+' | '−' | '!' //注：'!'仅出现在条件表达式中 // 三种均需覆盖
            UnaryOpNode unaryOpNode = unaryExpNode.getUnaryOpNode();
            Token op = unaryOpNode.getToken();
            if (op.getType() == TokenType.PLUS) {   // 如果是正号，不生成指令，返回正号后面的值 // 返回值类型为i1或i32
                return visitUnaryExpNode(unaryExpNode.getUnaryExpNode());
            } else if (op.getType() == TokenType.MINU) {    // 如果是负号，生成指令 // 将 -a 转换为 0 - a // 返回值类型为i1或i32
                // 必须先访问子结点，再分配局部寄存器编号，确保编号顺序递增
                Value operand2 = visitUnaryExpNode(unaryExpNode.getUnaryExpNode());
                // 如果operand2类型不是i32，转换到i32
                if (operand2.getType() != IntegerType.i32) {
                    // 插入zext指令
                    ZextInst zextInst = new ZextInst(irBuilder.genLocalVarName(), operand2, IntegerType.i32);
                    // 添加指令到当前基本块
                    irBuilder.addInstToCurBasicBlock(zextInst);
                    // 更新操作数2
                    operand2 = zextInst;
                }
                // 创建指令
                BinaryInst binaryInst = new BinaryInst(Opcode.sub, irBuilder.genLocalVarName(), new Constant(IntegerType.i32, 0), operand2);
                // 添加指令到当前基本块
                irBuilder.addInstToCurBasicBlock(binaryInst);
                return binaryInst;
            } else {    // 如果是'!' // 将 !a 转换为 a == 0    // 返回值类型为i1
                Value operand1 = visitUnaryExpNode(unaryExpNode.getUnaryExpNode());
                // 如果operand1类型不是i32，转换到i32
                if (operand1.getType() != IntegerType.i32) {
                    // 插入zext指令
                    ZextInst zextInst = new ZextInst(irBuilder.genLocalVarName(), operand1, IntegerType.i32);
                    // 添加指令到当前基本块
                    irBuilder.addInstToCurBasicBlock(zextInst);
                    // 更新操作数1
                    operand1 = zextInst;
                }
                // 创建指令
                IcmpInst icmpInst = new IcmpInst(irBuilder.genLocalVarName(), IcmpInst.IcmpKind.eq, operand1, new Constant(IntegerType.i32, 0));
                // 添加指令到当前基本块
                irBuilder.addInstToCurBasicBlock(icmpInst);
                return icmpInst;
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

    // 25.单目运算符 UnaryOp → '+' | '−' | '!' //注：'!'仅出现在条件表达式中 // 三种均需覆盖

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

    /**
     * @return 返回值type可能为i1或i32，调用该方法后请注意类型转换
     */
    private Value visitMulExpNode(MulExpNode mulExpNode) {
        if (mulExpNode.getMulExpNode() == null) {   // MulExp → UnaryExp
            return visitUnaryExpNode(mulExpNode.getUnaryExpNode());
        } else {    // MulExp → MulExp ('*' | '/' | '%') UnaryExp
            // 访问子结点
            // 必须按此顺序，确保寄存器编号递增
            Value operand1 = visitMulExpNode(mulExpNode.getMulExpNode());
            // 如果operand1类型不是i32, 转换到i32
            if (operand1.getType() != IntegerType.i32) {
                // 插入zext指令
                ZextInst zextInst = new ZextInst(irBuilder.genLocalVarName(), operand1, IntegerType.i32);
                // 添加指令到当前基本块
                irBuilder.addInstToCurBasicBlock(zextInst);
                // 更新操作数1
                operand1 = zextInst;
            }
            Value operand2 = visitUnaryExpNode(mulExpNode.getUnaryExpNode());
            // 如果operand2类型不是i32, 转换到i32
            if (operand2.getType() != IntegerType.i32) {
                // 插入zext指令
                ZextInst zextInst = new ZextInst(irBuilder.genLocalVarName(), operand2, IntegerType.i32);
                // 添加指令到当前基本块
                irBuilder.addInstToCurBasicBlock(zextInst);
                // 更新操作数2
                operand2 = zextInst;
            }
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

    /**
     * @return 返回值type可能为i1或i32，调用该方法后请注意类型转换
     */
    private Value visitAddExpNode(AddExpNode addExpNode) {
        if (addExpNode.getAddExpNode() == null) {   // AddExp → MulExp
            return visitMulExpNode(addExpNode.getMulExpNode());
        } else {    // AddExp → AddExp ('+' | '−') MulExp
            // 访问子结点
            // 必须按此顺序，确保寄存器编号递增
            Value operand1 = visitAddExpNode(addExpNode.getAddExpNode());
            // 如果operand1类型不是i32, 转换到i32
            if (operand1.getType() != IntegerType.i32) {
                // 插入zext指令
                ZextInst zextInst = new ZextInst(irBuilder.genLocalVarName(), operand1, IntegerType.i32);
                // 添加指令到当前基本块
                irBuilder.addInstToCurBasicBlock(zextInst);
                // 更新操作数1
                operand1 = zextInst;
            }
            Value operand2 = visitMulExpNode(addExpNode.getMulExpNode());
            // 如果operand2类型不是i32, 转换到i32
            if (operand2.getType() != IntegerType.i32) {
                // 插入zext指令
                ZextInst zextInst = new ZextInst(irBuilder.genLocalVarName(), operand2, IntegerType.i32);
                // 添加指令到当前基本块
                irBuilder.addInstToCurBasicBlock(zextInst);
                // 更新操作数2
                operand2 = zextInst;
            }
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

    // 29.关系表达式 RelExp → AddExp | RelExp ('<' | '>' | '<=' | '>=') AddExp // 1.AddExp 2.< 3.> 4.<= 5.>= 均需覆盖
    // 【消除左递归】 RelExp → AddExp { ('<' | '>' | '<=' | '>=') AddExp}

    /**
     * @return 返回值type可能为i1或i32，调用该方法后请注意类型转换
     */
    private Value visitRelExpNode(RelExpNode relExpNode) {
        if (relExpNode.getRelExpNode() == null) {   // RelExp → AddExp // 返回类型为i32
            return visitAddExpNode(relExpNode.getAddExpNode());
        } else {    // RelExp → RelExp ('<' | '>' | '<=' | '>=') AddExp // 返回类型为i1
            // 解析operand1
            Value operand1 = visitRelExpNode(relExpNode.getRelExpNode());
            // 如果operand1类型不是i32, 转换到i32
            if (operand1.getType() != IntegerType.i32) {
                // 插入zext指令
                ZextInst zextInst = new ZextInst(irBuilder.genLocalVarName(), operand1, IntegerType.i32);
                // 添加指令到当前基本块
                irBuilder.addInstToCurBasicBlock(zextInst);
                // 更新操作数1
                operand1 = zextInst;
            }
            // 解析比较符号
            Token op = relExpNode.getOp();
            IcmpInst.IcmpKind icmpKind;
            if (op.getType() == TokenType.LSS) {  // <
                icmpKind = IcmpInst.IcmpKind.slt;
            } else if (op.getType() == TokenType.LEQ) { // <=
                icmpKind = IcmpInst.IcmpKind.sle;
            } else if (op.getType() == TokenType.GRE) { // >
                icmpKind = IcmpInst.IcmpKind.sgt;
            } else {    // >=
                icmpKind = IcmpInst.IcmpKind.sge;
            }
            // 解析operand2
            Value operand2 = visitAddExpNode(relExpNode.getAddExpNode());
            // 如果operand2类型不是i32, 转换到i32
            if (operand2.getType() != IntegerType.i32) {
                // 插入zext指令
                ZextInst zextInst = new ZextInst(irBuilder.genLocalVarName(), operand2, IntegerType.i32);
                // 添加指令到当前基本块
                irBuilder.addInstToCurBasicBlock(zextInst);
                // 更新操作数2
                operand2 = zextInst;
            }
            // 创建icmp指令
            IcmpInst icmpInst = new IcmpInst(irBuilder.genLocalVarName(), icmpKind, operand1, operand2);
            // 添加指令到当前基本块
            irBuilder.addInstToCurBasicBlock(icmpInst);
            return icmpInst;
        }
    }

    // 30.相等性表达式 EqExp → RelExp | EqExp ('==' | '!=') RelExp // 1.RelExp 2.== 3.!= 均需覆盖
    // 【消除左递归】 EqExp → RelExp { ('==' | '!=') RelExp}

    /**
     * @return 返回值type可能为i1或i32，调用该方法后请注意类型转换
     */
    private Value visitEqExpNode(EqExpNode eqExpNode) {
        if (eqExpNode.getEqExpNode() == null) { // EqExp → RelExp   // 返回值类型为i1或i32
            return visitRelExpNode(eqExpNode.getRelExpNode());
        } else {    // EqExp → EqExp ('==' | '!=') RelExp   // 返回值类型为i1
            // 解析operand1
            Value operand1 = visitEqExpNode(eqExpNode.getEqExpNode());
            // 如果operand1类型不是i32，转换到i32
            if (operand1.getType() != IntegerType.i32) {
                // 插入zext指令
                ZextInst zextInst = new ZextInst(irBuilder.genLocalVarName(), operand1, IntegerType.i32);
                // 添加指令到当前基本块
                irBuilder.addInstToCurBasicBlock(zextInst);
                // 更新操作数1
                operand1 = zextInst;
            }
            // 解析比较符号
            Token op = eqExpNode.getOp();
            IcmpInst.IcmpKind icmpKind;
            if (op.getType() == TokenType.EQL) {    // ==
                icmpKind = IcmpInst.IcmpKind.eq;
            } else {    // !=
                icmpKind = IcmpInst.IcmpKind.ne;
            }
            // 解析operand2
            Value operand2 = visitRelExpNode(eqExpNode.getRelExpNode());
            // 如果operand2类型不是i32, 转换到i32
            if (operand2.getType() != IntegerType.i32) {
                // 插入zext指令
                ZextInst zextInst = new ZextInst(irBuilder.genLocalVarName(), operand2, IntegerType.i32);
                // 添加指令到当前基本块
                irBuilder.addInstToCurBasicBlock(zextInst);
                // 更新操作数2
                operand2 = zextInst;
            }
            // 创建icmp指令
            IcmpInst icmpInst = new IcmpInst(irBuilder.genLocalVarName(), icmpKind, operand1, operand2);
            // 添加指令到当前基本块
            irBuilder.addInstToCurBasicBlock(icmpInst);
            return icmpInst;
        }
    }

    // 31.逻辑与表达式 LAndExp → EqExp | LAndExp '&&' EqExp // 1.EqExp 2.&& 均需覆盖
    // 【消除左递归】 LAndExp → EqExp {'&&' EqExp}
    private void visitLAndExpNode(LAndExpNode lAndExpNode) {
        if (lAndExpNode.getlAndExpNode() == null) { // LAndExp → EqExp
            // 解析eqExp
            Value cond = visitEqExpNode(lAndExpNode.getEqExpNode());
            // 处理简化的cond //比如把 if(a) 变为 if(a != 0), 即需要插入一条icmp语句 cond != 0, 使其返回i1类型
            if (cond.getType() != IntegerType.i1) { // 如果类型不为i1，说明一定是简化的条件，也即lAndExpNode.eqExpNode下面只有eqExpNode结点，而没有relExpNode结点
                Value operand2 = new Constant(IntegerType.i32, 0);
                // 创建icmp指令
                IcmpInst icmpInst = new IcmpInst(irBuilder.genLocalVarName(), IcmpInst.IcmpKind.ne, cond, operand2);
                // 添加指令到当前基本块
                irBuilder.addInstToCurBasicBlock(icmpInst);
                // 更新cond
                cond = icmpInst;
            }
            // 创建条件跳转指令
            BranchInst branchInst = new BranchInst(cond, irBuilder.getCurTrueBlock(), irBuilder.getCurFalseBlock());
            // 添加指令到当前基本块
            irBuilder.addInstToCurBasicBlock(branchInst);
        } else {    // LAndExp → LAndExp '&&' EqExp
            // 保存右边的条件真基本块
            BasicBlock rightTrue = irBuilder.getCurTrueBlock();
            // 创建右边基本块
            BasicBlock right = new BasicBlock(irBuilder.genBasicBlockLabel(), irBuilder.getCurFunction());

            // 让右边作为左边的条件真基本块
            irBuilder.setCurTrueBlock(right);
            // 解析左边
            visitLAndExpNode(lAndExpNode.getlAndExpNode());

            // 添加right基本块到当前函数(添加时机 决定输出顺序)
            irBuilder.addBasicBlockToCurFunction(right);
            // 还原右边的条件真基本块
            irBuilder.setCurTrueBlock(rightTrue);
            // 设置右边基本块为curBasicBlock
            irBuilder.setCurBasicBlock(right);
            // 解析右边eqExp
            Value cond = visitEqExpNode(lAndExpNode.getEqExpNode());
            // 处理简化的cond //比如把 if(a) 变为 if(a != 0), 即需要插入一条icmp语句 cond != 0, 使其返回i1类型
            if (cond.getType() != IntegerType.i1) { // 如果类型不为i1，说明一定是简化的条件，也即lAndExpNode.eqExpNode下面只有eqExpNode结点，而没有relExpNode结点
                Value operand2 = new Constant(IntegerType.i32, 0);
                // 创建icmp指令
                IcmpInst icmpInst = new IcmpInst(irBuilder.genLocalVarName(), IcmpInst.IcmpKind.ne, cond, operand2);
                // 添加指令到当前基本块
                irBuilder.addInstToCurBasicBlock(icmpInst);
                // 更新cond
                cond = icmpInst;
            }
            // 创建条件跳转指令
            BranchInst branchInst = new BranchInst(cond, irBuilder.getCurTrueBlock(), irBuilder.getCurFalseBlock());
            // 添加指令到当前基本块
            irBuilder.addInstToCurBasicBlock(branchInst);
        }
    }

    // 32.逻辑或表达式 LOrExp → LAndExp | LOrExp '||' LAndExp // 1.LAndExp 2.|| 均需覆盖
    // 【消除左递归】 LOrExp → LAndExp {'||' LAndExp}
    private void visitLOrExpNode(LOrExpNode lOrExpNode) {
        if (lOrExpNode.getlOrExpNode() == null) {   // LOrExp → LAndExp
            visitLAndExpNode(lOrExpNode.getlAndExpNode());
        } else {    // LOrExp → LOrExp '||' LAndExp
            // 保存右边的条件假基本块
            BasicBlock rightFalse = irBuilder.getCurFalseBlock();
            // 创建右边基本块
            BasicBlock right = new BasicBlock(irBuilder.genBasicBlockLabel(), irBuilder.getCurFunction());

            // 让右边作为左边的条件假基本块
            irBuilder.setCurFalseBlock(right);
            // 解析左边
            visitLOrExpNode(lOrExpNode.getlOrExpNode());

            // 添加right基本块到当前函数(添加时机 决定输出顺序)
            irBuilder.addBasicBlockToCurFunction(right);
            // 还原右边的条件假基本块
            irBuilder.setCurFalseBlock(rightFalse);
            // 设置右边基本块为curBasicBlock
            irBuilder.setCurBasicBlock(right);
            // 解析右边lAndExp
            visitLAndExpNode(lOrExpNode.getlAndExpNode());
        }
    }

    // 33.常量表达式 ConstExp → AddExp 注：使用的Ident 必须是常量 // 存在即可

    /**
     * 计算常量表达式的值。
     *
     * @return 常量表达式的值。
     */
    int visitConstExpNode(ConstExpNode constExpNode) {
        return constExpNode.getAddExpNode().calVal();
    }
}
