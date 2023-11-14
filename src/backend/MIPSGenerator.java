package backend;

import backend.mips.Reg;
import backend.mips.data.AsciizData;
import backend.mips.data.SpaceData;
import backend.mips.data.WordData;
import backend.mips.text.Opcode;
import backend.mips.text.*;
import midend.ir.Module;
import midend.ir.*;
import midend.ir.inst.*;
import midend.ir.type.IntegerType;

import java.util.ArrayList;

public class MIPSGenerator {
    private static MIPSGenerator instance;

    public static MIPSGenerator getInstance() {
        if (instance == null) {
            instance = new MIPSGenerator();
        }
        return instance;
    }

    private MIPSGenerator() {
        this.mipsBuilder = MIPSBuilder.getInstance();
    }

    private final MIPSBuilder mipsBuilder;

    public void visitModule(Module module) {
        for (Value global : module.getGlobals()) {
            visitGlobal(global);
        }
        JalInst jalInst = new JalInst("main");
        mipsBuilder.addAsm(jalInst);
        JInst jInst = new JInst("end");
        mipsBuilder.addAsm(jInst);
        for (Function function : module.getFunctions()) {
            if (!function.isLib()) {    // 库函数不生成MIPS
                visitFunction(function);
            }
        }
        Label endLabel = new Label("end");
        mipsBuilder.addAsm(endLabel);
    }

    public void visitGlobal(Value global) {
        if (global instanceof GlobalVar globalVar) {  // 非数组全局变量
            ArrayList<Integer> values = new ArrayList<>();
            values.add((globalVar).getInitValue().getValue());
            WordData wordData = new WordData(globalVar.getName().substring(1), values);
            mipsBuilder.addAsm(wordData);
        } else if (global instanceof GlobalArray globalArray) { // 数组全局变量
            if (globalArray.getArrayInitValue().getType() != IntegerType.i8) { // 数字
                if (globalArray.checkAllZero()) {    // 初值全为0，用space
                    SpaceData spaceData = new SpaceData(globalArray.getName().substring(1), globalArray.getLen() * 4); // 字节数是数组长度*4
                    mipsBuilder.addAsm(spaceData);
                } else {    // 初值不全为0，用word
                    ArrayList<Integer> values = new ArrayList<>();
                    for (Constant constant : globalArray.getArrayInitValue().getConstants()) {
                        values.add(constant.getValue());
                    }
                    // Collections.reverse(values);  // 下标小的元素在低地址，不用逆序
                    WordData wordData = new WordData(globalArray.getName().substring(1), values);
                    mipsBuilder.addAsm(wordData);
                }
            } else {    // 字符串
                AsciizData asciizData = new AsciizData(globalArray.getName().substring(1), globalArray.toAsciizDataContent());
                mipsBuilder.addAsm(asciizData);
            }
        }
    }

    public void visitFunction(Function function) {
        Label funcLabel = new Label(function.getName().substring(1));
        mipsBuilder.addAsm(funcLabel);
        // 进入record
        mipsBuilder.enterRecord(function.getName());
        // 添加形参到栈空间（实际是在调用方生成mips代码，这里只是添加到被调用函数的record）
        ArrayList<Param> params = function.getParams();
        for (Param param : params) {
            mipsBuilder.addValueToCurRecord(param);
        }
        for (BasicBlock basicBlock : function.getBasicBlocks()) {
            visitBasicBlock(basicBlock);
        }
    }

    private void visitBasicBlock(BasicBlock basicBlock) {
        Label basicBlockLabel = new Label(mipsBuilder.getCurRecord().getFuncName().substring(1) + "_" + basicBlock.getName());
        mipsBuilder.addAsm(basicBlockLabel);
        for (Inst inst : basicBlock.getInstructions()) {
            visitInst(inst);
        }
    }

