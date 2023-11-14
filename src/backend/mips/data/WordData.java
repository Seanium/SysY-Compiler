package backend.mips.data;

import backend.mips.Asm;

import java.util.ArrayList;

public class WordData extends Asm {
    private final String name;
    private final ArrayList<Integer> values;

    /***
     * .word
     * @param name 标签名。
     * @param values 值列表。请传入逆序后的初值。
     */
    public WordData(String name, ArrayList<Integer> values) {
        this.name = name;
        this.values = values;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append(": .word ");
        for (int i = 0; i < values.size(); i++) {
            sb.append(values.get(i));
            if (i != values.size() - 1) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }
}
