package midend.ir.inst;

import midend.ir.Value;
import midend.ir.type.PointerType;

public class LoadInst extends Inst {

    /***
     *
     * @param name 待存放到的寄存器名
     * @param pointer 待取出元素的指针
     */
    public LoadInst(String name, Value pointer) {
        // 右值类型是访问指针解引用类型
        super(((PointerType) pointer.getType()).getTargetType(), name, Opcode.load);
        addOperand(pointer);
    }

    public Value getPointer() {
        return operandList.get(0);
    }

    @Override
    public String toString() {
        Value pointer = getPointer();
        return name + " = load " + type + ", " + pointer.getType() + " " + pointer.getName();
    }
}