    private void visitInst(Inst inst) {
        Comment comment = new Comment(inst.toString());
        mipsBuilder.addAsm(comment);
        if (inst instanceof AllocaInst allocaInst) {
            visitAllocaInst(allocaInst);
        } else if (inst instanceof GEPInst gepInst) {
            visitGEPInst(gepInst);
        } else if (inst instanceof StoreInst storeInst) {
            visitStoreInst(storeInst);
        } else if (inst instanceof LoadInst loadInst) {
            visitLoadInst(loadInst);
        } else if (inst instanceof CallInst callInst) {
            visitCallInst(callInst);
        } else if (inst instanceof ReturnInst returnInst) {
            visitReturnInst(returnInst);
        } else if (inst instanceof JumpInst jumpInst) {
            visitJumpInst(jumpInst);
        } else if (inst instanceof BranchInst branchInst) {
            visitBranchInst(branchInst);
        } else if (inst instanceof ZextInst zextInst) {
            visitZextInst(zextInst);
        } else if (inst instanceof IcmpInst icmpInst) {
            visitIcmpInst(icmpInst);
        } else if (inst instanceof BinaryInst binaryInst) {
            visitBinaryInst(binaryInst);
        }
    }

    private void visitAllocaInst(AllocaInst allocaInst) {
        if (allocaInst.getTargetType() == IntegerType.i32) {    // 不是数组
            mipsBuilder.addValueToCurRecord(allocaInst);
        } else {    // 局部数组 下标小的元素在低地址
            // 活动记录中，预留数组元素位置，并在之后的一个栈帧中存入首元素偏移量
            int arrayOffset = mipsBuilder.insertArray(allocaInst);
            // addiu $t0 $sp arrayOffset
            AddiuInst addiuInst = new AddiuInst(Reg.t0, Reg.sp, arrayOffset);
            mipsBuilder.addAsm(addiuInst);
            // sw $t0 offset($sp)
            int offset = mipsBuilder.addValueToCurRecord(allocaInst);
            SwInst swInst = new SwInst(Reg.t0, offset, Reg.sp);
            mipsBuilder.addAsm(swInst);
        }
    }

    private void visitGEPInst(GEPInst gepInst) {
        if (gepInst.getBasePointer() instanceof AllocaInst || gepInst.getBasePointer() instanceof Param) {   // 局部数组 数组形参 下标小的元素在低地址
            // lw $t0 offset($sp) 数组首元素地址存入t0
            int offset = mipsBuilder.getOffsetOfValue(gepInst.getBasePointer());
            LwInst lwInst = new LwInst(Reg.t0, offset, Reg.sp);
            mipsBuilder.addAsm(lwInst);
            // 目标元素内存地址偏移量存入t1
            if (gepInst.getOffset() instanceof Constant constant) {  // 若偏移量为常数
                // li $t1 eleOffset
                int eleOffset = 4 * constant.getValue(); // 下标小的元素在低地址
                LiInst liInst = new LiInst(Reg.t1, eleOffset);
                mipsBuilder.addAsm(liInst);
            } else {    // 偏移量不为常数
                // lw $t1 offset1($sp)
                int offset1 = mipsBuilder.getOffsetOfValue(gepInst.getOffset());
                LwInst lwInst1 = new LwInst(Reg.t1, offset1, Reg.sp);
                mipsBuilder.addAsm(lwInst1);
                // sll $t1 $t1 2 偏移量变量 需要*4 转换为地址偏移量 比如g[i]的实际地址偏移量是i*4
                SllInst sllInst = new SllInst(Reg.t1, Reg.t1, 2);
                mipsBuilder.addAsm(sllInst);
            }
            // addu $t0 $t0 $t1 目标元素地址存入t0
            AdduInst adduInst = new AdduInst(Reg.t0, Reg.t0, Reg.t1);
            mipsBuilder.addAsm(adduInst);
            // sw $t0 offset1($t1) 目标元素地址存入栈帧
            int offset1 = mipsBuilder.addValueToCurRecord(gepInst);
            SwInst swInst = new SwInst(Reg.t0, offset1, Reg.sp);
            mipsBuilder.addAsm(swInst);
        } else if (gepInst.getBasePointer() instanceof GlobalArray globalArray) {   // 全局数组 下标小的元素在低地址
            // 目标元素内存地址偏移量，存到t1
            if (gepInst.getOffset() instanceof Constant constant) { // 若偏移量为常数
                // li $t1 eleOffset
                int eleOffset = 4 * constant.getValue();
                LiInst liInst = new LiInst(Reg.t1, eleOffset);
                mipsBuilder.addAsm(liInst);
            } else {    // 偏移量变量 需要*4 转换为地址偏移量 比如g[i]的实际地址偏移量是i*4
                // lw $t1 offset($sp)
                int offset = mipsBuilder.getOffsetOfValue(gepInst.getOffset());
                LwInst lwInst = new LwInst(Reg.t1, offset, Reg.sp);
                mipsBuilder.addAsm(lwInst);
                // sll $t1 $t1 2 偏移量变量 需要*4 转换为地址偏移量 比如g[i]的实际地址偏移量是i*4
                SllInst sllInst = new SllInst(Reg.t1, Reg.t1, 2);
                mipsBuilder.addAsm(sllInst);
            }
            // la $t0 arrayLabel($t1) 目标元素地址存到t0
            LaInst laInst = new LaInst(Reg.t0, globalArray.getName().substring(1), Reg.t1);
            mipsBuilder.addAsm(laInst);
            // sw $t0 offset($sp) 目标元素地址存到栈帧
            int offset = mipsBuilder.addValueToCurRecord(gepInst);
            SwInst swInst = new SwInst(Reg.t0, offset, Reg.sp);
            mipsBuilder.addAsm(swInst);
        }
    }

