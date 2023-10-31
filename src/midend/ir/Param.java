package midend.ir;

import midend.ir.type.Type;

import java.util.Objects;

public class Param extends Value {
    /***
     *
     * @param type 形参类型。
     * @param name 形参名称。如果是库函数，请传入空字符串，即""。否则应为寄存器。
     */
    public Param(Type type, String name) {
        super(type, name);
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
