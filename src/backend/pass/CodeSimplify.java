package backend.pass;

import backend.mips.Asm;
import backend.mips.MIPSFile;
import backend.mips.text.JInst;
import backend.mips.text.Label;

import java.util.ArrayList;

public class CodeSimplify implements MIPSPass {
    private final MIPSFile mipsFile;

    public CodeSimplify(MIPSFile mipsFile) {
        this.mipsFile = mipsFile;
    }

    @Override
    public void run() {
        jumpSimplify();
    }

    private void jumpSimplify() {
        ArrayList<Asm> asms = mipsFile.getTextWithoutComment();
        ArrayList<Asm> toRemove = new ArrayList<>();
        for (int i = 0; i < asms.size() - 1; i++) {
            Asm asm = asms.get(i);
            if (asm instanceof JInst jInst) {
                Asm nextAsm = asms.get(i + 1);
                // 若j指令的目标是下一行标签，则删除这条j指令
                if (nextAsm instanceof Label label &&
                        label.getName().equals(jInst.getLabel())) {
                    toRemove.add(jInst);
                }
                i++;
            }
        }
        mipsFile.getText().removeAll(toRemove);
    }
}