    private void visitStoreInst(StoreInst storeInst) {
        Value from = storeInst.getFrom();
        Value to = storeInst.getTo();
        // 处理来源 存入t0
        if (from instanceof Constant constant) { // 要存入的是常数, li加载到t0
            // li $t0 constant
            LiInst liInst = new LiInst(Reg.t0, constant.getValue());
            mipsBuilder.addAsm(liInst);
        } else {    // 要存入的不是常数，lw从栈帧加载到t0
            // lw $t0 offset($sp)
            int offset = mipsBuilder.getOffsetOfValue(from);
            LwInst lwInst = new LwInst(Reg.t0, offset, Reg.sp);
            mipsBuilder.addAsm(lwInst);
        }
        // 处理目标 把t0内容存入内存相应位置
        if (to instanceof GlobalVar globalVar) {  // 如果目标是非数组全局变量
            // sw $t0 label
            String label = globalVar.getName().substring(1);
            SwInst swInst = new SwInst(Reg.t0, label);
            mipsBuilder.addAsm(swInst);
        } else if (to instanceof AllocaInst allocaInst) {   // 如果目标是非数组局部变量
            // sw $t0 offset($sp)
            int offset = mipsBuilder.getOffsetOfValue(allocaInst);
            SwInst swInst = new SwInst(Reg.t0, offset, Reg.sp);
            mipsBuilder.addAsm(swInst);
        } else if (to instanceof GEPInst gepInst) { // 如果目标是数组元素 包括全局数组、局部数组、参数数组元素 与load相对应，也要存两次 第一次把地址存入$t1 第二次把数据存入地址
            // lw $t1 offset($sp) 取出目标元素内存地址，存到t1
            int offset = mipsBuilder.getOffsetOfValue(gepInst);
            LwInst lwInst = new LwInst(Reg.t1, offset, Reg.sp);
            mipsBuilder.addAsm(lwInst);
            // sw $t0 0($t1) 存入目标元素
            SwInst swInst = new SwInst(Reg.t0, 0, Reg.t1);
            mipsBuilder.addAsm(swInst);
        }
    }

    private void visitLoadInst(LoadInst loadInst) {
        // 取出load源
        if (loadInst.getPointer() instanceof GlobalVar globalVar) {   // 如果数据源是全局变量
            // lw $t0 label
            LwInst lwInst = new LwInst(Reg.t0, globalVar.getName().substring(1));
            mipsBuilder.addAsm(lwInst);
        } else {    // 如果数据源不是全局变量
            // lw $t0 offset($sp)
            int offset = mipsBuilder.getOffsetOfValue(loadInst.getPointer());   // 从活动记录中查询栈帧偏移量
            LwInst lwInst = new LwInst(Reg.t0, offset, Reg.sp);
            mipsBuilder.addAsm(lwInst);
            if (loadInst.getPointer() instanceof GEPInst) { // 如果load源是gep指令，还需要再取一次，因为栈帧中存的是元素地址，还需要从元素地址中取出元素的值
                // lw $t0 0($t0)
                LwInst lwInst1 = new LwInst(Reg.t0, 0, Reg.t0);
                mipsBuilder.addAsm(lwInst1);
            }
        }
        // sw $t0 offset1($sp)  存入load目标
        int offset1 = mipsBuilder.addValueToCurRecord(loadInst);
        SwInst swInst = new SwInst(Reg.t0, offset1, Reg.sp);
        mipsBuilder.addAsm(swInst);
    }

