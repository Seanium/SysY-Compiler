import backend.MIPSGenerator;
import backend.mips.MIPSFile;
import frontend.Lexer;
import frontend.Parser;
import frontend.error.ErrorList;
import frontend.node.CompUnitNode;
import midend.IRGenerator;
import midend.ir.Module;

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
//        1.词法分析
//        String source = readFile(args[0]);  // 从命令行参数获取源文件名
        String source = readFile("testfile.txt");
        Lexer lexer = Lexer.getInstance(source);
//        输出
//        String tokens = lexer.TokensToString(lexer.tokenize());
//        System.out.println(tokens);
//        writeFile("output.txt", tokens);

//        2.语法分析
        Parser parser = Parser.getInstance(lexer);
        CompUnitNode compUnitNode = parser.parseCompUnit();
//        输出
//        String parseResult = compUnitNode.toString();
//        System.out.println(parseResult);
//        writeFile("output.txt", parseResult);

//        3. 错误处理
        String errorListStr = ErrorList.getInstance().toString();
        if (!errorListStr.isEmpty()) {   // 若存在错误，只输出错误，不进行代码生成
            writeFile("error.txt", errorListStr);
            return;
        }

//        4.中间代码生成 (LLVM IR)
        IRGenerator irGenerator = IRGenerator.getInstance();
        irGenerator.visitCompUnitNode(compUnitNode);
//        输出
        Module module = Module.getInstance();
//        System.out.println(module);
        writeFile("llvm_ir.txt", module.toString());

//        5.目标代码生成 (MIPS)
        MIPSGenerator mipsGenerator = MIPSGenerator.getInstance();
        mipsGenerator.visitModule(Module.getInstance());
//        输出
        MIPSFile mipsFile = MIPSFile.getInstance();
//        System.out.println(mipsFile);
        writeFile("mips.txt", mipsFile.toString());
    }
}
