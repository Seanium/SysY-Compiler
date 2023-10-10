package frontend.error;

public class Error {
    private ErrorType type;
    private int lineNum;

    public Error(ErrorType type, int lineNum) {
        this.type = type;
        this.lineNum = lineNum;
    }

    @Override
    public String toString() {
        return type + " " + lineNum + "\n";
    }
}
