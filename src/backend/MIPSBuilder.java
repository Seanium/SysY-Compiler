package backend;

import backend.mips.Asm;
import backend.mips.MIPSFile;
import backend.mips.Reg;
import backend.mips.data.AsciizData;
import backend.mips.data.SpaceData;
import backend.mips.data.WordData;
import backend.mips.record.Record;
import backend.mips.record.RecordManager;
import backend.mips.text.Comment;
import midend.ir.Value;
import midend.ir.inst.AllocaInst;

public class MIPSBuilder {

    private final MIPSFile mipsFile;
    private final RecordManager recordManager;
    private Record curRecord;   // 当前所在的函数记录

    public MIPSBuilder() {
        this.mipsFile = new MIPSFile();
        this.recordManager = new RecordManager();
    }

    public MIPSFile getMipsFile() {
        return mipsFile;
    }

    /***
     * 根据汇编成分，添加到数据段或代码段。
     */
    public void addAsm(Asm asm) {
        if (asm instanceof WordData || asm instanceof SpaceData || asm instanceof AsciizData) {
            mipsFile.addAsmToData(asm);
        } else {
            mipsFile.addAsmToText(asm);
        }
    }

    /***
     * 进入函数名对应的record。
     */
    public void enterRecord(String funcName) {
        curRecord = recordManager.getRecordByFuncName(funcName);
    }

    /***
     * 添加value到当前record。并返回其sp offset。
     */
    public int addValueToCurRecord(Value value) {
        int offset = curRecord.addValue(value);
        Comment comment = new Comment("Add value [name: " + value.getName() + ", offset: " + offset + "($sp)] to [" + curRecord.getFuncName().substring(1) + "] record.");
        addAsm(comment);
        return offset;
    }

    /***
     * 添加value到记录，指定其offset。用于解析zext命令。
     */
    public void addValueWithOffsetToCurRecord(Value value, int offset) {
        Comment comment = new Comment("Add value [name: " + value.getName() + ", offset: " + offset + "($sp)] to [" + curRecord.getFuncName().substring(1) + "] record.");
        addAsm(comment);
        curRecord.addValueWithOffset(value, offset);
    }

    /***
     * 返回当前record中指定value的sp偏移量。
     */
    public int getOffsetOfValue(Value value) {
        return curRecord.getOffsetOfValue(value);
    }

    public boolean isValueNotInCurRecord(Value value) {
        return !curRecord.getValueOffsetMap().containsKey(value);
    }

    /***
     * 添加reg到当前record。并返回其sp offset。
     */
    public int addRegToCurRecord(Reg reg) {
        int offset = curRecord.addReg(reg);
        Comment comment = new Comment("Add reg [name: " + reg + ", offset: " + offset + "($sp)] to [" + curRecord.getFuncName().substring(1) + "] record.");
        addAsm(comment);
        return offset;
    }

    /***
     * 返回当前record中指定reg的sp偏移量。
     */
    public int getOffsetOfReg(Reg reg) {
        return curRecord.getOffsetOfReg(reg);
    }

    /***
     * 向record中插入数组，并返回数组首元素sp偏移量。
     * @param allocaInst 分配数组指令。
     * @return 数组首元素sp偏移量。
     */
    public int insertArray(AllocaInst allocaInst) {
        return curRecord.insertArray(allocaInst);
    }

    public Record getCurRecord() {
        return curRecord;
    }

    public Record getRecordByFuncName(String funcName) {
        return recordManager.getRecordByFuncName(funcName);
    }
}
