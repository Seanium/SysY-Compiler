package midend.ir.inst;

import midend.ir.Value;
import midend.ir.type.IntegerType;

public class IcmpInst extends Inst {
    final IcmpKind icmpKind;  // 比较类型

    /**
     *
     * @param name  右值寄存器名。
     * @param icmpKind  比较类型。
     * @param operand1  操作数1。
     * @param operand2  操作数2。
     */
    public IcmpInst(String name, IcmpKind icmpKind, Value operand1, Value operand2) {
        super(IntegerType.i1, name, Opcode.icmp);
        this.icmpKind = icmpKind;
        addOperand(operand1);
        addOperand(operand2);
    }

    public IcmpKind getIcmpKind() {
        return icmpKind;
    }

    public Value getOperand1() {
        return operandList.get(0);
    }

    public Value getOperand2() {
        return operandList.get(1);
    }

    @Override
    public String toString() {
        Value operand1 = getOperand1();
        Value operand2 = getOperand2();
        return name + " = icmp " + icmpKind.toString() + " " +
                operand1.getType() + " " + operand1.getName() + ", " + operand2.getName();
    }

    /**
     * Icmp比较类型的枚举类。
     */
    public enum IcmpKind {
        /**
         * 等于
         */
        eq,
        /**
         * 不等于
         */
        ne,
        /**
         * 有符号大于
         */
        sgt,
        /**
         * 有符号大于等于
         */
        sge,
        /**
         * 有符号小于
         */
        slt,
        /**
         * 有符号小于等于
         */
        sle
    }
}
