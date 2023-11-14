package backend.mips.text;

import backend.mips.Asm;

public class Comment extends Asm {
    private final String content;

    public Comment(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "\n#--- " + content;
    }
}
