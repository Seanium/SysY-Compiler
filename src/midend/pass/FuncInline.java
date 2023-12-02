package midend.pass;

import midend.IRBuilder;
import midend.ir.Module;
import midend.ir.*;
import midend.ir.inst.*;
import midend.ir.type.IntegerType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class FuncInline implements IRPass {
    private final Module module;
    /**
     * 该函数所调用的函数集合。
     */
    private final HashMap<Function, HashSet<Function>> calleesOfFunc;
    /**
     * 调用了该函数的函数集合。
     */
    private final HashMap<Function, HashSet<Function>> callersOfFunc;

    /**
     * 函数内联。
     */
    public FuncInline() {
        this.module = Module.getInstance();
        this.calleesOfFunc = new HashMap<>();
        this.callersOfFunc = new HashMap<>();
    }

    @Override
    public void run() {
        funcInline();
    }

    /**
     * 每次迭代，内联当前调用树的叶子结点函数。
     */
    private void funcInline() {
        boolean change = true;
        while (change) {
            change = false;
            ArrayList<Function> calleesToInline = new ArrayList<>();
            getCallInfo();
            for (Function caller : module.getNotLibFunctions()) {
                for (Function callee : calleesOfFunc.get(caller)) {
                    // 若callee没有调用其他函数，则为调用树叶子结点，可以被内联
                    if (calleesOfFunc.get(callee).isEmpty()) {
                        calleesToInline.add(callee);
                        change = true;
                    }
                }
            }
            for (Function callee : calleesToInline) {
                funcInline(callee);
                // 移除callee
                module.getFunctions().remove(callee);
            }
        }
    }

    /**
     * 得到函数间的调用和被调用信息。
     */
    private void getCallInfo() {
        // 初始化map
        calleesOfFunc.clear();
        callersOfFunc.clear();
        for (Function function : module.getNotLibFunctions()) {
            calleesOfFunc.put(function, new HashSet<>());
            callersOfFunc.put(function, new HashSet<>());
        }
        // 填充map
        for (Function caller : module.getNotLibFunctions()) {
            for (BasicBlock basicBlock : caller.getBasicBlocks()) {
                for (Inst inst : basicBlock.getInsts()) {
                    if (inst instanceof CallInst callInst && !callInst.getTargetFunc().isLib()) {
                        Function callee = callInst.getTargetFunc();
                        calleesOfFunc.get(caller).add(callee);
                        callersOfFunc.get(callee).add(caller);
                    }
                }
            }
        }
    }

    /**
     * 将callee内联到其caller中。
     */
    private void funcInline(Function callee) {
        ArrayList<CallInst> callInsts = new ArrayList<>();
        for (Function caller : callersOfFunc.get(callee)) {
            for (BasicBlock basicBlock : caller.getBasicBlocks()) {
                for (Inst inst : basicBlock.getInsts()) {
                    if (inst instanceof CallInst callInst && callInst.getTargetFunc().equals(callee)) {
                        callInsts.add(callInst);
                    }
                }
            }
        }
        int callId = 0;
        for (CallInst callInst : callInsts) {
            replaceCall(callInst, callId);
            callId++;
        }
    }

    /**
     * 替换call指令。
     * 内联后的基本块顺序应为：..., preBlock(以call结尾), calleeBlocks(被调函数的全部基本块), sucBlock(新建), ...
     */
    private void replaceCall(CallInst callInst, int callId) {
        // 第一步 新建基本块sucBlock，插入到callInst所在的基本块preBlock后面
        BasicBlock preBlock = callInst.getParentBasicBlock();
        Function caller = preBlock.getParentFunction();
        Function callee = callInst.getTargetFunc();
        FuncCopier funcCopier = new FuncCopier();
        Function calleeCopy = funcCopier.copyFunc(callee);   // 克隆callee
        ArrayList<BasicBlock> callerBlocks = caller.getBasicBlocks();
        BasicBlock sucBlock = new BasicBlock(IRBuilder.getInstance().genBasicBlockLabelForFunc(caller), caller);
        callerBlocks.add(callerBlocks.indexOf(preBlock) + 1, sucBlock);

        // 第二步 将preBlock中callInst后面的指令，移动到新建的基本块sucBasicBlock中
        ArrayList<Inst> preBlockInsts = preBlock.getInsts();
        ArrayList<Inst> instsToMove = new ArrayList<>();
        for (int i = preBlockInsts.indexOf(callInst) + 1; i < preBlockInsts.size(); i++) {
            Inst inst = preBlockInsts.get(i);
            inst.setParentBasicBlock(sucBlock);    // 更改指令所属基本块
            instsToMove.add(inst);
        }
        preBlock.getInsts().removeAll(instsToMove);
        sucBlock.getInsts().addAll(instsToMove);

        // 第三步 将callee的全部基本块移到caller的preBlock后面
        ArrayList<BasicBlock> calleeCopyBlocks = calleeCopy.getBasicBlocks();
        for (BasicBlock basicBlock : calleeCopyBlocks) {
            basicBlock.setParentFunction(caller);   // 更改基本块所属函数
        }
        callerBlocks.addAll(callerBlocks.indexOf(preBlock) + 1, calleeCopyBlocks);
        preBlock.getCFGSucList().add(calleeCopyBlocks.get(0));  // 维护前驱后继信息
        calleeCopyBlocks.get(0).getCFGPreList().add(preBlock);

        // 第四步 把calleeCopyBlocks中所有对形参的使用替换成对实参的使用
        ArrayList<Param> params = calleeCopy.getParams();
        ArrayList<Value> args = callInst.getArgs();
        assert params.size() == args.size() : "错误，形参与实参数量不同。";
        for (int i = 0; i < params.size(); i++) {
            params.get(i).replaceAllUsesWith(args.get(i));
        }

        // 第五步 若callee有返回值且call语句有赋值，在sucBlock开头插入phi指令
        if (calleeCopy.getType() == IntegerType.i32) {
            ArrayList<BasicBlock> cfgPreListOfSucBlock = new ArrayList<>();
            ArrayList<Value> options = new ArrayList<>();
            for (BasicBlock basicBlock : calleeCopyBlocks) {
                if (basicBlock.getLastInst() instanceof ReturnInst returnInst) {
                    cfgPreListOfSucBlock.add(basicBlock);
                    options.add(returnInst.getValue());
                }
            }
            PhiInst phiInst = new PhiInst(callInst.getName(), cfgPreListOfSucBlock, options);
            sucBlock.addInstAtFirst(phiInst);
            callInst.replaceAllUsesWith(phiInst);  // 把之后对call的使用全部换成对phi的使用
        }

        // 第六步 将preBlock末尾的call替换为br，把calleeCopy中的ret替换为br到sucBlock
        preBlockInsts.remove(callInst);
        JumpInst jumpInst = new JumpInst(calleeCopyBlocks.get(0));
        preBlockInsts.add(jumpInst);

        for (BasicBlock basicBlock : calleeCopyBlocks) {
            if (basicBlock.getLastInst() instanceof ReturnInst returnInst) {
                basicBlock.getInsts().remove(returnInst);
                JumpInst jumpInst1 = new JumpInst(sucBlock);
                basicBlock.getInsts().add(jumpInst1);
                basicBlock.getCFGSucList().add(sucBlock);   // 维护前驱后继信息
                sucBlock.getCFGPreList().add(preBlock);
            }
        }

        // 第七步 将calleeCopy中的标签名和变量名后面加上"_callId", 避免多次内联的重名问题
        for (BasicBlock basicBlock : calleeCopyBlocks) {
            basicBlock.setName(basicBlock.getName() + "_" + callId);
            for (Inst inst : basicBlock.getInsts()) {
                if (!(inst.getName() == null) && !inst.getName().isEmpty()) {
                    inst.setName(inst.getName() + "_" + callId);
                }
            }
        }
    }
}
