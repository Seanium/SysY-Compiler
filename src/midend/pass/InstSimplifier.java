package midend.pass;

import midend.ir.Constant;
import midend.ir.Value;
import midend.ir.inst.*;
import midend.ir.type.IntegerType;

/**
 * 运算指令简化，包括常量折叠等。
 */
public class InstSimplifier {
    public static Value simplify(Inst inst) {
        if (inst.getOpcode() == Opcode.add) {
            return simplifyAdd(inst);
        } else if (inst.getOpcode() == Opcode.sub) {
            return simplifySub(inst);
        } else if (inst.getOpcode() == Opcode.mul) {
            return simplifyMul(inst);
        } else if (inst.getOpcode() == Opcode.sdiv) {
            return simplifySdiv(inst);
        } else if (inst.getOpcode() == Opcode.srem) {
            return simplifySrem(inst);
        } else if (inst.getOpcode() == Opcode.icmp) {
            return simplifyIcmp(inst);
        } else if (inst.getOpcode() == Opcode.zext) {
            return simplifyZext(inst);
        } else {
            return inst;
        }
    }

    private static Value simplifyAdd(Inst inst) {
        BinaryInst addInst = (BinaryInst) inst;
        Value op1 = addInst.getOperand1();
        Value op2 = addInst.getOperand2();
        // 常量折叠
        if (op1 instanceof Constant constant1 && op2 instanceof Constant constant2) {
            return new Constant(IntegerType.i32, constant1.getValue() + constant2.getValue());
        }
        // 常数作为第二个操作数
        if (op1 instanceof Constant) {
            addInst.swapOperand();
            op1 = addInst.getOperand1();
            op2 = addInst.getOperand2();
        }
        // a+0 = a
        if (op2 instanceof Constant constant && constant.getValue() == 0) {
            return op1;
        }
        // a+(0-a) = 0
        if (op2 instanceof BinaryInst binaryInst && binaryInst.getOpcode() == Opcode.sub &&
                binaryInst.getOperand1() instanceof Constant constant && constant.getValue() == 0 &&
                op1.equals(binaryInst.getOperand2())) {
            return new Constant(IntegerType.i32, 0);
        }
        // (0-a)+a = 0
        if (op1 instanceof BinaryInst binaryInst && binaryInst.getOpcode() == Opcode.sub &&
                binaryInst.getOperand1() instanceof Constant constant && constant.getValue() == 0 &&
                binaryInst.getOperand2().equals(op2)) {
            return new Constant(IntegerType.i32, 0);
        }
        // (a-b)+(b-a) = 0
        if (op1 instanceof BinaryInst binaryInst1 && binaryInst1.getOpcode() == Opcode.sub &&
                op2 instanceof BinaryInst binaryInst2 && binaryInst2.getOpcode() == Opcode.sub &&
                binaryInst1.getOperand1().equals(binaryInst2.getOperand2()) &&
                binaryInst1.getOperand2().equals(binaryInst2.getOperand1())) {
            return new Constant(IntegerType.i32, 0);
        }
        // a+(b-a) = b
        if (op2 instanceof BinaryInst binaryInst && binaryInst.getOpcode() == Opcode.sub &&
                op1.equals(binaryInst.getOperand2())) {
            return binaryInst.getOperand1();
        }
        // (b-a)+a = b
        if (op1 instanceof BinaryInst binaryInst && binaryInst.getOpcode() == Opcode.sub &&
                binaryInst.getOperand2().equals(op2)) {
            return binaryInst.getOperand1();
        }
        return inst;
    }

    private static Value simplifySub(Inst inst) {
        BinaryInst subInst = (BinaryInst) inst;
        Value op1 = subInst.getOperand1();
        Value op2 = subInst.getOperand2();
        // 常量折叠
        if (op1 instanceof Constant constant1 && op2 instanceof Constant constant2) {
            return new Constant(IntegerType.i32, constant1.getValue() - constant2.getValue());
        }
        // a-0 = a
        if (op2 instanceof Constant constant && constant.getValue() == 0) {
            return op1;
        }
        // a-a = 0
        if (op1.equals(op2)) {
            return new Constant(IntegerType.i32, 0);
        }
        // a-(a-b) = b
        if (op2 instanceof BinaryInst binaryInst && binaryInst.getOpcode() == Opcode.sub &&
                op1.equals(binaryInst.getOperand1())) {
            return binaryInst.getOperand2();
        }
        // (a+b)-a = b
        if (op1 instanceof BinaryInst binaryInst && binaryInst.getOpcode() == Opcode.add &&
                binaryInst.getOperand1().equals(op2)) {
            return binaryInst.getOperand2();
        }
        // (a+b)-b = a
        if (op1 instanceof BinaryInst binaryInst && binaryInst.getOpcode() == Opcode.add &&
                binaryInst.getOperand2().equals(op2)) {
            return binaryInst.getOperand1();
        }
        return inst;
    }

