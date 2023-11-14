package midend.ir;

import midend.ir.type.PointerType;
import midend.ir.type.Type;

public class GlobalVar extends User {   // todo 可能不需要继承User

    private final Constant initValue;   // 在符号表中也存有初值。此处的属性是用于toString。
    private final boolean isConst;

    /***
     * 非数组的全局常量，非数组的全局变量。
     * @param targetType 右值(指针类型)解引用后的类型，即要存储的变量类型
     * @param name 右值名
     * @param initValue 全局变量初始值，一定非空。
     * @param isConst 是否为全局常量
     */
    public GlobalVar(Type targetType, String name, Constant initValue, boolean isConst) {
        super(new PointerType(targetType), "@" + name);  // type 是其右值类型，为指针类型
        this.initValue = initValue;
        this.isConst = isConst;
    }

    public Constant getInitValue() {
        return initValue;
    }

    @Override
    public String toString() {
        if (isConst) {  // 全局常量
            return name + " = dso_local constant i32 " + initValue.getName();
        } else {    // 全局变量
            return name + " = dso_local global i32 " + initValue.getName();
        }
    }
}
