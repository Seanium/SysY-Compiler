package frontend.symbol;

public class ArraySymbol extends Symbol {
    private final boolean isConst;
    private final int dim;  //0: 变量, 1: 一维数组, 2: 二维数组

    public ArraySymbol(String name, boolean isConst, int dim) {
        super(name);
        this.isConst = isConst;
        this.dim = dim;
    }

    public int getDim() {
        return dim;
    }

    // 检测错误类型h【不能改变常量的值】
    public boolean isConst() {
        return isConst;
    }
}
