package midend.pass;

import midend.ir.BasicBlock;
import midend.ir.Function;
import midend.ir.Module;
import midend.ir.inst.Inst;

public class RegAllocate implements Pass {
    private final Module module;

    public RegAllocate() {
        this.module = Module.getInstance();
    }

    @Override
    public void run() {
        for (Function function : module.getNotLibFunctions()) {

        }
    }


}
