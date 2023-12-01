package midend.ir.inst;

import midend.ir.Value;

public class MoveInst extends Inst {

    /**
     * 汇编形式：   move to from
     */
    public MoveInst(Value to, Value from) {
        super(to.getType(), "move " + to.getName() + " " + from.getName(), Opcode.move);
        addOperand(to);
        addOperand(from);
    }

    public Value getTo() {
        return operandList.get(0);
    }

    public Value getFrom() {
        return operandList.get(1);
    }

    @Override
    public String toString() {
        Value to = getTo();
        Value from = getFrom();
        return "move " + to.getType() + " " + to.getName() +
                ", " + from.getType() + " " + from.getName();
    }
}