    private void visitCallInst(CallInst callInst) {
        if (!callInst.getTargetFunc().isLib()) {    // 不是库函数
            /* 第一步 保存ra和sp */
            // sw $ra raOffset($sp)
            int raOffset = mipsBuilder.addRegToCurRecord(Reg.ra);
            SwInst swInst = new SwInst(Reg.ra, raOffset, Reg.sp);
            mipsBuilder.addAsm(swInst);
            // sw $sp spOffset($sp)
            int spOffset = mipsBuilder.addRegToCurRecord(Reg.sp);
            SwInst swInst1 = new SwInst(Reg.sp, spOffset, Reg.sp);
            mipsBuilder.addAsm(swInst1);

            /* 第二步 保存实参到栈帧 */
            // 保存调用者和被调函数名
            String callerName = mipsBuilder.getCurRecord().getFuncName();
            String calleeName = callInst.getTargetFunc().getName();
            // 保存调用者函数栈帧当前偏移量
            int callerOffset = mipsBuilder.getCurRecord().getCurOffset();
            // 保存实参到被调函数栈帧开头
            ArrayList<Param> params = callInst.getTargetFunc().getParams();
            for (int i = 0; i < params.size(); i++) {
                Value value = callInst.getArgs().get(i);
                if (value instanceof Constant constant) {    // 如果实参是常数
                    // li $t0 constant
                    LiInst liInst = new LiInst(Reg.t0, constant.getValue());
                    mipsBuilder.addAsm(liInst);
                } else {    // 如果实参不是常数
                    // lw $t0 offset($sp)
                    int offset = mipsBuilder.getRecordByFuncName(callerName).getOffsetOfValue(value); // 在调用者函数中查找实参偏移量
                    LwInst lwInst = new LwInst(Reg.t0, offset, Reg.sp);
                    mipsBuilder.addAsm(lwInst);
                }
                // sw $t0 (callerOffset+argOffset)($sp) // 把实参存入被调函数的栈帧 （虽然这里lw和sw的是实参，但被调函数record中之前已经存入的是被调函数的形参，而不是调用者函数的实参）
                int argOffset = mipsBuilder.getRecordByFuncName(calleeName).getOffsetOfValue(params.get(i));    // 查询形参的offset
                SwInst swInst2 = new SwInst(Reg.t0, callerOffset + argOffset, Reg.sp);
                mipsBuilder.addAsm(swInst2);
            }
            // addiu $sp $sp callerOffset 重设sp，使其0地址为首个实参地址，做好调用函数的准备
            AddiuInst addiuInst = new AddiuInst(Reg.sp, Reg.sp, callerOffset);
            mipsBuilder.addAsm(addiuInst);
            // jal func
            JalInst jalInst = new JalInst(calleeName.substring(1));
            mipsBuilder.addAsm(jalInst);

            /* 第三步 恢复sp和ra */
            // lw $sp 0($sp) 恢复sp
            LwInst lwInst = new LwInst(Reg.sp, 4, Reg.sp);
            mipsBuilder.addAsm(lwInst);
            // lw $ra raOffset($sp) 恢复ra
            LwInst lwInst1 = new LwInst(Reg.ra, raOffset, Reg.sp);
            mipsBuilder.addAsm(lwInst1);
            if (callInst.getName() != null) {   // 如果调用的函数有返回值
                // sw $v0 retOffset($sp)
                int retOffset = mipsBuilder.addValueToCurRecord(callInst);
                SwInst swInst2 = new SwInst(Reg.v0, retOffset, Reg.sp);
                mipsBuilder.addAsm(swInst2);
            }
        } else {    // 库函数
            Function function = callInst.getTargetFunc();
            if (function.getName().equals("@getint")) {
                // li $v0 5 读入整数
                LiInst liInst = new LiInst(Reg.v0, 5);
                mipsBuilder.addAsm(liInst);
                // syscall
                SyscallInst syscallInst = new SyscallInst();
                mipsBuilder.addAsm(syscallInst);
                // sw $v0 offset($sp) 保存到栈帧
                int offset = mipsBuilder.addValueToCurRecord(callInst);
                SwInst swInst = new SwInst(Reg.v0, offset, Reg.sp);
                mipsBuilder.addAsm(swInst);
            } else if (function.getName().equals("@putint")) {
                Value arg = callInst.getArgs().get(0);
                if (arg instanceof Constant constant) {  // 常数
                    // li $a0 constant
                    LiInst liInst = new LiInst(Reg.a0, constant.getValue());
                    mipsBuilder.addAsm(liInst);
                } else {    // 不是常数
                    // lw $a0 offset($sp)
                    int offset = mipsBuilder.getOffsetOfValue(arg);
                    LwInst lwInst = new LwInst(Reg.a0, offset, Reg.sp);
                    mipsBuilder.addAsm(lwInst);
                }
                // li $v0 1 打印整数
                LiInst liInst = new LiInst(Reg.v0, 1);
                mipsBuilder.addAsm(liInst);
                // syscall
                SyscallInst syscallInst = new SyscallInst();
                mipsBuilder.addAsm(syscallInst);
            } else if (function.getName().equals("@putstr")) {
                // lw $a0 offset($sp)
                int offset = mipsBuilder.getOffsetOfValue(callInst.getArgs().get(0));
                LwInst lwInst = new LwInst(Reg.a0, offset, Reg.sp);
                mipsBuilder.addAsm(lwInst);
                // li $v0 4 打印字符串
                LiInst liInst = new LiInst(Reg.v0, 4);
                mipsBuilder.addAsm(liInst);
                // syscall
                SyscallInst syscallInst = new SyscallInst();
                mipsBuilder.addAsm(syscallInst);
            }
        }
    }

