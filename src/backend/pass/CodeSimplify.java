package backend.pass;

import backend.mips.Asm;
import backend.mips.MIPSFile;
import backend.mips.text.JInst;
import backend.mips.text.Label;
import backend.mips.text.MoveMIPSInst;

import java.util.ArrayList;

public class CodeSimplify implements MIPSPass {
    private final MIPSFile mipsFile;

    public CodeSimplify(MIPSFile mipsFile) {
        this.mipsFile = mipsFile;
    }

    @Override
    public void run() {
        jumpSimplify();
        moveSimplify();
    }

    /**
     * 相邻块合并，移除冗余j指令。
     */
    private void jumpSimplify() {
        ArrayList<Asm> asms = mipsFile.getTextWithoutComment();
        ArrayList<Asm> toRemove = new ArrayList<>();
        for (int i = 0; i < asms.size() - 1; i++) {
            // 若j指令的目标是下一行标签，则删除这条j指令 即合并相邻块
            if (asms.get(i) instanceof JInst jInst && asms.get(i + 1) instanceof Label label
                    && jInst.getLabel().equals(label.getName())) {
                toRemove.add(jInst);
                i++;
            }
        }
        mipsFile.getText().removeAll(toRemove);
    }

    /**
     * 移除冗余move指令。
     */
    private void moveSimplify() {
        // 删除 move $reg0 $reg0 这样的from和to寄存器相同的指令
        ArrayList<Asm> asms = mipsFile.getTextWithoutComment();
        ArrayList<Asm> toRemove = new ArrayList<>();
        for (Asm asm : asms) {
            if (asm instanceof MoveMIPSInst moveMIPSInst
                    && moveMIPSInst.getFrom().equals(moveMIPSInst.getTo())) {
                toRemove.add(moveMIPSInst);
            }
        }
        mipsFile.getText().removeAll(toRemove);
        toRemove.clear();
        // 1.删除 move $reg1 $reg0; move $reg0 $reg1 这样连续两条指令中的第二条
        // 2.删除 move $reg1 $reg0; move $reg1 $reg0 这样连续两条指令中的第二条
        // 3.删除 move $reg1 $reg0; move $reg1 $reg2 这样连续两条指令中的第一条
        boolean change = true;
        while (change) {
            change = false;
            asms = mipsFile.getTextWithoutComment();
            for (int i = 0; i < asms.size() - 1; i++) {
                if (asms.get(i) instanceof MoveMIPSInst moveMIPSInst0 && asms.get(i + 1) instanceof MoveMIPSInst moveMIPSInst1) {
                    if (moveMIPSInst0.getFrom() == moveMIPSInst1.getTo() && moveMIPSInst0.getTo() == moveMIPSInst1.getFrom() ||
                            moveMIPSInst0.getFrom() == moveMIPSInst1.getFrom() && moveMIPSInst0.getTo() == moveMIPSInst1.getTo()) {
                        change = true;
                        toRemove.add(moveMIPSInst1);
                        i++;
                    } else if (moveMIPSInst0.getTo() == moveMIPSInst1.getTo()) {
                        change = true;
                        toRemove.add(moveMIPSInst0);
                    }
                }
            }
            mipsFile.getText().removeAll(toRemove);
            toRemove.clear();
        }
    }
}
