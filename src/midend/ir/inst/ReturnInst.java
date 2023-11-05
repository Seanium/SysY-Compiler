package midend.ir.inst;

import midend.ir.Value;
import midend.ir.type.VoidType;

public class ReturnInst extends Inst {
    // ret void             value == null
    // ret <type> <value>   value != null
    private final Value value;

    /***
     * 若为return;请传入null。
     */
    public ReturnInst(Value value) {
        super(VoidType.voidType, "", Opcode.ret);    // 不需要name，因为不会作为右值被引用; 同理，右值类型为void即可
        this.opcode = Opcode.ret;
        this.value = value;
        if (value != null) {
            addOperand(value);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (value == null) {    // ret void             value == null
            sb.append("ret void");
        } else {                // ret <type> <value>   value != null
            sb.append("ret i32 ").append(value.getName());
        }
        return sb.toString();
    }
}
