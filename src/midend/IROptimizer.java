package midend;

import midend.ir.Module;
import midend.pass.*;
import utils.FileIO;

import java.util.ArrayList;

public class IROptimizer {
    private static IROptimizer instance;

    public static IROptimizer getInstance() {
        if (instance == null) {
            instance = new IROptimizer();
        }
        return instance;
    }

    private final ArrayList<Pass> passes;
    private final Module module;

    private IROptimizer() {
        this.passes = new ArrayList<>();
        this.module = Module.getInstance();
        addPass(new BlockSimplify());
        addPass(new DFBuild());
        addPass(new Mem2Reg());
        addPass(new DeadCodeRemove());
        addPass(new PhiRemove());
        addPass(new LivenessAnalyze());
//        addPass(new RegAllocator());
    }

    private void addPass(Pass pass) {
        passes.add(pass);
    }

    public void runPasses() {
        for (Pass pass : passes) {
            pass.run();
            // 在消除phi之前，保存llvm_ir
            if (pass instanceof DeadCodeRemove) {
                FileIO.write("llvm_ir.txt", module.toString());
            }
        }
    }
}
