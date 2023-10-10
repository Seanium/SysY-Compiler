package frontend.error;

import java.util.ArrayList;

public class ErrorList {
    private static ErrorList instance;
    ArrayList<Error> errors;

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
        StringBuilder sb = new StringBuilder();
        for (Error error : errors) {
            sb.append(error.toString());
        }
        return sb.toString();
    }
}
