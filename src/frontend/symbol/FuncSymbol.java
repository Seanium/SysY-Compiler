package frontend.symbol;

import java.util.ArrayList;

public class FuncSymbol extends Symbol {
    private boolean isVoid; // true: 返回void, false: 返回int
    private ArrayList<Param> params = new ArrayList<>();

    public FuncSymbol(String name, ArrayList<Param> params) {
        super(name);
        this.params = params;
    }

    public ArrayList<Param> getParams() {
        return params;
    }
}
