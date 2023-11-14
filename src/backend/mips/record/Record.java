package backend.mips.record;

import backend.mips.Reg;
import midend.ir.LocalArray;
import midend.ir.Value;
import midend.ir.inst.AllocaInst;
import midend.ir.type.ArrayType;

import java.util.HashMap;

public class Record {
    private final String funcName;
    private final HashMap<Value, Integer> valueOffsetMap;
    private final HashMap<Reg, Integer> regOffsetMap;
    private int curOffset;

    public String getFuncName() {
        return funcName;
    }

    public Record(String funcName) {
        this.funcName = funcName;
        this.valueOffsetMap = new HashMap<>();
        this.regOffsetMap = new HashMap<>();
        curOffset = 0;
    }

    /***
     * 添加value到记录，并返回其offset。
     */
    public int addValue(Value value) {
        int offset = curOffset;
        valueOffsetMap.put(value, curOffset);
        if (value instanceof LocalArray localArray) {   // 数组要算入全部元素
            curOffset -= localArray.getLen() * 4;
        } else {    // 非数组
            curOffset -= 4;
        }
        return offset;
    }

    /***
     * 添加value到记录，指定其offset。用于解析zext命令。
     */
    public void addValueWithOffset(Value value, int offset) {
        valueOffsetMap.put(value, offset);
    }

    /***
     * 查找value的offset。
     */
    public int getOffsetOfValue(Value value) {
        return valueOffsetMap.get(value);
    }

    /***
     * 添加reg到记录，并返回其offset。
     */
    public int addReg(Reg reg) {
        int offset = curOffset;
        regOffsetMap.put(reg, curOffset);
        curOffset -= 4;
        return offset;
    }

    /***
     * 查找reg的offset。
     */
    public int getOffsetOfReg(Reg reg) {
        return regOffsetMap.get(reg);
    }

    /***
     * 向record中插入数组，并返回数组首元素地址。
     * @param allocaInst 分配数组指令。
     * @return 数组首元素sp偏移量。
     */
    public int insertArray(AllocaInst allocaInst) {
        ArrayType arrayType = (ArrayType) allocaInst.getTargetType();
        int len = arrayType.getLen();
        curOffset -= len * 4;
        return curOffset + 4;
    }

    public int getCurOffset() {
        return curOffset;
    }
}
