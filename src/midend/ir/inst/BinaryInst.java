package midend.ir.inst;

import midend.ir.Value;
import midend.ir.type.IntegerType;

public class BinaryInst extends Inst {
    private final Value operand1;
    private final Value operand2;


    public BinaryInst(Opcode opcode, String name, Value operand1, Value operand2) {
        super(IntegerType.i32, name, opcode);   // BinaryInst 的 name 是等号左边的变量名
        this.operand1 = operand1;
        this.operand2 = operand2;
        addOperand(operand1);
        addOperand(operand2);
    }

    public Value getOperand1() {
        return operand1;
    }

    public Value getOperand2() {
        return operand2;
    }

    @Override
    public String toString() {
        return name + " = " + opcode + " i32 " + operand1.getName() + ", " + operand2.getName();
    }
}
