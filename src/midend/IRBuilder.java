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

    /***
     * 添加全局变量到模块
     * @param globalVar
     */
    public void addGlobalVar(GlobalVar globalVar) {
        module.addGlobalVar(globalVar);
    }

    /***
     * 添加函数到模块
     * @param function
     */
    public void addFunctionToModule(Function function) {
        module.addFunction(function);
    }

    /***
     * 添加基本块到当前函数
     * @param basicBlock
     */
    public void addBasicBlockToCurFunction(BasicBlock basicBlock) {
        getCurFunction().addBasicBlock(basicBlock);
    }

    /***
     * 添加指令到基本块
     * @param inst
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
        return String.valueOf(genRegIndex(getCurFunction()));
    }

    /***
     * 生成当前函数的局部变量名
     */
    public String genLocalVarName() {
        return "%" + genRegIndex(getCurFunction());
    }
}
