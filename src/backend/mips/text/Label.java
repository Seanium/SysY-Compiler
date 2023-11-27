package backend.mips.text;

import backend.mips.Asm;

public class Label extends Asm {
    private final String name;

    public Label(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name + ":";
    }
}
