package midend.ir.inst;

import midend.ir.Function;
import midend.ir.Value;
import midend.ir.type.IntegerType;

import java.util.ArrayList;

public class CallInst extends Inst {
    final Function targetFunc;
    final ArrayList<Value> args;  // 实参

    /***
     *
     * @param name 右值寄存器名。如果调用的函数无返回值，请传入null。
     * @param targetFunc 调用的函数。
     * @param args 实参列表。如果无实参，请传入空数组，而不要传入null。
     */
    public CallInst(String name, Function targetFunc, ArrayList<Value> args) {
        super(targetFunc.getType(), name, Opcode.call); // 右值类型是函数返回值类型
        this.targetFunc = targetFunc;
        this.args = args;
        addOperand(targetFunc);
        for (Value arg : args) {
            addOperand(arg);
        }
    }

    public Function getTargetFunc() {
        return targetFunc;
    }

    public ArrayList<Value> getArgs() {
        return args;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (type == IntegerType.i32) {  // 如果调用的函数返回值为int，则将call的值存入虚拟寄存器
            sb.append(name).append(" = call i32 ");
        } else {    // 如果调用的函数返回值为void，只call
            sb.append("call void ");
        }
        sb.append(targetFunc.getName()).append("(");
        for (int i = 0; i < args.size(); i++) {
            sb.append(args.get(i).getType()).append(" ").append(args.get(i).getName());
            if (i != args.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append(")");
        return sb.toString();
    }
}
