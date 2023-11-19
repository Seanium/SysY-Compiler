package midend;

import midend.pass.BuildCFGPass;
import midend.pass.Pass;
import midend.pass.SimplifyBasicBlockPass;

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
        addPass(new SimplifyBasicBlockPass());
        addPass(new BuildCFGPass());
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
