package midend.ir.type;

public class IntegerType extends Type {
    private final int bitWidth;
    public static final IntegerType i32 = new IntegerType(32);

    private IntegerType(int bitWidth) {
        this.bitWidth = bitWidth;
    }

    @Override
    public String toString() {
        if (bitWidth == 32) {
            return "i32";
        } else {
            return null;
        }
    }
}
