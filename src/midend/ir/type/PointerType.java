package midend.ir.type;

public class PointerType extends Type {
    private final Type targetType;

    public PointerType(Type targetType) {
        this.targetType = targetType;
    }

    @Override
    public String toString() {
        return targetType + "*";
    }

    public Type getTargetType() {
        return targetType;
    }
}
