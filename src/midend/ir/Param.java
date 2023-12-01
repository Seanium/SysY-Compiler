package midend.ir;

import midend.ir.type.Type;

import java.util.ArrayList;
import java.util.Objects;

public class Param extends Value {
    private ArrayList<Integer> dims;

    /**
     *
     * @param type 形参类型。
     * @param name 形参名称。如果是库函数，请传入空字符串，即""。否则应为寄存器。
     */
    public Param(Type type, String name) {
        super(type, name);
    }

    /**
     * 设置c源代码中数组形参维数列表。空缺的首个维度补为1。
     * 比如形参 int a[][2][3] 的 dims 为 {1, 2, 3}。
     * 比如形参 int b[] 的 dims 为 {0}。
     * 只有带中括号的形参(数组形参)需要调用此方法。
     */
    public void setDims(ArrayList<Integer> dims) {
        this.dims = dims;
    }

    public ArrayList<Integer> getDims() {
        return dims;
    }

    @Override
    public String toString() {
        if (Objects.equals(name, "")) { // 如果形参名为空串，则只输出形参类型（用于库函数）
            return type.toString();
        } else {    // 否则，输出形参类型和形参名
            return type + " " + name;
        }
    }
}
