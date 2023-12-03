package midend.pass;

import midend.ir.BasicBlock;
import midend.ir.Function;
import midend.ir.Module;
import utils.Config;

public class LoopDepthAnalyze implements IRPass {
    private final Module module;

    /**
     * 循环深度分析。
     */
    public LoopDepthAnalyze() {
        this.module = Module.getInstance();
    }

    @Override
    public void run() {
        for (Function function : module.getNotLibFunctions()) {
            loopDepthAnalyze(function);
        }
        if (Config.getMode() == Config.Mode.DEBUG) {
            printLoopDepth();
        }
    }

    /**
     * 遍历基本块，设置循环深度
     */
    private void loopDepthAnalyze(Function function) {
        int loopDepth = 0;
        for (BasicBlock basicBlock : function.getBasicBlocks()) {
            if (basicBlock.isLoopHead()) {      // 进入循环，深度++
                loopDepth++;
            }
            basicBlock.setLoopDepth(loopDepth); // 设置深度
            if (basicBlock.isLoopTail()) {      // 退出循环，深度--
                loopDepth--;
            }
        }
    }

    private void printLoopDepth() {
        StringBuilder loopDepthInfo = new StringBuilder();
        for (Function function : module.getNotLibFunctions()) {
            loopDepthInfo.append(function.getName()).append(" [loopDepth of basicBlock]\n");
            for (BasicBlock basicBlock : function.getBasicBlocks()) {
                loopDepthInfo.append(basicBlock.getName()).append(": ").append(basicBlock.getLoopDepth()).append("\n");
            }
            loopDepthInfo.append("\n");
        }
        System.out.println(loopDepthInfo);
    }
}
