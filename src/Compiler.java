import backend.MIPSGenerator;
import backend.MIPSOptimizer;
import backend.mips.MIPSFile;
import frontend.Lexer;
import frontend.Parser;
import frontend.error.ErrorList;
import frontend.node.CompUnitNode;
import midend.IRGenerator;
import midend.IROptimizer;
import midend.ir.Module;
import utils.Config;
import utils.FileIO;

public class Compiler {

    public static void main(String[] args) {
//        Config.setMode(Config.Mode.DEBUG);
        Config.setMode(Config.Mode.RELEASE);

//        1.词法分析
//        String source = FileIO.read(args[0]);  // 从命令行参数获取源文件名
        String source = FileIO.read("testfile.txt");
        Lexer lexer = Lexer.getInstance(source);
//        String tokens = lexer.tokensToString(lexer.tokenize());
//        System.out.println(tokens);
//        FileIO.write("output.txt", tokens);

//        2.语法分析
        Parser parser = Parser.getInstance(lexer);
        CompUnitNode compUnitNode = parser.parseCompUnit();
//        String parseResult = compUnitNode.toString();
//        System.out.println(parseResult);
//        FileIO.write("output.txt", parseResult);

//        3. 错误处理
        String errorListStr = ErrorList.getInstance().toString();
        if (!errorListStr.isEmpty()) {   // 若存在错误，只输出错误，不进行代码生成
            FileIO.write("error.txt", errorListStr);
            return;
        }

//        4.中间代码生成 (LLVM IR)
        IRGenerator irGenerator = IRGenerator.getInstance();
        irGenerator.visitCompUnitNode(compUnitNode);
        Module module = Module.getInstance();
        if (Config.getMode() == Config.Mode.DEBUG) {
//        System.out.println(module);
            FileIO.write("llvm_ir_raw.txt", module.toString());
        }

        MIPSGenerator mipsGenerator;
        MIPSFile mipsFile;
        if (Config.getMode() == Config.Mode.DEBUG) {
//        5.目标代码生成 (MIPS)(无优化)
            mipsGenerator = new MIPSGenerator();
            mipsGenerator.visitModule(Module.getInstance());
            mipsFile = mipsGenerator.getCurMIPSFile();
//        System.out.println(mipsFile);
            FileIO.write("mips_raw.txt", mipsFile.toString());
        }

//        6.中端优化
        IROptimizer irOptimizer = IROptimizer.getInstance();
        irOptimizer.runPasses();

//        7.目标代码生成 (MIPS)(仅中端优化)
        mipsGenerator = new MIPSGenerator();
        mipsGenerator.visitModule(Module.getInstance());
        if (Config.getMode() == Config.Mode.DEBUG) {
            mipsFile = mipsGenerator.getCurMIPSFile();
            FileIO.write("mips_no_back_opt.txt", mipsFile.toString());
        }

//        8.后端优化
        mipsFile = mipsGenerator.getCurMIPSFile();
        MIPSOptimizer mipsOptimizer = new MIPSOptimizer(mipsFile);
        mipsOptimizer.runPasses();
        FileIO.write("mips.txt", mipsFile.toString());
    }
}
