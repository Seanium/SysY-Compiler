package midend;

import midend.ir.Module;
import midend.ir.*;
import midend.ir.inst.*;
import midend.ir.type.IntegerType;
import midend.ir.type.VoidType;

import java.util.ArrayList;
import java.util.HashMap;

public class IRBuilder {
    private static IRBuilder instance;

    private IRBuilder() {
        this.module = Module.getInstance();
        this.regIndexMap = new HashMap<>();
    }

    public static IRBuilder getInstance() {
        if (instance == null) {
            instance = new IRBuilder();
        }
        return instance;
    }

    private final Module module;
    private final HashMap<Function, Integer> regIndexMap;
    private Function curFunction;
    private BasicBlock curBasicBlock;
    private BasicBlock curTrueBlock;    // 条件真目标基本块 用于if语句
    private BasicBlock curFalseBlock;   // 条件假目标基本块 用于if语句
    private BasicBlock continueTargetBlock; // continue语句的跳转目标基本块
    private BasicBlock breakTargetBlock;    // break语句的跳转目标基本块
    private ArrayInitValue curArrayInitValue;  // 保存当前解析的数组初值
    private AllocaInst allocaInst;  // 保存当前的局部数组变量指针 // 用于解析局部数组变量初值。依次创建GEP，解析初值，创建store指令。
    private int offset; // 保存当前的GEP指令偏移量    // 用于解析局部数组变量初值。依次创建GEP，解析初值，创建store指令。
    private int strArrIndex;   // 全局字符串常量数组的索引，用于生成globalArray的name

    /***
     * 添加全局变量(非数组)到模块
     */
    public void addGlobalVar(GlobalVar globalVar) {
        module.addGlobalVar(globalVar);
    }

    /***
     * 添加全局变量(数组)到模块
     */
    public void addGlobalArray(GlobalArray globalArray) {
        module.addGlobalArray(globalArray);
    }

    /***
     * 添加函数到模块
     */
    public void addFunctionToModule(Function function) {
        module.addFunction(function);
    }

    /***
     * 添加基本块到当前函数
     */
    public void addBasicBlockToCurFunction(BasicBlock basicBlock) {
        getCurFunction().addBasicBlock(basicBlock);
    }

    /***
     * 添加指令到当前基本块。
     */
    public void addInstToCurBasicBlock(Inst inst) {
        getCurBasicBlock().addInst(inst);
    }

    /***
     * 添加指令到指定基本块。
     */
    private void addInstToBasicBlock(Inst inst, BasicBlock basicBlock) {
        basicBlock.addInst(inst);
    }

    // 获取当前函数
    public Function getCurFunction() {
        return curFunction;
    }

    // 设置当前函数
    public void setCurFunction(Function curFunction) {
        this.curFunction = curFunction;
        regIndexMap.put(curFunction, 0);
    }

    // 获取当前基本块
    public BasicBlock getCurBasicBlock() {
        return curBasicBlock;
    }

    // 设置当前基本块
    public void setCurBasicBlock(BasicBlock curBasicBlock) {
        this.curBasicBlock = curBasicBlock;
    }

    // 生成虚拟寄存器编号
    private int genRegIndex(Function function) {
        int curRegIndex = regIndexMap.get(function);
        regIndexMap.put(function, curRegIndex + 1);
        return curRegIndex;
    }

    /***
     * 生成当前函数的基本块标签名
     */
    public String genBasicBlockLabel() {
        return "v" + genRegIndex(getCurFunction());
    }

    /***
     * 生成当前函数的局部变量名
     */
    public String genLocalVarName() {
        return "%v" + genRegIndex(getCurFunction());
    }

    /***
     * 生成全局字符串常量数组名。
     */
    public String genStrArrName() {
        int curStrArrIndex = strArrIndex;
        strArrIndex++;
        return ".str" + curStrArrIndex;
    }

    /***
     * 获取当前条件真目标基本块。
     */
    public BasicBlock getCurTrueBlock() {
        return curTrueBlock;
    }

    /***
     * 设置当前条件真目标基本块。
     */
    public void setCurTrueBlock(BasicBlock curTrueBlock) {
        this.curTrueBlock = curTrueBlock;
    }

