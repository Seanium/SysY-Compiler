package midend.ir.inst;

import midend.ir.Value;
import midend.ir.type.VoidType;

public class StoreInst extends Inst {
    final Value from;
    final Value to;

    /***
     *
     * @param from  待存储的value
     * @param to    存储位置
     */
    public StoreInst(Value from, Value to) {
        super(VoidType.voidType, "", Opcode.store);    // 不需要name，因为不会作为右值被引用; 同理，右值类型为void即可
        this.from = from;
        this.to = to;
        addOperand(from);
        addOperand(to);
    }

    public Value getFrom() {
        return from;
    }

    public Value getTo() {
        return to;
    }

    @Override
    public String toString() {
        return "store " + from.getType() + " " + from.getName() + ", " + to.getType() + " " + to.getName();
    }
}
