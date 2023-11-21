package midend.ir.inst;

import midend.ir.Value;
import midend.ir.type.VoidType;

public class StoreInst extends Inst {

    /***
     *
     * @param from  待存储的value
     * @param to    存储位置
     */
    public StoreInst(Value from, Value to) {
        super(VoidType.voidType, "", Opcode.store);    // 不需要name，因为不会作为右值被引用; 同理，右值类型为void即可
        addOperand(from);
        addOperand(to);
    }

    public Value getFrom() {
        return operandList.get(0);
    }

    public Value getTo() {
        return operandList.get(1);
    }

    @Override
    public String toString() {
        Value from = getFrom();
        Value to = getTo();
        return "store " + from.getType() + " " + from.getName() + ", " + to.getType() + " " + to.getName();
    }
}
