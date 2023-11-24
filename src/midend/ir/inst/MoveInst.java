package midend.ir.inst;

import midend.ir.Value;

public class MoveInst extends Inst {
    private final Value to;

    /***
     * 汇编形式：   move to from
     */
    public MoveInst(Value to, Value from) {
        super(to.getType(), to.getName(), Opcode.move);
        this.to = to;
        addOperand(from);
    }

    public Value getTo() {
        return to;
    }

    public Value getFrom() {
        return operandList.get(0);
    }

    @Override
    public String toString() {
        Value to = getTo();
        Value from = getFrom();
        return "move " + to.getType() + " " + to.getName() +
                ", " + from.getType() + " " + from.getName();
    }
}