    private void visitReturnInst(ReturnInst returnInst) {
        if (returnInst.getValue() == null) {    // 如果无返回值
            JrInst jrInst = new JrInst(Reg.ra);
            mipsBuilder.addAsm(jrInst);
        } else {    // 有返回值
            if (returnInst.getValue() instanceof Constant constant) {    // 返回值为常数
                // li $v0 constant
                LiInst liInst = new LiInst(Reg.v0, constant.getValue());
                mipsBuilder.addAsm(liInst);
            } else {    // 返回值不是常数
                // lw $v0 offset($sp)
                int offset = mipsBuilder.getOffsetOfValue(returnInst.getValue());
                LwInst lwInst = new LwInst(Reg.v0, offset, Reg.sp);
                mipsBuilder.addAsm(lwInst);
            }
            // jr $ra
            JrInst jrInst = new JrInst(Reg.ra);
            mipsBuilder.addAsm(jrInst);
        }
    }

    private void visitJumpInst(JumpInst jumpInst) {
        JInst jInst = new JInst(mipsBuilder.getCurRecord().getFuncName().substring(1) + "_" + jumpInst.getTargetBasicBlock().getName());
        mipsBuilder.addAsm(jInst);
    }

    private void visitBranchInst(BranchInst branchInst) {
        // lw $t0 offset($sp) 获取条件，存入t0
        int offset = mipsBuilder.getOffsetOfValue(branchInst.getCond());
        LwInst lwInst = new LwInst(Reg.t0, offset, Reg.sp);
        mipsBuilder.addAsm(lwInst);
        // beqz $t0 falseLabel 若条件为假，则跳转到假基本块
        BeqzInst beqzInst = new BeqzInst(Reg.t0, mipsBuilder.getCurRecord().getFuncName().substring(1) + "_" + branchInst.getFalseBlock().getName());
        mipsBuilder.addAsm(beqzInst);
        // j trueLabel 若条件为真，则跳转到真基本块
        JInst jInst = new JInst(mipsBuilder.getCurRecord().getFuncName().substring(1) + "_" + branchInst.getTrueBlock().getName());
        mipsBuilder.addAsm(jInst);
    }

    private void visitZextInst(ZextInst zextInst) {
        int offset = mipsBuilder.getOffsetOfValue(zextInst.getOriValue());
        mipsBuilder.addValueWithOffsetToCurRecord(zextInst, offset);
    }

