package utils;

public class Config {
    private static Mode mode;

    public static Mode getMode() {
        return mode;
    }

    public static void setMode(Mode mode) {
        Config.mode = mode;
    }

    public enum Mode {
        DEBUG,
        RELEASE
    }
}