    /***
     * 获取当前条件假目标基本块。
     */
    public BasicBlock getCurFalseBlock() {
        return curFalseBlock;
    }

    /***
     * 设置当前条件假目标基本块。
     */
    public void setCurFalseBlock(BasicBlock curFalseBlock) {
        this.curFalseBlock = curFalseBlock;
    }

    /***
     * 获取continue语句的跳转目标基本块。
     */
    public BasicBlock getContinueTargetBlock() {
        return continueTargetBlock;
    }

    /***
     * 设置continue语句的跳转目标基本块。
     */
    public void setContinueTargetBlock(BasicBlock continueTargetBlock) {
        this.continueTargetBlock = continueTargetBlock;
    }

    /***
     * 获取break语句的跳转目标基本块。
     */
    public BasicBlock getBreakTargetBlock() {
        return breakTargetBlock;
    }

    /***
     * 设置break语句的跳转目标基本块。
     */
    public void setBreakTargetBlock(BasicBlock breakTargetBlock) {
        this.breakTargetBlock = breakTargetBlock;
    }

    /***
     * 获取当前解析的数组初值。
     */
    public ArrayInitValue getCurArrayInitValue() {
        return curArrayInitValue;
    }

    /***
     * 设置当前解析的数组初值。
     */
    public void setCurArrayInitValue(ArrayInitValue curArrayInitValue) {
        this.curArrayInitValue = curArrayInitValue;
    }

    /***
     * 获取当前的局部数组变量指针。
     */
    public AllocaInst getAllocaInst() {
        return allocaInst;
    }

    /***
     * 设置当前的局部数组变量指针。
     */
    public void setAllocaInst(AllocaInst allocaInst) {
        this.allocaInst = allocaInst;
    }

    /***
     * 获取当前的GEP指令偏移量。
     */
    public int getOffset() {
        return offset;
    }

    /***
     * 设置当前的GEP指令偏移量。
     */
    public void setOffset(int offset) {
        this.offset = offset;
    }

    /***
     * 根据数组维数计算len。
     * @param dims 数组维数列表。
     */
    public int calLen(ArrayList<Integer> dims) {
        int len = 1;
        for (int dim : dims) {
            len *= dim;
        }
        return len;
    }

    /***
     * 计算数组元素左值的偏移量。
     * @param indexes   左值下标列表。
     * @param dims  数组维度列表。
     */
    public Value calOffset(ArrayList<Value> indexes, ArrayList<Integer> dims) {
        // 转换数组维数列表 到 数组维数乘积列表
        // 方法是，丢弃第一维，其余位置是与后面所有维度的成绩，最后补1
        // 比如 {1, 3, 2, 4} 转化为 {3*2*4, 2*4, 4, 1} = {24, 8, 4, 1}
        // 比如 {1} 转化为 {1}
        // 转换之后得到的dimsProd列表，只需与左值下标列表做点乘，即得到偏移量
        ArrayList<Integer> dimsProd = new ArrayList<>();
        int curProd = 1;    // 暂存目前的乘积
        for (int i = dims.size() - 1; i >= 1; i--) {    // 倒序遍历
            curProd *= dims.get(i);
            dimsProd.add(0, curProd);   // 插入到头
        }
        dimsProd.add(1);

        // 计算偏移量
        // 比如数组声明为 int a[][3][2][4], disProd列表为 {24, 8, 4, 1}
        // 若左值为 a[exp0][exp1][exp2]
        // 则偏移量为 exp0*24 + exp1*8 + exp2*4
        // 若左值为 a
        // 则偏移量为 0
        Constant zero = new Constant(IntegerType.i32, 0);
        Value offset = zero;    // 将偏移量初始化为0，以适应下标列表为空的情况
        for (int i = 0; i < indexes.size(); i++) {
            Value ithIndex = indexes.get(i);    // 第i个下标
            Constant ithDimProd = new Constant(IntegerType.i32, dimsProd.get(i));   // 第i个维数乘积
            if (offset == zero) {   // 如果是第一次循环，只乘不加
                if (ithDimProd.getValue() == 1) {   // 如果维数为1，不用创建乘指令
                    offset = ithIndex;
                } else {    // 如果维数大于1，要创建乘指令
                    offset = new BinaryInst(Opcode.mul, genLocalVarName(), ithDimProd, ithIndex);
                    addInstToCurBasicBlock((Inst) offset);
                }
            } else {    // 如果是后续循环，乘完当前结果，再加上之前的偏移量
                if (ithDimProd.getValue() == 1) {   // 如果维数为1，不用创建乘指令，只需创建加指令
                    offset = new BinaryInst(Opcode.add, genLocalVarName(), offset, ithIndex);
                    addInstToCurBasicBlock((Inst) offset);
                } else {    // 如果维数大于1，要创建乘指令和加指令
                    BinaryInst mulInst = new BinaryInst(Opcode.mul, genLocalVarName(), ithDimProd, ithIndex);
                    addInstToCurBasicBlock(mulInst);
                    offset = new BinaryInst(Opcode.add, genLocalVarName(), offset, mulInst);
                    addInstToCurBasicBlock((Inst) offset);
                }
            }
        }
        return offset;
    }

