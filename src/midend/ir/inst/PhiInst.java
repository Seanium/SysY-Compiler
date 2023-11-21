package midend.ir.inst;

import midend.ir.BasicBlock;
import midend.ir.Value;
import midend.ir.type.IntegerType;

import java.util.ArrayList;

public class PhiInst extends Inst {
    private final ArrayList<BasicBlock> cfgPreList;

    public PhiInst(String name, ArrayList<BasicBlock> cfgPreList) {
        super(IntegerType.i32, name, Opcode.phi);
        this.cfgPreList = cfgPreList;
        for (int i = 0; i < cfgPreList.size(); i++) {
            addOperand(null);
        }
    }

    public void addOption(Value value, BasicBlock pre) {
        int index = cfgPreList.indexOf(pre);
        operandList.set(index, value);
        value.addUser(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append(" = phi ").append(type).append(" ");
        for (int i = 0; i < cfgPreList.size(); i++) {
            sb.append("[ ").append(operandList.get(i).getName()).append(", %").append(cfgPreList.get(i).getName()).append(" ]");
            if (i != cfgPreList.size() - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }
}