    private static Value simplifyMul(Inst inst) {
        BinaryInst mulInst = (BinaryInst) inst;
        Value op1 = mulInst.getOperand1();
        Value op2 = mulInst.getOperand2();
        // 常量折叠
        if (op1 instanceof Constant constant1 && op2 instanceof Constant constant2) {
            return new Constant(IntegerType.i32, constant1.getValue() * constant2.getValue());
        }
        // 常数作为第二个操作数
        if (op1 instanceof Constant) {
            mulInst.swapOperand();
            op1 = mulInst.getOperand1();
            op2 = mulInst.getOperand2();
        }
        // a*0 = 0;
        if (op2 instanceof Constant constant && constant.getValue() == 0) {
            return new Constant(IntegerType.i32, 0);
        }
        // a*1 = a;
        if (op2 instanceof Constant constant && constant.getValue() == 1) {
            return op1;
        }
        return inst;
    }

    private static Value simplifySdiv(Inst inst) {
        BinaryInst sdivInst = (BinaryInst) inst;
        Value op1 = sdivInst.getOperand1();
        Value op2 = sdivInst.getOperand2();
        // 常量折叠
        if (op1 instanceof Constant constant1 && op2 instanceof Constant constant2) {
            if (constant2.getValue() != 0) {
                return new Constant(IntegerType.i32, constant1.getValue() / constant2.getValue());
            }
        }
        // 0/a = 0
        if (op1 instanceof Constant constant && constant.getValue() == 0) {
            return new Constant(IntegerType.i32, 0);
        }
        // a/a = 1
        if (op1.equals(op2)) {
            return new Constant(IntegerType.i32, 1);
        }
        // a/1 = a
        if (op2 instanceof Constant constant && constant.getValue() == 1) {
            return op1;
        }
        // (a*b)/b = a
        if (op1 instanceof BinaryInst binaryInst && binaryInst.getOpcode() == Opcode.mul &&
                binaryInst.getOperand2().equals(op2)) {
            return binaryInst.getOperand1();
        }
        return inst;
    }

    private static Value simplifySrem(Inst inst) {
        BinaryInst sremInst = (BinaryInst) inst;
        Value op1 = sremInst.getOperand1();
        Value op2 = sremInst.getOperand2();
        // 常量折叠
        if (op1 instanceof Constant constant1 && op2 instanceof Constant constant2) {
            if (constant2.getValue() != 0) {
                return new Constant(IntegerType.i32, constant1.getValue() % constant2.getValue());
            }
        }
        // 0%a = 0
        if (op1 instanceof Constant constant && constant.getValue() == 0) {
            return new Constant(IntegerType.i32, 0);
        }
        // a%a = 0
        if (op1.equals(op2)) {
            return new Constant(IntegerType.i32, 0);
        }
        // a%1 = 0
        if (op2 instanceof Constant constant && constant.getValue() == 1) {
            return new Constant(IntegerType.i32, 0);
        }
        return inst;
    }

    private static Value simplifyIcmp(Inst inst) {
        IcmpInst icmpInst = (IcmpInst) inst;
        Value op1 = icmpInst.getOperand1();
        Value op2 = icmpInst.getOperand2();
        IcmpInst.IcmpKind icmpKind = icmpInst.getIcmpKind();
        if (op1 instanceof Constant constant1 && op2 instanceof Constant constant2) {
            int v1 = constant1.getValue();
            int v2 = constant2.getValue();
            int cond;
            if (icmpKind == IcmpInst.IcmpKind.eq) {
                cond = v1 == v2 ? 1 : 0;
            } else if (icmpKind == IcmpInst.IcmpKind.ne) {
                cond = v1 != v2 ? 1 : 0;
            } else if (icmpKind == IcmpInst.IcmpKind.sgt) {
                cond = v1 > v2 ? 1 : 0;
            } else if (icmpKind == IcmpInst.IcmpKind.sge) {
                cond = v1 >= v2 ? 1 : 0;
            } else if (icmpKind == IcmpInst.IcmpKind.slt) {
                cond = v1 < v2 ? 1 : 0;
            } else {
                assert icmpKind == IcmpInst.IcmpKind.sle;
                cond = v1 <= v2 ? 1 : 0;
            }
            return new Constant(IntegerType.i1, cond);
        }
        // a==a = 1
        if (icmpKind == IcmpInst.IcmpKind.eq && op1.equals(op2)) {
            return new Constant(IntegerType.i1, 1);
        }
        return inst;
    }

    private static Value simplifyZext(Inst inst) {
        ZextInst zextInst = (ZextInst) inst;
        Value oriValue = zextInst.getOriValue();
        if (oriValue instanceof Constant constant) {
            return new Constant(IntegerType.i32, constant.getValue());
        }
        return inst;
    }
}