    /***
     * 计算数组元素左值的偏移量。用于常量初值的编译时计算，不创建指令，直接返回int。
     * @param indexes   左值下标列表。
     * @param dims  数组维度列表。
     */
    public int calOffsetForCalVal(ArrayList<Integer> indexes, ArrayList<Integer> dims) {
        // 转换数组维数列表 到 数组维数乘积列表
        // 方法是，丢弃第一维，其余位置是与后面所有维度的成绩，最后补1
        // 比如 {1, 3, 2, 4} 转化为 {3*2*4, 2*4, 4, 1} = {24, 8, 4, 1}
        // 比如 {1} 转化为 {1}
        // 转换之后得到的dimsProd列表，只需与左值下标列表做点乘，即得到偏移量
        ArrayList<Integer> dimsProd = new ArrayList<>();
        int curProd = 1;    // 暂存目前的乘积
        for (int i = dims.size() - 1; i >= 1; i--) {    // 倒序遍历
            curProd *= dims.get(i);
            dimsProd.add(0, curProd);   // 插入到头
        }
        dimsProd.add(1);

        // 计算偏移量
        // 比如数组声明为 int a[][3][2][4], disProd列表为 {24, 8, 4, 1}
        // 若左值为 a[exp0][exp1][exp2][exp3] // 这里的exp其实都是int
        // 则偏移量为 exp0*24 + exp1*8 + exp2*4 + exp3*1
        // 若左值为 a
        // 则偏移量为 0
        int offset = 0;    // 将偏移量初始化为0，以适应下标列表为空的情况
        for (int i = 0; i < indexes.size(); i++) {
            int ithIndex = indexes.get(i);    // 第i个下标
            int ithDimProd = dimsProd.get(i);   // 第i个维数乘积
            if (offset == 0) {   // 如果当前偏移为0(包括第一次进入循环的情况)，只乘不加
                if (ithDimProd == 1) {   // 如果维数为1，不用乘
                    offset = ithIndex;
                } else {    // 如果维数大于1，要乘
                    offset = ithDimProd * ithIndex;
                }
            } else {    // 如果是后续循环，乘完当前结果，再加上之前的偏移量
                if (ithDimProd == 1) {   // 如果维数为1，不用乘，只需加
                    offset = offset + ithIndex;
                } else {    // 如果维数大于1，要乘和加
                    int mul = ithDimProd * ithIndex;
                    offset = offset + mul;
                }
            }
        }
        return offset;
    }

    /***
     * // 如果是void函数且最后没有return;，则补充return;
     * @param func 要检查的函数。
     */

    public void checkReturn(Function func) {
        // 如果是void函数
        if (func.getType() == VoidType.voidType) {
            BasicBlock lastBasicBlock = func.getBasicBlocks().get(func.getBasicBlocks().size() - 1);
            // 如果最后一个基本块的没有指令，或者最后一条指令不是return
            if (lastBasicBlock.getInstructions().isEmpty() ||
                    !(lastBasicBlock.getInstructions().get(lastBasicBlock.getInstructions().size() - 1) instanceof ReturnInst)) {
                // 则补充return;
                ReturnInst returnInst = new ReturnInst(null);
                addInstToBasicBlock(returnInst, lastBasicBlock);
            }
        }
    }
}
