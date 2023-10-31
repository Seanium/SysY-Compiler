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

    private final ArrayList<GlobalVar> globalVars;
    private final ArrayList<Function> functions;

    private Module() {
        super(OtherType.module, "module");
        this.globalVars = new ArrayList<>();
        this.functions = new ArrayList<>();
    }

    public void addGlobalVar(GlobalVar globalVar) {
        globalVars.add(globalVar);
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
        sb.append("\n");
        // 全局变量定义
        for (GlobalVar globalVar : globalVars) {
            sb.append(globalVar.toString()).append("\n");
        }
        sb.append("\n");
        // 函数定义
        for (Function function : defFuncs) {
            sb.append(function.toString()).append("\n");
        }

        return sb.toString();
    }
}
