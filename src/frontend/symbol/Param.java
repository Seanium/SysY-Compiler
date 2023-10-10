package frontend.symbol;

import java.util.Objects;

class Param {
    private String name;    //形参名称
    private int dim;    // 形参类型 0: 变量, 1: 一维数组, 2: 二维数组

    public Param(String name, int dim) {
        this.name = name;
        this.dim = dim;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Param param = (Param) o;
        return dim == param.dim && Objects.equals(name, param.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, dim);
    }
}
