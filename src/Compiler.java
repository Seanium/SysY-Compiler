import frontend.Lexer;
import frontend.Parser;
import frontend.node.CompUnitNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Compiler {
    public static String readFile(String filename) {
        // 从文件中读取源代码
        try {
            return Files.readString(Path.of(filename));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // 写入到文件
    public static void writeFile(String filename, String content) {
        try {
            Files.writeString(Path.of(filename), content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        String source = readFile("testfile.txt");
//        System.out.println(source);
        Lexer lexer = new Lexer(source);

        // 词法分析作业
//        String tokens = lexer.TokensToString(lexer.tokenize());
//        System.out.println(tokens);
//        writeFile("output.txt", tokens);

        // 语法分析作业
        Parser parser = new Parser(lexer);
        CompUnitNode compUnitNode = parser.parseCompUnit();
        String parseResult = compUnitNode.toString();
        System.out.println(parseResult);
        writeFile("output.txt", parseResult);
    }
}
