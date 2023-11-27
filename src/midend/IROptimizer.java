package midend;

import midend.ir.Module;
import midend.pass.*;
import utils.FileIO;

public class IROptimizer {
    private static IROptimizer instance;

    public static IROptimizer getInstance() {
        if (instance == null) {
            instance = new IROptimizer();
        }
        return instance;
    }

    private final Module module;

    private IROptimizer() {
        this.module = Module.getInstance();
    }


    public void runPasses() {
        new BlockSimplify().run();
        new DFBuild().run();
        new Mem2Reg().run();
        new DeadCodeRemove().run();
        FileIO.write("llvm_ir.txt", module.toString());
        new PhiRemove().run();
        new LivenessAnalyze().run();
        new RegAlloc().run();
    }
}
