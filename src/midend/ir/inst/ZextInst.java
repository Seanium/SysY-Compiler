package midend.ir.inst;

import midend.ir.Value;
import midend.ir.type.Type;

public class ZextInst extends Inst {
    private final Value oriValue;
    private final Type targetType;

    /***
     * 创建类型扩充指令。比如把i1转换到i32。
     * @param name 右值寄存器名。
     * @param oriValue 待转换类型的value。
     * @param targetType 要转换到的类型。
     */
    public ZextInst(String name, Value oriValue, Type targetType) {
        super(targetType, name, Opcode.zext);
        this.oriValue = oriValue;
        this.targetType = targetType;
        addOperand(oriValue);
    }

    public Value getOriValue() {
        return oriValue;
    }

    @Override
    public String toString() {
        return name + " = zext " + oriValue.getType() + " " + oriValue.getName() + " to " + targetType;
    }
}
