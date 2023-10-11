package frontend.error;

public class Error {
    private final ErrorType type;
    private final int lineNum;

    public Error(ErrorType type, int lineNum) {
        this.type = type;
        this.lineNum = lineNum;
    }

    public int getLineNum() {
        return lineNum;
    }

    @Override
    public String toString() {
        return lineNum + " " + type + "\n";
    }
}
