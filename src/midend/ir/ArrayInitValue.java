package midend.ir;

import midend.ir.type.IntegerType;
import midend.ir.type.Type;

import java.util.ArrayList;

public class ArrayInitValue extends Value {
    private final ArrayList<Constant> constants;

    /***
     * 数组的初值（自定义初值内容）。
     * @param type  初值元素类型。
     * @param constants 初值列表，元素为Constant。
     */
    public ArrayInitValue(Type type, ArrayList<Constant> constants) {
        super(type, ""); //type为数组元素类型，name为空串
        this.constants = constants;
    }

    /***
     * 向初值列表中添加i32元素。
     */
    public void addi32(int e) {
        this.constants.add(new Constant(IntegerType.i32, e));
    }

    /***
     * 向初值列表中添加n个i32的0。
     * @param n 添加 i32的0 的数量
     */
    public void addZeros(int n) {
        for (int i = 0; i < n; i++) {
            this.constants.add(new Constant(IntegerType.i32, 0));
        }
    }

    /***
     * 检查初值列表是否全为0。
     */
    private boolean checkAllZero() {
        for (Constant constant : constants) {
            if (constant.getValue() != 0) {
                return false;
            }
        }
        return true;
    }

    public ArrayList<Constant> getConstants() {
        return constants;
    }

    @Override
    public String toString() {
        if (type == IntegerType.i8) {   // 如果是格式字符串
            StringBuilder sb = new StringBuilder();
            sb.append("c\"");
            for (Constant constant : constants) {
                int ascii = constant.getValue();
                char c = (char) ascii;
                if (c == '\n') {    // '\n' 输出为 \0A
                    sb.append("\\0A");
                } else if (c == '\0') { // '\0' 输出为 \00
                    sb.append("\\00");
                } else {    // 其他的字符原样输出
                    sb.append(c);
                }
            }
            sb.append("\"");
            return sb.toString();
        } else {    // 如果是数字数组
            if (checkAllZero()) {
                return "zeroinitializer";
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append("[");
                for (int i = 0; i < constants.size(); i++) {
                    sb.append(type).append(" ").append(constants.get(i).getName());
                    if (i != constants.size() - 1) {
                        sb.append(", ");
                    }
                }
                sb.append("]");
                return sb.toString();
            }
        }
    }
}
