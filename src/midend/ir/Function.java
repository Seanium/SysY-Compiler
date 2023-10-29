package midend.ir;

import midend.ir.type.Type;

import java.util.ArrayList;

public class Function extends Value {
    private final ArrayList<Param> params;
    private final ArrayList<BasicBlock> basicBlocks;

    /**
     * @param name 函数名。
     * @param type 右值类型，即返回值类型。
     * @param params 形参列表。如果无参数，请传入空数组，而不要传入null。
     */
    public Function(String name, Type type, ArrayList<Param> params) {
        super(type, "@" + name);  // 右值类型为其返回值类型，右值名为其函数名
        this.params = params;
        this.basicBlocks = new ArrayList<>();
    }

    public void addBasicBlock(BasicBlock basicBlock) {
        basicBlocks.add(basicBlock);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("define dso_local ").append(type.toString()).append(" ").append(name).append("(");
        for (int i = 0; i < params.size(); i++) {
            sb.append(params.get(i).toString());
            if (i != params.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append(") ").append("{\n");
        for (BasicBlock basicBlock : basicBlocks) {
            sb.append(basicBlock.toString());
        }
        sb.append("}");
        return sb.toString();
    }
}
