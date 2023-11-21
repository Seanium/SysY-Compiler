package midend;

import midend.pass.*;

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

    private IROptimizer() {
        this.passes = new ArrayList<>();
        addPass(new BlockTailSimplifyPass());
        addPass(new BuildDFPass());
        addPass(new Mem2RegPass());
        addPass(new DeadCodeEmitPass());
    }

    private void addPass(Pass pass) {
        passes.add(pass);
    }

    public void runPasses() {
        for (Pass pass : passes) {
            pass.run();
        }
    }
}