    private void visitIcmpInst(IcmpInst icmpInst) {
        // 获取操作数1
        if (icmpInst.getOperand1() instanceof Constant constant) {
            // li $t0 constant
            LiInst liInst = new LiInst(Reg.t0, constant.getValue());
            mipsBuilder.addAsm(liInst);
        } else {
            // lw $t0 offset($sp)
            int offset = mipsBuilder.getOffsetOfValue(icmpInst.getOperand1());
            LwInst lwInst = new LwInst(Reg.t0, offset, Reg.sp);
            mipsBuilder.addAsm(lwInst);
        }
        // 获取操作数2
        if (icmpInst.getOperand2() instanceof Constant constant) {
            // li $t1 constant
            LiInst liInst = new LiInst(Reg.t1, constant.getValue());
            mipsBuilder.addAsm(liInst);
        } else {
            // lw $t1 offset($sp)
            int offset = mipsBuilder.getOffsetOfValue(icmpInst.getOperand2());
            LwInst lwInst = new LwInst(Reg.t1, offset, Reg.sp);
            mipsBuilder.addAsm(lwInst);
        }
        // opcode $t0 $t0 $t1
        Opcode opcode;
        switch (icmpInst.getIcmpKind()) {
            case eq -> opcode = Opcode.seq;
            case ne -> opcode = Opcode.sne;
            case sgt -> opcode = Opcode.sgt;
            case sge -> opcode = Opcode.sge;
            case slt -> opcode = Opcode.slt;
            case sle -> opcode = Opcode.sle;
            default -> opcode = null;
        }
        SetCmpInst setCmpInst = new SetCmpInst(opcode, Reg.t0, Reg.t0, Reg.t1);
        mipsBuilder.addAsm(setCmpInst);
        // 添加到record
        int resOffset = mipsBuilder.addValueToCurRecord(icmpInst);
        // sw $t0 resOffset($sp)
        SwInst swInst = new SwInst(Reg.t0, resOffset, Reg.sp);
        mipsBuilder.addAsm(swInst);
    }

    private void visitBinaryInst(BinaryInst binaryInst) {
        // 获取操作数1
        if (binaryInst.getOperand1() instanceof Constant constant) {
            // li $t0 constant
            LiInst liInst = new LiInst(Reg.t0, constant.getValue());
            mipsBuilder.addAsm(liInst);
        } else {
            // lw $t0 offset($sp)
            int offset = mipsBuilder.getOffsetOfValue(binaryInst.getOperand1());
            LwInst lwInst = new LwInst(Reg.t0, offset, Reg.sp);
            mipsBuilder.addAsm(lwInst);
        }
        // 获取操作数2
        if (binaryInst.getOperand2() instanceof Constant constant) {
            // li $t1 constant
            LiInst liInst = new LiInst(Reg.t1, constant.getValue());
            mipsBuilder.addAsm(liInst);
        } else {
            // lw $t1 offset($sp)
            int offset = mipsBuilder.getOffsetOfValue(binaryInst.getOperand2());
            LwInst lwInst = new LwInst(Reg.t1, offset, Reg.sp);
            mipsBuilder.addAsm(lwInst);
        }
        // 创建指令 运算结果保存到t0
        switch (binaryInst.getOpcode()) {
            case add -> {
                // addu $t0 $t0 $t1
                AdduInst adduInst = new AdduInst(Reg.t0, Reg.t0, Reg.t1);
                mipsBuilder.addAsm(adduInst);
            }
            case sub -> {
                // subu $t0 $t0 $t1
                SubuInst subuInst = new SubuInst(Reg.t0, Reg.t0, Reg.t1);
                mipsBuilder.addAsm(subuInst);
            }
            case mul -> {
                // mult $t0 $t1
                MultInst multInst = new MultInst(Reg.t0, Reg.t1);
                mipsBuilder.addAsm(multInst);
                // mflo $t0
                MfHiloInst mfloInst = new MfHiloInst(Opcode.mflo, Reg.t0);
                mipsBuilder.addAsm(mfloInst);
            }
            case sdiv -> {
                // div $t0 $t1
                DivInst divInst = new DivInst(Reg.t0, Reg.t1);
                mipsBuilder.addAsm(divInst);
                // mflo $t0
                MfHiloInst mfloInst = new MfHiloInst(Opcode.mflo, Reg.t0);
                mipsBuilder.addAsm(mfloInst);
            }
            case srem -> {
                // div $t0 $t1
                DivInst divInst = new DivInst(Reg.t0, Reg.t1);
                mipsBuilder.addAsm(divInst);
                // mfhi $t0
                MfHiloInst mfhiInst = new MfHiloInst(Opcode.mfhi, Reg.t0);
                mipsBuilder.addAsm(mfhiInst);
            }
        }
        // 添加到record
        int resOffset = mipsBuilder.addValueToCurRecord(binaryInst);
        // sw $t0 resOffset($sp)
        SwInst swInst = new SwInst(Reg.t0, resOffset, Reg.sp);
        mipsBuilder.addAsm(swInst);
    }
}
