package midend.ir;

import midend.ir.type.OtherType;

import java.util.ArrayList;

public class Module extends Value {
    private static Module instance;

    public static Module getInstance() {
        if (instance == null) {
            instance = new Module();
        }
        return instance;
    }

    /***
     * 全局声明列表，包括globalVar(非数组)和globalArray(数组)。
     */
    private final ArrayList<Value> globals;
    private final ArrayList<Function> functions;

    private Module() {
        super(OtherType.module, "module");
        this.globals = new ArrayList<>();
        this.functions = new ArrayList<>();
    }

    public void addGlobalVar(GlobalVar globalVar) {
        globals.add(globalVar);
    }

    public void addGlobalArray(GlobalArray globalArray) {
        globals.add(globalArray);
    }

    public void addFunction(Function function) {
        functions.add(function);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        ArrayList<Function> declFuncs = new ArrayList<>();  // 函数声明列表
        ArrayList<Function> defFuncs = new ArrayList<>();   // 函数定义列表
        for (Function function : functions) {
            if (function.isLib()) {
                declFuncs.add(function);
            } else {
                defFuncs.add(function);
            }
        }

        // 函数声明
        for (Function function : declFuncs) {
            sb.append(function.toString()).append("\n");
        }
        if (!declFuncs.isEmpty()) {
            sb.append("\n");
        }
        // 全局变量定义
        for (Value global : globals) {
            sb.append(global.toString()).append("\n");
        }
        if (!globals.isEmpty()) {
            sb.append("\n");
        }
        // 函数定义
        for (Function function : defFuncs) {
            sb.append(function.toString()).append("\n\n");
        }

        return sb.toString();
    }
}
