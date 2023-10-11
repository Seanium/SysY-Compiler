package frontend.symbol;

import java.util.Objects;

public class Param {
    private final String name;    //形参名称
    private final int dim;    // 形参类型 0: 变量, 1: 一维数组, 2: 二维数组

    public Param(String name, int dim) {
        this.name = name;
        this.dim = dim;
    }

    public int getDim() {
        return dim;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, dim);
    }
}
