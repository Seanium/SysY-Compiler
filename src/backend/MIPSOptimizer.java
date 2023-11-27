package backend;

import backend.mips.MIPSFile;
import backend.pass.CodeSimplify;

public class MIPSOptimizer {
    private final MIPSFile mipsFile;

    public MIPSOptimizer(MIPSFile mipsFile) {
        this.mipsFile = mipsFile;
    }

    public void runPasses() {
        new CodeSimplify(mipsFile).run();
    }
}
