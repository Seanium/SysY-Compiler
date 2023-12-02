package midend.pass;

import midend.ir.BasicBlock;
import midend.ir.Function;
import midend.ir.Module;
import midend.ir.inst.CallInst;
import midend.ir.inst.Inst;
import midend.ir.inst.StoreInst;

import java.util.HashSet;

public class FuncSideEffectAnalyze implements IRPass {
    private final Module module;
    private final HashSet<Function> analyzed;

    /**
     * 函数副作用分析。有副作用的函数包括: 1.库函数 2.有store指令的函数 3.调用了有副作用函数的函数
     */
    public FuncSideEffectAnalyze() {
        this.module = Module.getInstance();
        this.analyzed = new HashSet<>();
    }

    @Override
    public void run() {
        sideEffectInit();
        sideEffectAnalyze();
    }

    /**
     * 初始化有副作用的函数。共有4种情况。
     */
    private void sideEffectInit() {
        for (Function function : module.getFunctions()) {
            function.getCallees().clear();
            function.setHasSideEffect(false);   // 先初始化为无副作用
            // 如果是库函数，则有副作用
            if (function.isLib()) {
                function.setHasSideEffect(true);
                analyzed.add(function);
                continue;
            }
            for (BasicBlock basicBlock : function.getBasicBlocks()) {
                if (function.hasSideEffect()) {
                    break;
                }
                for (Inst inst : basicBlock.getInsts()) {
                    if (inst instanceof CallInst callInst) {
                        function.addCallee(callInst.getTargetFunc());   // 维护调用信息
                        if (callInst.getTargetFunc().isLib()) { // 如果调用了库函数，则有副作用
                            function.setHasSideEffect(true);
                            analyzed.add(function);
                            break;
                        }
                    } else if (inst instanceof StoreInst) { // 如果有store指令，则有副作用
                        function.setHasSideEffect(true);
                        analyzed.add(function);
                        break;
                    }
                }
            }
        }
    }

    /**
     * 迭代对所有函数进行副作用分析。
     */
    private void sideEffectAnalyze() {
        boolean change = true;
        while (change) {
            change = false;
            for (Function function : module.getFunctions()) {
                if (analyzed.contains(function)) {  // 已分析，跳过
                    continue;
                }
                for (Function callee : function.getCallees()) {
                    // 若该函数调用了有副作用的函数，则有副作用
                    if (callee.hasSideEffect()) {
                        function.setHasSideEffect(true);
                        analyzed.add(function);
                        change = true;
                        break;
                    }
                }
            }
        }
    }
}
