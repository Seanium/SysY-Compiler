package midend.ir.symbol;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 中间代码生成阶段的符号表。
 */
public class IRSymbolManager {
    private static IRSymbolManager instance;

    public static IRSymbolManager getInstance() {
        if (instance == null) {
            instance = new IRSymbolManager();
        }
        return instance;
    }

    private final ArrayList<HashMap<String, IRSymbol>> symbolTables;
    private boolean allowAddTable;  // 标记是否允许创建符号表，用于控制函数形参与第一层Block为同一张符号表

    private boolean isGlobal;   // 标记当前是否为全局变量

    private IRSymbolManager() {
        this.symbolTables = new ArrayList<>();
        this.allowAddTable = true;
        this.isGlobal = true;
    }

    private void addTable() {
        symbolTables.add(new HashMap<>());
    }

    private void removeTable() {
        symbolTables.remove(symbolTables.size() - 1);
    }

    public void enterFunction() {
        // 创建形参和第一层Block的符号表
        addTable();
        allowAddTable = false;
    }

    public void enterBlock() {
        // 创建Block的符号表(除了函数定义的第一层Block, 它与形参共享符号表)
        if (allowAddTable) {    // 当前不是函数定义的Block，需要创建符号表
            addTable();
        } else {    // 当前是函数定义的Block，无需创建符号表(与形参时创建的共享即可)，只需重置为允许创建
            allowAddTable = true;
        }
    }

    public void leaveBlock() {  //只需 leaveBlock，无需 leaveFunction
        removeTable();
    }

    public void addSymbol(String name, IRSymbol irSymbol) {
        symbolTables.get(symbolTables.size() - 1).put(name, irSymbol);
    }

    public IRSymbol findSymbol(String name) {
        for (int i = symbolTables.size() - 1; i >= 0; i--) {
            HashMap<String, IRSymbol> curSymbolTable = symbolTables.get(i);
            if (curSymbolTable.containsKey(name)) {
                return curSymbolTable.get(name);    // 已声明的标识符，返回value
            }
        }
        return null;   // 未声明的标识符，返回空
    }

    public boolean isGlobal() {
        return isGlobal;
    }

    public void setGlobal(boolean global) {
        isGlobal = global;
    }
}
