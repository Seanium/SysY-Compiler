package midend.ir;

import midend.ir.type.ArrayType;
import midend.ir.type.IntegerType;

import java.util.ArrayList;

public class LocalArray extends Value {
    private final ArrayList<Integer> dims;
    private final int len;

    private final boolean isConst;

    /**
     * 局部数组。
     * 与全局数组不同，其type为数组类型。局部数组的指针类型为其alloc指令。
     * 局部数组不存入IR的结构，只会存入符号表。局部数组的alloc指令才会被存入IR的结构。
     * @param dims  C源代码中数组维数列表。比如 int a[2][3] 的 dims 为 {2, 3}。
     * @param len 数组中元素总个数。
     * @param isConst 声明是否有const关键字。
     */
    public LocalArray(ArrayList<Integer> dims, int len, boolean isConst) {
        super(new ArrayType(dims, len, IntegerType.i32), "");   // 局部数组的元素类型目前都是i32
        this.dims = dims;
        this.len = len;
        this.isConst = isConst;
    }

    public int getLen() {
        return len;
    }

    /**
     * 设置局部数组的右值名。
     * @param name 右值名为：数组的alloc语句中，赋值号左侧的寄存器名。
     */
    public void setName(String name) {
        this.name = name;
    }
}
