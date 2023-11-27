package backend.mips;

import backend.mips.text.Comment;

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

    /***
     * 获得text中的非注释部分。
     */
    public ArrayList<Asm> getTextWithoutComment() {
        ArrayList<Asm> textWithoutComment = new ArrayList<>();
        for (Asm asm : text) {
            if (asm instanceof Comment) {
                continue;
            }
            textWithoutComment.add(asm);
        }
        return textWithoutComment;
    }

    public void addAsmToData(Asm asm) {
        data.add(asm);
    }

    public void addAsmToText(Asm asm) {
        text.add(asm);
    }

    public ArrayList<Asm> getText() {
        return text;
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
