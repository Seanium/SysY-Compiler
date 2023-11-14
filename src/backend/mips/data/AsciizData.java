package backend.mips.data;

import backend.mips.Asm;

public class AsciizData extends Asm {
    private final String name;
    private final String content;

    /***
     * 字符串。
     * @param name  字符串名。请传入以.开头的名字。
     * @param content 字符串内容。请去掉末尾的\0。
     */
    public AsciizData(String name, String content) {
        this.name = name;
        this.content = content;
    }

    @Override
    public String toString() {
        return name + ": .asciiz \"" + content.replace("\n", "\\n") + "\"";
    }
}
