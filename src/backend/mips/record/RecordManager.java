package backend.mips.record;

import java.util.HashMap;

public class RecordManager {

    private final HashMap<String, Record> records;

    public RecordManager() {
        this.records = new HashMap<>();
    }

    /**
     * 根据函数名查询record。若不存在则先创建再返回。
     * @param funcName 函数名。
     * @return 对应的record。
     */
    public Record getRecordByFuncName(String funcName) {
        if (records.get(funcName) == null) {
            Record record = new Record(funcName);
            records.put(funcName, record);
        }
        return records.get(funcName);
    }
}
