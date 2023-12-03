package midend.pass;

import midend.ir.BasicBlock;
import midend.ir.Function;
import midend.ir.Module;
import midend.ir.Value;
import midend.ir.inst.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import static midend.ir.inst.IcmpInst.IcmpKind.*;

public class GVN implements IRPass {
    private final Module module;
    /**
     * 键是某个value，值是该value实际使用的value。
     */
    private final HashMap<Value, Value> valueNumberMap;
    private final HashSet<BasicBlock> visited;
    private final ArrayList<BasicBlock> order;

    /**
     * 全局值标号。Global Value Numbering.
     */
    public GVN() {
        this.module = Module.getInstance();
        this.valueNumberMap = new HashMap<>();
        this.visited = new HashSet<>();
        this.order = new ArrayList<>();
    }

    @Override
    public void run() {
        for (Function function : module.getNotLibFunctions()) {
            runGVNFuc(function);
        }
    }

    private void runGVNFuc(Function function) {
        order.clear();
        visited.clear();
        postOrderDFS(function.getBasicBlocks().get(0)); // 得到后序遍历顺序
        Collections.reverse(order); // 得到逆后序遍历顺序
        // 按逆后序遍历基本块
        for (BasicBlock basicBlock : order) {
            runGVNBlock(basicBlock);
        }
    }

    private void runGVNBlock(BasicBlock basicBlock) {
        ArrayList<Inst> insts = new ArrayList<>(basicBlock.getInsts()); // 先拷贝再遍历
        for (Inst inst : insts) {
            runGVNInst(inst);
        }
    }

    private void runGVNInst(Inst inst) {
        if (!inst.getUserList().isEmpty()) {
            Value simpVal = InstSimplifier.simplify(inst);  // 尝试做常量折叠
            if (simpVal instanceof Inst) {  // 若常量折叠失败，则尝试消除公共子表达式
                simpVal = findNumberByValue(simpVal);
            }
            replaceValue(inst, simpVal);
        }
    }

    /**
     * 使用simpVal替换inst。
     */
    private void replaceValue(Inst inst, Value simpVal) {
        if (inst.equals(simpVal)) {
            return;
        }
        valueNumberMap.remove(inst);
        inst.replaceAllUsesWith(simpVal);
        inst.delThisUserFromAllOperands();
        inst.getParentBlock().getInsts().remove(inst);
    }

    private Value findNumberByValue(Value value) {
        if (valueNumberMap.containsKey(value)) {  // 如果存在同样的value，直接返回其number
            return valueNumberMap.get(value);
        }

        Value number = allocNumber(value);  // 如果不存在，则需要查找等价value，找不到则插入原value
        valueNumberMap.put(value, number);
        return number;
    }

    /**
     * 在valueNumberMap中查找与value等价的value，若找到则返回其number，若未找到返回原value。
     */
    private Value allocNumber(Value value) {
        if (value instanceof BinaryInst binaryInst) {
            return allocNumberForBinaryInst(binaryInst);
        } else if (value instanceof IcmpInst icmpInst) {
            return allocNumberForIcmpInst(icmpInst);
        } else if (value instanceof GEPInst gepInst) {
            return allocNumberForGEPInst(gepInst);
        } else {
            return value;
        }
    }

    /**
     * 对于二元运算指令，若能查到等价指令，则返回对应number。否则返回原指令。
     */
    private Value allocNumberForBinaryInst(BinaryInst a) {
        // a指令操作数的findNumber必须在遍历hashmap前进行，不然就会出现遍历时添加
        Value aop1 = findNumberByValue(a.getOperand1());
        Value aop2 = findNumberByValue(a.getOperand2());
        for (Value value : valueNumberMap.keySet()) {
            // 若存在与a等价的指令b，则返回其number
            if (value instanceof BinaryInst b) {
                Value bop1 = findNumberByValue(b.getOperand1());
                Value bop2 = findNumberByValue(b.getOperand2());
                if (isEquivalentBinaryInst(a, b, aop1, aop2, bop1, bop2)) {
                    return valueNumberMap.get(b);
                }
            }
        }
        // 若查不到，则返回原指令
        return a;
    }

