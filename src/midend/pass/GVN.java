package midend.pass;

import midend.ir.Module;

public class GVN implements IRPass {
    private final Module module;

    public GVN() {
        this.module = Module.getInstance();
    }

    @Override
    public void run() {

    }
}
