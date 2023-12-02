package midend.ir;

import midend.ir.type.Type;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;

public class Function extends Value {

    private final ArrayList<Param> params;    // 没有形参则为无元素
    private final ArrayList<BasicBlock> basicBlocks;
    private boolean isLib;  // 是否为库函数
    /**
     * 该函数所有的变量。也就是要分配寄存器的value。
     */
    private final LinkedHashSet<Value> vars;
    private boolean hasSideEffect;
    /**
     * 该函数所调用的函数集合。
     */
    private HashSet<Function> callees;

    /**
     * @param name 函数名。
     * @param type 右值类型，即返回值类型。
     */
    public Function(String name, Type type) {
        super(type, "@" + name);  // 右值类型为其返回值类型，右值名为其函数名
        this.basicBlocks = new ArrayList<>();
        this.params = new ArrayList<>();
        this.isLib = false;
        this.vars = new LinkedHashSet<>();
        this.hasSideEffect = true;
        this.callees = new HashSet<>();
    }

    /**
     * 添加形参到形参列表。如果没有形参，则不必调用该函数。
     *
     * @param param 要添加的形参。
     */
    public void addParam(Param param) {
        this.params.add(param);
    }

    /**
     * 标记函数为库函数。
     */
    public void setIsLib() {
        isLib = true;
    }

    public boolean isLib() {
        return isLib;
    }

    public void addBasicBlock(BasicBlock basicBlock) {
        basicBlocks.add(basicBlock);
    }

    public ArrayList<BasicBlock> getBasicBlocks() {
        return basicBlocks;
    }

    public ArrayList<Param> getParams() {
        return params;
    }

    public LinkedHashSet<Value> getVars() {
        return vars;
    }

    public boolean hasSideEffect() {
        return hasSideEffect;
    }

    public void setHasSideEffect(boolean hasSideEffect) {
        this.hasSideEffect = hasSideEffect;
    }

    public void addCallee(Function callee) {
        callees.add(callee);
    }

    public HashSet<Function> getCallees() {
        return callees;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (isLib) {    // 如果是库函数，只需要声明
            sb.append("declare ");
        } else {        // 如果不是库函数，需要定义
            sb.append("define dso_local ");
        }
        sb.append(type.toString()).append(" ").append(name).append("(");
        for (int i = 0; i < params.size(); i++) {
            sb.append(params.get(i).toString());
            if (i != params.size() - 1) {
                sb.append(", ");
            }
        }
        if (isLib) {    // 如果是库函数，不输出大括号，无基本块
            sb.append(")");
        } else {        // 如果不是库函数，要输出大括号和基本块
            sb.append(") ").append("{\n");
            for (BasicBlock basicBlock : basicBlocks) {
                sb.append(basicBlock.toString());
            }
            sb.append("}");
        }
        return sb.toString();
    }
}
