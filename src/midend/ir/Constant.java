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
}
