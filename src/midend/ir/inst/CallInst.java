package midend.ir.inst;

import midend.ir.Function;
import midend.ir.Value;

import java.util.ArrayList;

public class CallInst extends Inst {
    final Function targetFunc;
    final ArrayList<Value> args;  // 实参

    /***
     *
     * @param name 右值寄存器名
     * @param targetFunc 调用的函数
     * @param args 实参列表 //todo 空实参 待补充说明，参考Function
     */
    public CallInst(String name, Function targetFunc, ArrayList<Value> args) {
        super(targetFunc.getType(), name, Opcode.call); // 右值类型是函数返回值类型
        this.targetFunc = targetFunc;
        this.args = args;
        // todo addOperand的内容是这些吗
        addOperand(targetFunc);
        for (Value arg : args) {
            addOperand(arg);
        }
    }
}
