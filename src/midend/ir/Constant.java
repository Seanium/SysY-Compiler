package midend.ir;

import midend.ir.type.Type;

public class Constant extends Value {
    private final int value;

    public Constant(Type type, int value) {
        // Constant 的 name 就是其 value 的字符串形式
        super(type, String.valueOf(value));
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    /**
     * 用于GVN时的公共子表达式合并。
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Constant constant = (Constant) o;

        return value == constant.value;
    }

    @Override
    public int hashCode() {
        return value;
    }
}
