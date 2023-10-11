package frontend.error;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

public class ErrorList {
    private static ErrorList instance;
    final ArrayList<Error> errors;

    private ErrorList() {
        this.errors = new ArrayList<>();
    }

    public static ErrorList getInstance() {
        if (instance == null) {
            instance = new ErrorList();
        }
        return instance;
    }

    public void addError(Error error) {
        this.errors.add(error);
    }

    @Override
    public String toString() {
        clear();
        StringBuilder sb = new StringBuilder();
        for (Error error : errors) {
            sb.append(error.toString());
        }
        return sb.toString();
    }

    // 同一行的错误只保留第一个
    private void clear() {
        HashSet<Integer> lineSet = new HashSet<>();
        Iterator<Error> iterator = errors.iterator();
        while (iterator.hasNext()) {
            Error error = iterator.next();
            if (lineSet.contains(error.getLineNum())) {
                iterator.remove();
            } else {
                lineSet.add(error.getLineNum());
            }
        }
    }
}
