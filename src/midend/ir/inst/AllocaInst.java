package midend.ir.inst;

import midend.ir.type.PointerType;
import midend.ir.type.Type;

public class AllocaInst extends Inst {
    final Type targetType;

    /**
     *
     * @param name 赋值号左边的寄存器名
     * @param targetType 所存储变量的类型
     */
    public AllocaInst(String name, Type targetType) {
        super(new PointerType(targetType), name, Opcode.alloca);
        this.targetType = targetType;
    }

    public Type getTargetType() {
        return targetType;
    }

    @Override
    public String toString() {
        return name + " = alloca " + targetType;
    }
}
