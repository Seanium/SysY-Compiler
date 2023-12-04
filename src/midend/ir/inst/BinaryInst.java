package midend.ir.inst;

import midend.ir.Value;
import midend.ir.type.IntegerType;

public class BinaryInst extends Inst {

    public BinaryInst(Opcode opcode, String name, Value operand1, Value operand2) {
        super(IntegerType.i32, name, opcode);   // BinaryInst 的 name 是等号左边的变量名
        addOperand(operand1);
        addOperand(operand2);
    }

    public Value getOperand1() {
        return operandList.get(0);
    }

    public Value getOperand2() {
        return operandList.get(1);
    }

    public void swapOperand() {
        Value op1 = operandList.get(0);
        Value op2 = operandList.get(1);
        operandList.set(0, op2);
        operandList.set(1, op1);
    }

    @Override
    public String toString() {
        Value operand1 = getOperand1();
        Value operand2 = getOperand2();
        return name + " = " + opcode + " i32 " + operand1.getName() + ", " + operand2.getName();
    }
}
