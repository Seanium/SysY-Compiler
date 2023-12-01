package midend.ir.type;

import java.util.ArrayList;

public class ArrayType extends Type {
    /**
     * 数组中元素的个数。
     */
    private final int len;
    /**
     * 数组中元素的类型。
     */
    private final Type eleType;
    private final ArrayList<Integer> dims;

    /**
     *
     * @param len 数组中元素总个数，即 dims 所有元素之乘积。
     * @param dims C源代码中数组维数列表。比如 int a[2][3] 的 dims 为 {2, 3}。
     * @param eleType 元素类型。
     */
    public ArrayType(ArrayList<Integer> dims, int len, Type eleType) {
        this.dims = dims;
        this.len = len;
        this.eleType = eleType;
    }

    public ArrayList<Integer> getDims() {
        return dims;
    }

    public Type getEleType() {
        return eleType;
    }

    public int getLen() {
        return len;
    }

    @Override
    public String toString() {
        return "[" + len + " x " + eleType + "]";
    }
}
