package frontend.symbol;

public class ArraySymbol extends Symbol {
    private boolean isConst;
    private int dim;  //0: 变量, 1: 一维数组, 2: 二维数组

    public ArraySymbol(String name, boolean isConst, int dim) {
        super(name);
        this.isConst = isConst;
        this.dim = dim;
    }
}
