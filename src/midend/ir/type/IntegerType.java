package midend.ir.type;

public class IntegerType extends Type {
    private final int bitWidth;
    public static final IntegerType i32 = new IntegerType(32);
    public static final IntegerType i8 = new IntegerType(8);
    public static final IntegerType i1 = new IntegerType(1);

    private IntegerType(int bitWidth) {
        this.bitWidth = bitWidth;
    }

    @Override
    public String toString() {
        return "i" + bitWidth;
    }
}
