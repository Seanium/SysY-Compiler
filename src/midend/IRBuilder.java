package midend;

import midend.ir.BasicBlock;
import midend.ir.Function;
import midend.ir.GlobalVar;
import midend.ir.Module;
import midend.ir.inst.Inst;

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

    /***
     * 添加全局变量到模块
     */
    public void addGlobalVar(GlobalVar globalVar) {
        module.addGlobalVar(globalVar);
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
     * 添加指令到基本块
     */
    public void addInstToCurBasicBlock(Inst inst) {
        getCurBasicBlock().addInst(inst);
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
}
