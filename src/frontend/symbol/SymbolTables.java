package frontend.symbol;

import frontend.error.ErrorType;

import java.util.ArrayList;
import java.util.HashMap;

public class SymbolTables {     // 整个程序的符号表
    private static SymbolTables instance;
    private final ArrayList<HashMap<String, Symbol>> symbolTables;

    private SymbolTables() {
        this.symbolTables = new ArrayList<>();
    }

    public static SymbolTables getInstance() {
        if (instance == null) {
            instance = new SymbolTables();
        }
        return instance;
    }

    public void addTable() {
        symbolTables.add(new HashMap<>());
    }

    public void removeTable() {
        symbolTables.remove(symbolTables.size() - 1);
    }

    // 检测错误类型b【名字重定义】
    public boolean addSymbol(Symbol symbol) {
        HashMap<String, Symbol> curSymbolTable = symbolTables.get(symbolTables.size() - 1);
        if (curSymbolTable.containsKey(symbol.getName())) {
            return false;   // 重复声明，插入失败
        } else {
            curSymbolTable.put(symbol.getName(), symbol);
            return true;    // 插入成功
        }
    }

    // 检测错误类型c【未定义的名字】
    public Symbol findSymbol(String name) {
        for (int i = symbolTables.size() - 1; i >= 0; i--) {
            HashMap<String, Symbol> curSymbolTable = symbolTables.get(i);
            if (curSymbolTable.containsKey(name)) {
                return curSymbolTable.get(name);    // 已声明的标识符，返回symbol
            }
        }
        return null;   // 未声明的标识符，返回空
    }

    // 匹配函数的形参与实参
    // 检测错误类型d【函数参数个数不匹配】和错误类型e【函数参数类型不匹配】
    // 因为函数调用不会恶意换行，而一行最多一个错误，所以这里两种错误类型一起检测
    public ErrorType matchFuncParam(ArrayList<Param> formalParams, ArrayList<Param> realParams) {
        if (formalParams.size() != realParams.size()) {
            return ErrorType.d; // d【函数参数个数不匹配】
        } else {
            for (int i = 0; i < formalParams.size(); i++) {
                if (formalParams.get(i).getDim() != realParams.get(i).getDim()) {
                    return ErrorType.e; // e【函数参数类型不匹配】
                }
            }
            return null;    // 参数匹配成功
        }
    }

}
