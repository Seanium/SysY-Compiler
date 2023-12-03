package midend.ir.inst;

import midend.ir.BasicBlock;
import midend.ir.Value;
import midend.ir.type.IntegerType;

import java.util.ArrayList;

public class PhiInst extends Inst {
    private final ArrayList<BasicBlock> cfgPreList;

    /**
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

    /**
     * 用于多返回值函数内联。须确保cfgPreList与options中下标对应。
     */
    public PhiInst(String name, ArrayList<BasicBlock> cfgPreList, ArrayList<Value> options) {
        super(IntegerType.i32, name, Opcode.phi);
        this.cfgPreList = cfgPreList;
        for (Value option : options) {
            addOperand(option);
        }
    }

    public void addOption(Value option, BasicBlock pre) {
        int index = cfgPreList.indexOf(pre);
        if (operandList.get(index) != null) {   // 如果该下标已经有选项，则需要先删除phi作为其user的信息
            operandList.get(index).getUserList().remove(this);
        }
        operandList.set(index, option);
        option.addUser(this);   // 未调用addOperand，需手动addUser
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
