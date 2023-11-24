package midend.ir.inst;

import midend.ir.BasicBlock;
import midend.ir.Value;
import midend.ir.type.IntegerType;

import java.util.ArrayList;

public class PhiInst extends Inst {
    private final ArrayList<BasicBlock> cfgPreList;

    /***
     * cfgPreList中的基本块一定不会重复，而operandList中的Value有可能重复，
     * 因此如果用Value来查找其在operandList的下标，再用此下标找对应的preBlock是错的，只找到了第一个该Value的下标。
     * 只能用operand的下标获取operand对应的preBlock，因为二者之间是多对一映射。
     */
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

    public ArrayList<BasicBlock> getCfgPreList() {
        return cfgPreList;
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
