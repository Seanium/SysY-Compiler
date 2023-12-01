package midend.ir.symbol;

import midend.ir.Value;

public class IRSymbol {
    private final Value symbol;
    private final Value initValue;

    /**
     *
     * @param symbol 符号，为 Value 子类的实例。
     *               对于非数组全局变量、非数组全局常量，为 GlobalVar 的实例;
     *               对于非数组局部变量、非数组局部常量，为 AllocaInst 的实例;
     *               对于函数，为 Function 的示例；
     *               对于数组全局变量、数组全局常量，为 GlobalArray 的实例；
     *               对于数组局部变量、数组局部常量，为 AllocaInst 的实例。
     * @param initValue 初值。
     *                  如果没有初值（如函数、无初值的变量），请传入null。
     */
    public IRSymbol(Value symbol, Value initValue) {
        this.symbol = symbol;
        this.initValue = initValue;
    }

    public Value getSymbol() {
        return symbol;
    }

    public Value getInitValue() {
        return initValue;
    }
}
