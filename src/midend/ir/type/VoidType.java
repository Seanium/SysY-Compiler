package midend.ir.type;

public class VoidType extends Type {
    public static final VoidType voidType = new VoidType();

    @Override
    public String toString() {
        return "void";
    }
}
