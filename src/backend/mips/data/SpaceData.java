package backend.mips.data;

import backend.mips.Asm;

public class SpaceData extends Asm {
    private final String name;
    private final int byteNum;

    public SpaceData(String name, int byteNum) {
        this.name = name;
        this.byteNum = byteNum;
    }

    @Override
    public String toString() {
        return name + ": .space " + byteNum;
    }
}
