package midend.pass;

import midend.ir.*;
import midend.ir.inst.*;

import java.util.ArrayList;
import java.util.HashMap;

public class FuncCopier {
    /**
     * 源函数的value到新函数的value的映射。
     */
    private final HashMap<Value, Value> copyMap = new HashMap<>();
    private final ArrayList<PhiInst> srcPhiInsts = new ArrayList<>();

    public Function copyFunc(Function srcFunc) {
        Function copyFunc = new Function(srcFunc.getName().substring(1), srcFunc.getType());
        // 拷贝形参
        for (Param srcParam : srcFunc.getParams()) {
            Param param = new Param(srcParam.getType(), srcParam.getName());
            copyFunc.getParams().add(param);    // 添加形参到函数
            copyMap.put(srcParam, param);   // 添加形参到copyMap
        }
        // 先构建空块
        ArrayList<BasicBlock> srcBlocks = srcFunc.getBasicBlocks();
        ArrayList<BasicBlock> copyBlocks = copyFunc.getBasicBlocks();
        for (BasicBlock srcBlock : srcBlocks) {
            BasicBlock copyBlock = new BasicBlock(srcBlock.getName(), copyFunc);
            copyBlocks.add(copyBlock);   // 添加基本块到函数
            copyBlock.setParentFunction(copyFunc);  // 设置基本块的所属函数
            copyMap.put(srcBlock, copyBlock);   // 添加基本块到copyMap
        }
        // 维护前驱后继关系
        for (BasicBlock srcBlock : srcBlocks) {
            BasicBlock copyBlock = (BasicBlock) copyMap.get(srcBlock);
            for (BasicBlock srcSuc : srcBlock.getCFGSucList()) {
                if (copyMap.get(srcSuc) != null) {  // 只加入函数内部块的后继信息
                    copyBlock.getCFGSucList().add((BasicBlock) copyMap.get(srcSuc));
                }
            }
            for (BasicBlock srcPre : srcBlock.getCFGPreList()) {
                if (copyMap.get(srcPre) != null) {
                    copyBlock.getCFGPreList().add((BasicBlock) copyMap.get(srcPre));
                }
            }
        }
        // 按顺序拷贝指令
        for (BasicBlock srcBlock : srcBlocks) {
            for (Inst srcInst : srcBlock.getInsts()) {
                Inst copyInst = copyInst(srcInst);
                BasicBlock copyBlock = (BasicBlock) copyMap.get(srcBlock);
                copyBlock.getInsts().add(copyInst); // 添加指令到基本块
                copyInst.setParentBasicBlock(copyBlock);    // 设置所属基本块
                copyMap.put(srcInst, copyInst); // 添加指令到copyMap
            }
        }
        // 拷贝phi的option
        for (PhiInst srcPhiInst : srcPhiInsts) {
            PhiInst copyPhiInst = (PhiInst) copyMap.get(srcPhiInst);
            for (int i = 0; i < srcPhiInst.getOperandList().size(); i++) {
                BasicBlock copyPre = copyPhiInst.getCfgPreList().get(i);
                Value copyOption = findValue(srcPhiInst.getOperandList().get(i));
                copyPhiInst.addOption(copyOption, copyPre);
            }
        }
        return copyFunc;
    }

    private Value findValue(Value srcValue) {
        if (srcValue instanceof GlobalVar || srcValue instanceof GlobalArray
                || srcValue instanceof Constant || srcValue instanceof Function) {
            return srcValue;
        } else {
            assert copyMap.containsKey(srcValue) : "错误，srcValue不存在于valueMap的键!";
            assert copyMap.get(srcValue) != null : "错误，srcValue在valueMap中对应的值为null!";
            return copyMap.get(srcValue);
        }
    }

    private Inst copyInst(Inst srcInst) {
        Inst copyInst = null;
        if (srcInst instanceof AllocaInst allocaInst) {
            copyInst = new AllocaInst(allocaInst.getName(), allocaInst.getTargetType());
        } else if (srcInst instanceof BinaryInst binaryInst) {
            copyInst = new BinaryInst(binaryInst.getOpcode(), binaryInst.getName(), findValue(binaryInst.getOperand1()), findValue(binaryInst.getOperand2()));
        } else if (srcInst instanceof BranchInst branchInst) {
            copyInst = new BranchInst(findValue(branchInst.getCond()),
                    (BasicBlock) findValue(branchInst.getTrueBlock()), (BasicBlock) findValue(branchInst.getFalseBlock()));
        } else if (srcInst instanceof CallInst callInst) {
            ArrayList<Value> copyArgs = new ArrayList<>();
            for (Value srcArg : callInst.getArgs()) {
                copyArgs.add(findValue(srcArg));
            }
            copyInst = new CallInst(callInst.getName(), callInst.getTargetFunc(), copyArgs);
        } else if (srcInst instanceof GEPInst gepInst) {
            copyInst = new GEPInst(gepInst.getType(), gepInst.getName(),
                    findValue(gepInst.getBasePointer()), findValue(gepInst.getOffset()));
        } else if (srcInst instanceof IcmpInst icmpInst) {
            copyInst = new IcmpInst(icmpInst.getName(), icmpInst.getIcmpKind(),
                    findValue(icmpInst.getOperand1()), findValue(icmpInst.getOperand2()));
        } else if (srcInst instanceof JumpInst jumpInst) {
            copyInst = new JumpInst((BasicBlock) findValue(jumpInst.getTargetBasicBlock()));
        } else if (srcInst instanceof LoadInst loadInst) {
            copyInst = new LoadInst(loadInst.getName(), findValue(loadInst.getPointer()));
        } else if (srcInst instanceof MoveInst moveInst) {
            copyInst = new MoveInst(findValue(moveInst.getTo()), findValue(moveInst.getFrom()));
        } else if (srcInst instanceof PhiInst phiInst) {
            ArrayList<BasicBlock> cfgPreList = new ArrayList<>();
            for (BasicBlock basicBlock : phiInst.getCfgPreList()) {
                cfgPreList.add((BasicBlock) findValue(basicBlock));
            }
            copyInst = new PhiInst(phiInst.getName(), cfgPreList);
            srcPhiInsts.add(phiInst);   // 先加入列表，最后再填充phi的options
        } else if (srcInst instanceof ReturnInst returnInst) {
            if (returnInst.getValue() == null) {    // return; 无返回值
                copyInst = new ReturnInst(null);
            } else {    // 有返回值
                copyInst = new ReturnInst(findValue(returnInst.getValue()));
            }
        } else if (srcInst instanceof StoreInst storeInst) {
            copyInst = new StoreInst(findValue(storeInst.getFrom()), findValue(storeInst.getTo()));
        } else if (srcInst instanceof ZextInst zextInst) {
            copyInst = new ZextInst(zextInst.getName(), findValue(zextInst.getOriValue()), zextInst.getType());
        }
        return copyInst;
    }
}
