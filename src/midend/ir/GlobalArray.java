package midend.ir;

import midend.ir.type.ArrayType;
import midend.ir.type.PointerType;

import java.util.ArrayList;

/**
 * 在中间代码生成时，将任意维数组转为一维。
 */

public class GlobalArray extends Value {
    private final ArrayType arrayType;    // 数组类型，即自身type（属于PointerType）的targetType
    /**
     * C源代码中数组维数列表。比如 int a[2][3] 的 dims 为 {2, 3}。
     */
    private final ArrayList<Integer> dims;
    /**
     * 数组中元素总个数，即 dims 所有元素之乘积。
     * 比如 int a[2][3] 的 len 为 6。
     */
    private final int len;
    private final ArrayInitValue arrayInitValue;  // 在符号表中也存有初值。此处的属性是用于toString。
    private final boolean isConst;

    /**
     * 全局数组。type是数组类型的指针类型。
     *
     * @param name           数组名。
     * @param dims           C源代码中数组维数列表。比如 int a[2][3] 的 dims 为 {2, 3}。
     * @param len            数组中元素总个数。
     * @param arrayInitValue 数组初值。如果不显式声明初值，默认全为0，仍需传入初值。
     * @param isConst        声明是否有const关键字。
     */
    public GlobalArray(String name, ArrayList<Integer> dims, int len, ArrayInitValue arrayInitValue, boolean isConst) {
        super(new PointerType(new ArrayType(dims, len, arrayInitValue.type)), "@g_" + name);
        this.arrayType = (ArrayType) ((PointerType) type).getTargetType();
        this.dims = dims;
        this.len = len;
        this.arrayInitValue = arrayInitValue;
        this.isConst = isConst;
    }

    public boolean checkAllZero() {
        return arrayInitValue.checkAllZero();
    }

    public int getLen() {
        return len;
    }

    public ArrayInitValue getArrayInitValue() {
        return arrayInitValue;
    }

    public ArrayType getArrayType() {
        return arrayType;
    }

    @Override
    public String toString() {
        if (isConst) {  // 全局数组常量
            return name + " = dso_local constant " + arrayType + " " + arrayInitValue;
        } else {    // 全局数组变量
            return name + " = dso_local global " + arrayType + " " + arrayInitValue;
        }
    }

    /**
     * 转换为AsciizData构造函数所需字符串。
     */
    public String toAsciizDataContent() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arrayInitValue.getConstants().size() - 1; i++) {
            sb.append((char) arrayInitValue.getConstants().get(i).getValue());
        }
        return sb.toString();
    }
}
