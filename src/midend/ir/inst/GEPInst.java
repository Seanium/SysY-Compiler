package midend.ir.inst;

import midend.ir.Value;
import midend.ir.type.ArrayType;
import midend.ir.type.PointerType;
import midend.ir.type.Type;

public class GEPInst extends Inst {

    /***
     * GetElementPtr指令。
     * @param type 右值类型，必须是一个指针类型。目前，除了全局常量字符串是i8*，其他均为i32*。
     * @param name 右值名：赋值号左边的寄存器名。
     * @param basePointer 基址指针，其type应为指针类型。
     *                    局部数组请传入alloc指令，全局数组请传入globalArray，数组形参请传入param。
     * @param offset 偏移量。
     */
    public GEPInst(Type type, String name, Value basePointer, Value offset) {
        super(type, name, Opcode.gep);
        addOperand(basePointer);
        addOperand(offset);
    }

    public Value getBasePointer() {
        return operandList.get(0);
    }

    public Value getOffset() {
        return operandList.get(1);
    }

    @Override
    public String toString() {
        Value basePointer = getBasePointer();
        Value offset = getOffset();
        if (((PointerType) basePointer.getType()).getTargetType() instanceof ArrayType) {    // 如果是基指针是数组指针（全局数组、局部数组取元素）
            return name + " = getelementptr " +
                    ((PointerType) basePointer.getType()).getTargetType() + ", " +
                    basePointer.getType() + " " +
                    basePointer.getName() + ", i32 0, i32 " +
                    offset.getName();
        } else {    // 如果基指针是i32*（数组形参取元素）
            return name + " = getelementptr " +
                    ((PointerType) basePointer.getType()).getTargetType() + ", " +
                    basePointer.getType() + " " +
                    basePointer.getName() + ", i32 " +
                    offset.getName();
        }
    }
}
