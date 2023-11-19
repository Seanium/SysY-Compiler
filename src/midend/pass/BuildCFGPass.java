package midend.pass;

import midend.ir.BasicBlock;
import midend.ir.Function;
import midend.ir.Module;
import midend.ir.inst.Inst;

public class BuildCFGPass implements Pass {
    private final Module module;

    public BuildCFGPass() {
        this.module = Module.getInstance();
    }


    @Override
    public void run() {
        buildCFG();
    }

    private void buildCFG() {
        for (Function function : module.getNotLibFunctions()) {
            for (BasicBlock basicBlock : function.getBasicBlocks()) {
                Inst lastInst = basicBlock.getLastInst();

            }
        }
    }
}