    /**
     * 比较两条binaryInst是否等价。要考虑交换律。
     */
    private boolean isEquivalentBinaryInst(BinaryInst a, BinaryInst b, Value aop1, Value aop2, Value bop1, Value bop2) {
        if (!a.getOpcode().equals(b.getOpcode())) {
            return false;
        }
        // a_b与a_b等价。此外，a+b与b+a等价，a*b与b*a等价。
        return (aop1.equals(bop1) && aop2.equals(bop2) ||
                aop1.equals(bop2) && aop2.equals(bop1) && isCommunicativeBinaryInst(a.getOpcode()));
    }

    /**
     * 加法和乘法满足交换律。
     */
    private boolean isCommunicativeBinaryInst(Opcode opcode) {
        return opcode == Opcode.add || opcode == Opcode.mul;
    }

    /**
     * 对于icmp指令，若能查到等价指令，则返回对应number，否则返回原指令。
     */
    private Value allocNumberForIcmpInst(IcmpInst a) {
        // a指令操作数的findNumber必须在遍历hashmap前进行，不然就会出现遍历时添加
        Value aop1 = findNumberByValue(a.getOperand1());
        Value aop2 = findNumberByValue(a.getOperand2());
        for (Value value : valueNumberMap.keySet()) {
            // 若存在与a等价的指令b，则返回其number
            if (value instanceof IcmpInst b) {
                Value bop1 = findNumberByValue(b.getOperand1());
                Value bop2 = findNumberByValue(b.getOperand2());
                if (isEquivalentIcmpInst(a, b, aop1, aop2, bop1, bop2)) {
                    return valueNumberMap.get(b);
                }
            }
        }
        // 若查不到，则返回原指令
        return a;
    }

    /**
     * 比较两条icmpInst是否等价。
     */
    private boolean isEquivalentIcmpInst(IcmpInst a, IcmpInst b, Value aop1, Value aop2, Value bop1, Value bop2) {
        IcmpInst.IcmpKind kindA = a.getIcmpKind();
        IcmpInst.IcmpKind kindB = b.getIcmpKind();
        if (kindA == kindB) {   // 若比较符一致，a_b与a_b等价。此外，a==b与b==a等价，a!=b与b!=a等价
            return aop1.equals(bop1) && aop2.equals(bop2) ||
                    aop1.equals(bop2) && aop2.equals(bop1) && (kindA == IcmpInst.IcmpKind.eq || kindA == IcmpInst.IcmpKind.ne);
        } else {    // 若比较符不一致，则a>b和b<a等价，a>=b和b<=a等价
            return aop1.equals(bop2) && aop2.equals(bop1) &&
                    (kindA == sgt && kindB == slt ||
                            kindA == slt && kindB == sgt ||
                            kindA == sge && kindB == sle ||
                            kindA == sle && kindB == sge);
        }
    }

    /**
     * 对于gep指令，等价的条件是base与offset等价。
     */
    private Value allocNumberForGEPInst(GEPInst a) {
        for (Value value : valueNumberMap.keySet()) {
            if (value instanceof GEPInst b) {
                if (a.getBasePointer().equals(b.getBasePointer()) && a.getOffset().equals(b.getOffset())) {
                    return b;   // 没必要再取值了，gep指令在valueNumberMap的键和值都是这条gep
                }
            }
        }
        return a;
    }

    /**
     * 后序遍历。
     */
    private void postOrderDFS(BasicBlock basicBlock) {
        visited.add(basicBlock);
        for (BasicBlock suc : basicBlock.getImmDomList()) {
            if (!visited.contains(suc)) {
                postOrderDFS(suc);
            }
        }
        order.add(basicBlock);
    }
}
