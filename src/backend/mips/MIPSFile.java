package backend.mips;

import java.util.ArrayList;

public class MIPSFile {

    public MIPSFile() {
        this.data = new ArrayList<>();
        this.text = new ArrayList<>();
    }

    /***
     * 数据段
     */
    private final ArrayList<Asm> data;
    /***
     * 代码段
     */
    private final ArrayList<Asm> text;

    public void addAsmToData(Asm asm) {
        data.add(asm);
    }

    public void addAsmToText(Asm asm) {
        text.add(asm);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(".data\n");
        for (Asm asm : data) {
            sb.append(asm.toString()).append("\n");
        }
        sb.append(".text\n");
        for (Asm asm : text) {
            sb.append(asm.toString()).append("\n");
        }
        return sb.toString();
    }
}
