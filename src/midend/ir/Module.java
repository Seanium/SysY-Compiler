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
        for (GlobalVar globalVar : globalVars) {
            sb.append(globalVar.toString()).append("\n");
        }
        for (Function function : functions) {
            sb.append(function.toString()).append("\n");
        }
        return sb.toString();
    }
}
