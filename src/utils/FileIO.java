package utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileIO {
    /**
     * 输入文件名，输出对应文件内容的字符串形式。
     */
    public static String read(String filename) {
        //
        try {
            return Files.readString(Path.of(filename));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 将指定字符串写入文件名对应的文件。
     */
    public static void write(String filename, String content) {
        try {
            Files.writeString(Path.of(filename), content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
