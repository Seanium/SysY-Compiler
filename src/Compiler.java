import backend.MIPSGenerator;
import backend.mips.MIPSFile;
import utils.FileIO;
import frontend.Lexer;
import frontend.Parser;
import frontend.error.ErrorList;
import frontend.node.CompUnitNode;
import midend.IRGenerator;
import midend.IROptimizer;
import midend.ir.Module;

public class Compiler {

    public static void main(String[] args) {
//        1.词法分析
//        String source = FileIOUtils.read(args[0]);  // 从命令行参数获取源文件名
        String source = FileIO.read("testfile.txt");
        Lexer lexer = Lexer.getInstance(source);
//        输出
//        String tokens = lexer.TokensToString(lexer.tokenize());
//        System.out.println(tokens);
//        FileIOUtils.write("output.txt", tokens);

//        2.语法分析
        Parser parser = Parser.getInstance(lexer);
        CompUnitNode compUnitNode = parser.parseCompUnit();
//        输出
//        String parseResult = compUnitNode.toString();
//        System.out.println(parseResult);
//        FileIOUtils.write("output.txt", parseResult);

//        3. 错误处理
        String errorListStr = ErrorList.getInstance().toString();
        if (!errorListStr.isEmpty()) {   // 若存在错误，只输出错误，不进行代码生成
            FileIO.write("error.txt", errorListStr);
            return;
        }

//        4.中间代码生成 (LLVM IR)
        IRGenerator irGenerator = IRGenerator.getInstance();
        irGenerator.visitCompUnitNode(compUnitNode);
//        输出
        Module module = Module.getInstance();
//        System.out.println(module);
        FileIO.write("llvm_ir_raw.txt", module.toString());

//        5.中间代码优化
        IROptimizer irOptimizer = IROptimizer.getInstance();
        irOptimizer.runPasses();
        FileIO.write("llvm_ir_move.txt", module.toString());

////        6.目标代码生成 (MIPS)
//        MIPSGenerator mipsGenerator = MIPSGenerator.getInstance();
//        mipsGenerator.visitModule(Module.getInstance());
////        输出
//        MIPSFile mipsFile = MIPSFile.getInstance();
////        System.out.println(mipsFile);
//        FileIO.write("mips.txt", mipsFile.toString());
    }
}
