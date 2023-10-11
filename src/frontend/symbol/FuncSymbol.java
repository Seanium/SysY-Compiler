package frontend.symbol;

import java.util.ArrayList;

public class FuncSymbol extends Symbol {
    private final boolean isVoid; // true: 返回void, false: 返回int
    private final ArrayList<Param> params;

    public FuncSymbol(String name, ArrayList<Param> params, boolean isVoid) {
        super(name);
        this.params = params;
        this.isVoid = isVoid;
    }

    public boolean isVoid() {
        return isVoid;
    }

    public ArrayList<Param> getParams() {
        return params;
    }
}
