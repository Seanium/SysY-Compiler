package midend.pass;

import midend.ir.Constant;
import midend.ir.Value;
import midend.ir.inst.*;
import midend.ir.type.IntegerType;

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
        if (op1 instanceof Constant constant1 && op2 instanceof Constant constant2) {
            return new Constant(IntegerType.i32, constant1.getValue() + constant2.getValue());
        }
        return inst;
    }

    private static Value simplifySub(Inst inst) {
        BinaryInst subInst = (BinaryInst) inst;
        Value op1 = subInst.getOperand1();
        Value op2 = subInst.getOperand2();
        if (op1 instanceof Constant constant1 && op2 instanceof Constant constant2) {
            return new Constant(IntegerType.i32, constant1.getValue() - constant2.getValue());
        }
        return inst;
    }

    private static Value simplifyMul(Inst inst) {
        BinaryInst mulInst = (BinaryInst) inst;
        Value op1 = mulInst.getOperand1();
        Value op2 = mulInst.getOperand2();
        if (op1 instanceof Constant constant1 && op2 instanceof Constant constant2) {
            return new Constant(IntegerType.i32, constant1.getValue() * constant2.getValue());
        }
        return inst;
    }

    private static Value simplifySdiv(Inst inst) {
        BinaryInst sdivInst = (BinaryInst) inst;
        Value op1 = sdivInst.getOperand1();
        Value op2 = sdivInst.getOperand2();
        if (op1 instanceof Constant constant1 && op2 instanceof Constant constant2) {
            if (constant2.getValue() != 0) {
                return new Constant(IntegerType.i32, constant1.getValue() / constant2.getValue());
            }
        }
        return inst;
    }

    private static Value simplifySrem(Inst inst) {
        BinaryInst sremInst = (BinaryInst) inst;
        Value op1 = sremInst.getOperand1();
        Value op2 = sremInst.getOperand2();
        if (op1 instanceof Constant constant1 && op2 instanceof Constant constant2) {
            if (constant2.getValue() != 0) {
                return new Constant(IntegerType.i32, constant1.getValue() % constant2.getValue());
            }
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
