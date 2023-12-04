package backend;

import backend.mips.MIPSFile;
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
    private final MIPSBuilder mipsBuilder;

    public MIPSGenerator() {
        this.mipsBuilder = new MIPSBuilder();
    }

    public MIPSFile getCurMIPSFile() {
        return mipsBuilder.getMipsFile();
    }

    public void visitModule(Module module) {
        for (Value global : module.getGlobals()) {
            visitGlobal(global);
        }
//        JalInst jalInst = new JalInst("main");
//        mipsBuilder.addAsm(jalInst);
//        JInst jInst = new JInst("end");
//        mipsBuilder.addAsm(jInst);
        JInst jInst = new JInst("main");
        mipsBuilder.addAsm(jInst);
        for (Function function : module.getFunctions()) {
            if (!function.isLib()) {    // 库函数不生成MIPS
                visitFunction(function);
            }
        }
//        Label endLabel = new Label("end");
//        mipsBuilder.addAsm(endLabel);
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
        // 添加全部形参到栈空间（之后在调用方将实参store到这些空间，这里只是先在被调用函数的record开辟空间）
        ArrayList<Param> params = function.getParams();
        for (Param param : params) {
            mipsBuilder.addValueToCurRecord(param);
        }
        // 该步骤在函数第一个块的标签之前
        // 若形参分配到寄存器，将其从参数寄存器$ai move到寄存器$reg(前四个参数),或从内存load到寄存器reg(第五个参数及之后)
        // 若形参未分配到寄存器，前四个参数需要从$ai save到内存
        for (int i = 0; i < params.size(); i++) {
            Param param = params.get(i);
            if (param.inReg()) {    // 若形参分配到寄存器，将其从参数寄存器$ai move到寄存器$reg(前四个参数),或从内存load到寄存器reg(第五个参数及之后)
                if (i <= 3) {
                    // move $reg %ai
                    Reg reg = param.getReg();
                    Reg ai = Reg.values()[Reg.a0.ordinal() + i];
                    MoveMIPSInst moveMIPSInst = new MoveMIPSInst(reg, ai);
                    mipsBuilder.addAsm(moveMIPSInst);
                } else {
                    // lw $reg offset($sp)
                    Reg reg = param.getReg();
                    int offset = mipsBuilder.getOffsetOfValue(param);
                    LwInst lwInst = new LwInst(reg, offset, Reg.sp);
                    mipsBuilder.addAsm(lwInst);
                }
            } else {    // 若形参未分配到寄存器，前四个参数需要从$ai save到内存
                if (i <= 3) {
                    // sw $ai offset($sp)
                    Reg ai = Reg.values()[Reg.a0.ordinal() + i];
                    int offset = mipsBuilder.getOffsetOfValue(param);
                    SwInst swInst = new SwInst(ai, offset, Reg.sp);
                    mipsBuilder.addAsm(swInst);
                }
            }
        }
        // 访问基本块
        for (BasicBlock basicBlock : function.getBasicBlocks()) {
            visitBasicBlock(basicBlock);
        }
    }

    private void visitBasicBlock(BasicBlock basicBlock) {
        Label basicBlockLabel = new Label(basicBlock.getName());
        mipsBuilder.addAsm(basicBlockLabel);
        for (Inst inst : basicBlock.getInsts()) {
            visitInst(inst);
        }
    }

    private void visitInst(Inst inst) {
        Comment comment = new Comment("---[INST]" + inst.toString());
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
        } else if (inst instanceof MoveInst moveInst) {
            visitMoveInst(moveInst);
        }
    }

    private void visitAllocaInst(AllocaInst allocaInst) {
        if (allocaInst.getTargetType() == IntegerType.i32) {    // 不是数组
            mipsBuilder.addValueToCurRecord(allocaInst);
        } else {    // 局部数组 下标小的元素在低地址
            int arrayOffset = mipsBuilder.insertArray(allocaInst);
            if (allocaInst.notInReg()) {    // 首元素地址未分配寄存器
                // addiu $t0 $sp arrayOffset
                AddiuInst addiuInst = new AddiuInst(Reg.t0, Reg.sp, arrayOffset);
                mipsBuilder.addAsm(addiuInst);
                // sw $t0 offset($sp) 在栈帧中存入首元素偏移量
                int offset = mipsBuilder.addValueToCurRecord(allocaInst);
                SwInst swInst = new SwInst(Reg.t0, offset, Reg.sp);
                mipsBuilder.addAsm(swInst);
            } else {    // 首元素地址分配了寄存器
                // addiu $toReg $sp arrayOffset
                Reg toReg = allocaInst.getReg();
                AddiuInst addiuInst = new AddiuInst(toReg, Reg.sp, arrayOffset);
                mipsBuilder.addAsm(addiuInst);
            }
        }
    }

    private void visitGEPInst(GEPInst gepInst) {
        // 局部数组 数组形参 下标小的元素在低地址
        if (gepInst.getBasePointer() instanceof AllocaInst || gepInst.getBasePointer() instanceof Param
                || gepInst.getBasePointer() instanceof GEPInst) {   // 函数内联后，数组形参会被替换为实参，实参的类型为gep，因此被调函数内对形参数组的gep的basePointer的类型便是gep，而不再是alloca
            Value base = gepInst.getBasePointer();  // 内存基地址
            Reg baseReg = Reg.t0;  // 默认为t0，若已分配寄存器再更改
            Reg toReg = gepInst.inReg() ? gepInst.getReg() : Reg.t1;
            if (gepInst.getBasePointer().notInReg()) {  // 若基地址未分配寄存器
                // lw $t0 offset($sp) 数组首元素地址存入t0
                int offset = mipsBuilder.getOffsetOfValue(gepInst.getBasePointer());
                LwInst lwInst = new LwInst(Reg.t0, offset, Reg.sp);
                mipsBuilder.addAsm(lwInst);
            } else {    // 若基地址分配了寄存器
                baseReg = base.getReg();
            }
            Value gepOffset = gepInst.getOffset();  // 目标元素内存地址偏移量
            Reg gepOffsetReg = Reg.t1;  // 默认为t1，若已分配寄存器再更改
            if (gepOffset instanceof Constant constant) {  // 若偏移量为常数
                // addiu $toReg $baseReg eleOffset
                int eleOffset = 4 * constant.getValue(); // 下标小的元素在低地址
                AddiuInst addiuInst = new AddiuInst(toReg, baseReg, eleOffset);
                mipsBuilder.addAsm(addiuInst);
            } else {    // 偏移量不为常数
                if (gepOffset.notInReg()) {   // 若gep偏移量未分配寄存器
                    // lw $t1 offset1($sp)
                    int offset1 = mipsBuilder.getOffsetOfValue(gepOffset);
                    LwInst lwInst1 = new LwInst(Reg.t1, offset1, Reg.sp);
                    mipsBuilder.addAsm(lwInst1);
                } else {    // 若gep偏移量分配了寄存器
                    gepOffsetReg = gepOffset.getReg();
                }
                // sll $t1 $gepOffsetReg 2 偏移量变量 需要*4 转换为地址偏移量 比如g[i]的实际地址偏移量是i*4
                SllInst sllInst = new SllInst(Reg.t1, gepOffsetReg, 2);
                mipsBuilder.addAsm(sllInst);
                // addu $toReg $baseReg $t1 目标元素地址存入toReg
                AdduInst adduInst = new AdduInst(toReg, baseReg, Reg.t1);
                mipsBuilder.addAsm(adduInst);
            }
            if (gepInst.notInReg()) {   // 若gep结果未分配寄存器
                // sw $t1 offset1($sp) 目标元素地址存入栈帧
                int offset1 = mipsBuilder.addValueToCurRecord(gepInst);
                SwInst swInst = new SwInst(Reg.t1, offset1, Reg.sp);
                mipsBuilder.addAsm(swInst);
            }
        } else if (gepInst.getBasePointer() instanceof GlobalArray globalArray) {   // 全局数组 下标小的元素在低地址
            Value gepOffset = gepInst.getOffset();  // 目标元素地址偏移量 gep偏移量
            Reg gepOffsetReg = Reg.t1;  // gep偏移量寄存器 默认为t1，若已分配寄存器再更改
            Reg toReg = gepInst.inReg() ? gepInst.getReg() : Reg.t0;    // 默认为t0
            String arrayLabel = globalArray.getName().substring(1);
            // 目标元素内存地址偏移量，存到t1
            if (gepOffset instanceof Constant constant) { // 若偏移量为常数
                int eleOffset = 4 * constant.getValue();
                // la $toReg arrayLabel+eleOffset   // 优化为常数偏移量的la指令
                LaInst laInst = new LaInst(toReg, arrayLabel, eleOffset);
                mipsBuilder.addAsm(laInst);
            } else {    // 偏移量变量 需要*4 转换为地址偏移量 比如g[i]的实际地址偏移量是i*4
                if (gepOffset.notInReg()) { // 若gep偏移量未分配寄存器
                    // lw $t1 offset($sp)
                    int offset = mipsBuilder.getOffsetOfValue(gepOffset);
                    LwInst lwInst = new LwInst(Reg.t1, offset, Reg.sp);
                    mipsBuilder.addAsm(lwInst);
                } else {    // 若gep偏移量分配到寄存器
                    gepOffsetReg = gepOffset.getReg();
                }
                // sll $t1 $gepOffsetReg 2 偏移量变量 需要*4 转换为地址偏移量 比如g[i]的实际地址偏移量是i*4
                SllInst sllInst = new SllInst(Reg.t1, gepOffsetReg, 2);
                mipsBuilder.addAsm(sllInst);
                // la $toReg arrayLabel($t1) 目标元素地址存到toReg
                LaInst laInst = new LaInst(toReg, globalArray.getName().substring(1), Reg.t1);
                mipsBuilder.addAsm(laInst);
            }
            if (gepInst.notInReg()) {   // 若gep结果即目标元素地址未分配寄存器
                // sw $t0 offset($sp) 目标元素地址存到栈帧
                int offset = mipsBuilder.addValueToCurRecord(gepInst);
                SwInst swInst = new SwInst(Reg.t0, offset, Reg.sp);
                mipsBuilder.addAsm(swInst);
            }
        }
    }

    private void visitStoreInst(StoreInst storeInst) {
        Value from = storeInst.getFrom();
        Value to = storeInst.getTo();
        Reg fromReg = Reg.t0;   // 默认为t0，若分配了寄存器则修改
        // 处理来源 存入t0
        if (from instanceof Constant constant) { // 要存入的是常数, li加载到t0
            // li $t0 constant
            LiInst liInst = new LiInst(Reg.t0, constant.getValue());
            mipsBuilder.addAsm(liInst);
        } else {    // 要存入的不是常数
            if (from.notInReg()) {  // 若未分配寄存器，从栈帧加载
                // lw $t0 offset($sp)
                int offset = mipsBuilder.getOffsetOfValue(from);
                LwInst lwInst = new LwInst(Reg.t0, offset, Reg.sp);
                mipsBuilder.addAsm(lwInst);
            } else {    // 若已分配寄存器，从寄存器加载
                fromReg = from.getReg();
            }
        }
        // 处理目标 把fromReg内容存入内存相应位置
        if (to instanceof GlobalVar globalVar) {  // 如果目标是非数组全局变量
            // sw $fromReg label
            String label = globalVar.getName().substring(1);
            SwInst swInst = new SwInst(fromReg, label);
            mipsBuilder.addAsm(swInst);
        } else if (to instanceof AllocaInst allocaInst) {   // 如果目标是非数组局部变量 // 只有不做消除phi才会出现其alloc，因此不用考虑寄存器
            // sw $fromReg offset($sp)
            int offset = mipsBuilder.getOffsetOfValue(allocaInst);
            SwInst swInst = new SwInst(fromReg, offset, Reg.sp);
            mipsBuilder.addAsm(swInst);
        } else if (to instanceof GEPInst gepInst) { // 如果目标是数组元素 包括全局数组、局部数组、参数数组元素 与load相对应，也要存两次 第一次把地址存入$t1 第二次把数据存入地址
            if (gepInst.notInReg()) {   // 若目标元素地址在内存
                // lw $t1 offset($sp) 取出目标元素内存地址，存到t1
                int offset = mipsBuilder.getOffsetOfValue(gepInst);
                LwInst lwInst = new LwInst(Reg.t1, offset, Reg.sp);
                mipsBuilder.addAsm(lwInst);
                // sw $fromReg 0($t1) 存入目标元素
                SwInst swInst = new SwInst(fromReg, 0, Reg.t1);
                mipsBuilder.addAsm(swInst);
            } else {    // 若目标元素地址在寄存器
                Reg gepReg = gepInst.getReg();
                // sw $fromReg 0($gepReg) 存入目标元素
                SwInst swInst = new SwInst(fromReg, 0, gepReg);
                mipsBuilder.addAsm(swInst);
            }
        }
    }

    private void visitLoadInst(LoadInst loadInst) {
        Reg toReg;  // 保存加载出来的数据的寄存器
        if (loadInst.inReg()) {
            toReg = loadInst.getReg();
        } else {
            toReg = Reg.t0;
        }
        // 取出load源
        if (loadInst.getPointer() instanceof GlobalVar globalVar) {   // 如果数据源是全局变量
            // lw $toReg label
            LwInst lwInst = new LwInst(toReg, globalVar.getName().substring(1));
            mipsBuilder.addAsm(lwInst);
        } else {    // 如果数据源不是全局变量
            // lw $toReg offset($sp)
            if (loadInst.getPointer().notInReg()) { // 若数据源不在寄存器
                int offset = mipsBuilder.getOffsetOfValue(loadInst.getPointer());   // 从活动记录中查询栈帧偏移量
                LwInst lwInst = new LwInst(toReg, offset, Reg.sp);
                mipsBuilder.addAsm(lwInst);
            } else {    // 若数据源在寄存器
                // move $toReg $fromReg
                Reg fromReg = loadInst.getPointer().getReg();
                MoveMIPSInst moveMIPSInst = new MoveMIPSInst(toReg, fromReg);
                mipsBuilder.addAsm(moveMIPSInst);
            }
            if (loadInst.getPointer() instanceof GEPInst) { // 如果load源是gep指令，还需要再取一次，因为栈帧中存的是元素地址，还需要从元素地址中取出元素的值
                // lw $toReg 0($toReg)
                LwInst lwInst1 = new LwInst(toReg, 0, toReg);
                mipsBuilder.addAsm(lwInst1);
            }
        }
        if (loadInst.notInReg()) {  // 若加载目标未分配寄存器，存到栈帧
            // sw $toReg offset1($sp)  存入load目标
            int offset1 = mipsBuilder.addValueToCurRecord(loadInst);
            SwInst swInst = new SwInst(toReg, offset1, Reg.sp);
            mipsBuilder.addAsm(swInst);
        }
    }

    private void visitCallInst(CallInst callInst) {
        if (!callInst.getTargetFunc().isLib()) {    // 不是库函数
            /* 第一步 保存活跃寄存器、ra和sp */
            ArrayList<Reg> activeRegs = callInst.getActiveRegs();
            ArrayList<Inst> insts = callInst.getParentBlock().getInsts();
            Inst nextInst = insts.get(insts.indexOf(callInst) + 1);
            // 需要保存的寄存器是当前指令和下条指令活跃寄存器的交集
            activeRegs.retainAll(nextInst.getActiveRegs());
            for (Reg reg : activeRegs) {
                // sw $reg regOffset($sp)
                int regOffset = mipsBuilder.addRegToCurRecord(reg);
                SwInst swInst = new SwInst(reg, regOffset, Reg.sp);
                mipsBuilder.addAsm(swInst);
            }
            // sw $ra raOffset($sp)
            int raOffset = mipsBuilder.addRegToCurRecord(Reg.ra);
            SwInst swInst = new SwInst(Reg.ra, raOffset, Reg.sp);
            mipsBuilder.addAsm(swInst);
            // sw $sp spOffset($sp)
            int spOffset = mipsBuilder.addRegToCurRecord(Reg.sp);
            SwInst swInst1 = new SwInst(Reg.sp, spOffset, Reg.sp);
            mipsBuilder.addAsm(swInst1);

            /* 第二步 保存实参到寄存器或栈帧 */
            String calleeName = callInst.getTargetFunc().getName(); // 被调函数名
            // 调用者函数栈帧当前偏移量
            int callerOffset = mipsBuilder.getCurRecord().getCurOffset();
            // 保存前四个实参到$ai寄存器，剩余的到被调函数栈帧开头
            ArrayList<Param> params = callInst.getTargetFunc().getParams();
            for (int i = 0; i < params.size(); i++) {
                Value arg = callInst.getArgs().get(i);
                if (i <= 3) {
                    Reg ai = Reg.values()[Reg.a0.ordinal() + i];
                    if (arg instanceof Constant constant) {    // 如果实参是常数
                        // li $ai constant
                        LiInst liInst = new LiInst(ai, constant.getValue());
                        mipsBuilder.addAsm(liInst);
                    } else {    // 如果实参不是常数
                        if (arg.notInReg()) { // 如果实参不在寄存器中
                            // lw $ai offset($sp)
                            int offset = mipsBuilder.getOffsetOfValue(arg); // 在调用者函数中查找实参偏移量
                            LwInst lwInst = new LwInst(ai, offset, Reg.sp);
                            mipsBuilder.addAsm(lwInst);
                        } else {    // 如果实参在寄存器中
                            // move $ai $valueReg
                            Reg valueReg = arg.getReg();
                            MoveMIPSInst moveMIPSInst = new MoveMIPSInst(ai, valueReg);
                            mipsBuilder.addAsm(moveMIPSInst);
                        }
                    }
                } else {
                    Reg valueReg = Reg.t0;  // 默认为t0，若在寄存器内再修改
                    if (arg instanceof Constant constant) {    // 如果实参是常数
                        // li $t0 constant
                        LiInst liInst = new LiInst(Reg.t0, constant.getValue());
                        mipsBuilder.addAsm(liInst);
                    } else {    // 如果实参不是常数
                        if (arg.notInReg()) { // 如果实参不在寄存器中
                            // lw $t0 offset($sp)
                            int offset = mipsBuilder.getOffsetOfValue(arg); // 在调用者函数中查找实参偏移量
                            LwInst lwInst = new LwInst(Reg.t0, offset, Reg.sp);
                            mipsBuilder.addAsm(lwInst);
                        } else {    // 如果实参在寄存器中
                            valueReg = arg.getReg();
                        }
                    }
                    // sw $valueReg (callerOffset+argOffset)($sp) // 把实参存入被调函数的栈帧 （虽然这里lw和sw的是实参，但被调函数record中之前已经存入的是被调函数的形参，而不是调用者函数的实参）
                    int argOffset = mipsBuilder.getRecordByFuncName(calleeName).getOffsetOfValue(params.get(i));    // 查询形参的offset
                    SwInst swInst2 = new SwInst(valueReg, callerOffset + argOffset, Reg.sp);
                    mipsBuilder.addAsm(swInst2);
                }
            }
            // addiu $sp $sp callerOffset 重设sp，使其0地址为首个实参地址，做好调用函数的准备
            AddiuInst addiuInst = new AddiuInst(Reg.sp, Reg.sp, callerOffset);
            mipsBuilder.addAsm(addiuInst);
            // jal func
            JalInst jalInst = new JalInst(calleeName.substring(1));
            mipsBuilder.addAsm(jalInst);

            /* 第三步 恢复sp、ra和活跃寄存器 */
            // lw $sp 4($sp) 恢复sp
            LwInst lwInst = new LwInst(Reg.sp, 4, Reg.sp);
            mipsBuilder.addAsm(lwInst);
            // lw $ra raOffset($sp) 恢复ra
            LwInst lwInst1 = new LwInst(Reg.ra, raOffset, Reg.sp);
            mipsBuilder.addAsm(lwInst1);
            // lw $reg regOffset($sp) 恢复活跃寄存器
            for (Reg reg : activeRegs) {
                int regOffset = mipsBuilder.getOffsetOfReg(reg);
                LwInst lwInst2 = new LwInst(reg, regOffset, Reg.sp);
                mipsBuilder.addAsm(lwInst2);
            }
            if (callInst.getName() != null) {   // 如果调用的函数有返回值
                if (callInst.notInReg()) {  // 若没有分配寄存器，加载到栈帧
                    // sw $v0 retOffset($sp)
                    int retOffset = mipsBuilder.addValueToCurRecord(callInst);
                    SwInst swInst2 = new SwInst(Reg.v0, retOffset, Reg.sp);
                    mipsBuilder.addAsm(swInst2);
                } else {    // 若分配了寄存器，加载到寄存器
                    // move $toReg $v0
                    Reg toReg = callInst.getReg();
                    MoveMIPSInst moveMIPSInst = new MoveMIPSInst(toReg, Reg.v0);
                    mipsBuilder.addAsm(moveMIPSInst);
                }
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
                if (callInst.notInReg()) {  // 若没有分配寄存器，加载到栈帧
                    // sw $v0 offset($sp) 保存到栈帧
                    int offset = mipsBuilder.addValueToCurRecord(callInst);
                    SwInst swInst = new SwInst(Reg.v0, offset, Reg.sp);
                    mipsBuilder.addAsm(swInst);
                } else {    // 若分配了寄存器，加载到寄存器
                    // move $toReg $v0
                    Reg toReg = callInst.getReg();
                    MoveMIPSInst moveMIPSInst = new MoveMIPSInst(toReg, Reg.v0);
                    mipsBuilder.addAsm(moveMIPSInst);
                }
            } else if (function.getName().equals("@putint")) {
                Value arg = callInst.getArgs().get(0);
                if (arg instanceof Constant constant) {  // 常数
                    // li $a0 constant
                    LiInst liInst = new LiInst(Reg.a0, constant.getValue());
                    mipsBuilder.addAsm(liInst);
                } else {    // 不是常数
                    if (arg.notInReg()) {   // 若实参不在寄存器中，从栈帧加载
                        // lw $a0 offset($sp)
                        int offset = mipsBuilder.getOffsetOfValue(arg);
                        LwInst lwInst = new LwInst(Reg.a0, offset, Reg.sp);
                        mipsBuilder.addAsm(lwInst);
                    } else {    // 若实参在寄存器中，从寄存器加载
                        // move $a0 $argReg
                        Reg argReg = arg.getReg();
                        MoveMIPSInst moveMIPSInst = new MoveMIPSInst(Reg.a0, argReg);
                        mipsBuilder.addAsm(moveMIPSInst);
                    }
                }
                // li $v0 1 打印整数
                LiInst liInst = new LiInst(Reg.v0, 1);
                mipsBuilder.addAsm(liInst);
                // syscall
                SyscallInst syscallInst = new SyscallInst();
                mipsBuilder.addAsm(syscallInst);
            } else if (function.getName().equals("@putstr")) {
                Value arg = callInst.getArgs().get(0);
                if (arg.notInReg()) {   // 若字符串地址不在寄存器
                    // lw $a0 offset($sp)
                    int offset = mipsBuilder.getOffsetOfValue(arg);
                    LwInst lwInst = new LwInst(Reg.a0, offset, Reg.sp);
                    mipsBuilder.addAsm(lwInst);
                } else {    // 若字符串地址在寄存器
                    // move $a0 $argReg
                    Reg argReg = arg.getReg();
                    MoveMIPSInst moveMIPSInst = new MoveMIPSInst(Reg.a0, argReg);
                    mipsBuilder.addAsm(moveMIPSInst);
                }
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
        if (returnInst.getParentBlock().getParentFunc().getName().equals("@main")) {
            return;
        }
        Value retVal = returnInst.getValue();
        if (retVal == null) {    // 如果无返回值
            JrInst jrInst = new JrInst(Reg.ra);
            mipsBuilder.addAsm(jrInst);
        } else {    // 有返回值
            if (retVal instanceof Constant constant) {    // 返回值为常数
                // li $v0 constant
                LiInst liInst = new LiInst(Reg.v0, constant.getValue());
                mipsBuilder.addAsm(liInst);
            } else {    // 返回值不是常数
                if (retVal.notInReg()) {    // 不在寄存器内，从栈帧加载
                    // lw $v0 offset($sp)
                    int offset = mipsBuilder.getOffsetOfValue(retVal);
                    LwInst lwInst = new LwInst(Reg.v0, offset, Reg.sp);
                    mipsBuilder.addAsm(lwInst);
                } else {    // 在寄存器内，从寄存器加载
                    // move $v0 $retValReg
                    Reg retValReg = retVal.getReg();
                    MoveMIPSInst moveMIPSInst = new MoveMIPSInst(Reg.v0, retValReg);
                    mipsBuilder.addAsm(moveMIPSInst);
                }
            }
            // jr $ra
            JrInst jrInst = new JrInst(Reg.ra);
            mipsBuilder.addAsm(jrInst);
        }
    }

    private void visitJumpInst(JumpInst jumpInst) {
        JInst jInst = new JInst(jumpInst.getTargetBasicBlock().getName());
        mipsBuilder.addAsm(jInst);
    }

    private void visitBranchInst(BranchInst branchInst) {
        Value cond = branchInst.getCond();
        Reg condReg = Reg.t0;
        if (cond instanceof Constant constant) {    // cond为常数
            // li $t0 constant
            LiInst liInst = new LiInst(Reg.t0, constant.getValue());
            mipsBuilder.addAsm(liInst);
        } else if (cond.notInReg()) {  // cond在内存中
            // lw $t0 offset($sp) 获取条件，存入t0
            int offset = mipsBuilder.getOffsetOfValue(cond);
            LwInst lwInst = new LwInst(Reg.t0, offset, Reg.sp);
            mipsBuilder.addAsm(lwInst);
        } else {    // cond在寄存器中
            condReg = cond.getReg();
        }
        // beqz $condReg falseLabel 若条件为假，则跳转到假基本块
        BeqzInst beqzInst = new BeqzInst(condReg, branchInst.getFalseBlock().getName());
        mipsBuilder.addAsm(beqzInst);
        // j trueLabel 若条件为真，则跳转到真基本块
        JInst jInst = new JInst(branchInst.getTrueBlock().getName());
        mipsBuilder.addAsm(jInst);
    }

    private void visitZextInst(ZextInst zextInst) {
        Value ori = zextInst.getOriValue();
        if (ori.notInReg() && zextInst.notInReg()) {    // ori和zext都在内存
            int offset = mipsBuilder.getOffsetOfValue(ori);  // zext的offset是其ori的offset
            mipsBuilder.addValueWithOffsetToCurRecord(zextInst, offset);
        } else if (ori.inReg() && zextInst.inReg()) {   // ori和zext都在寄存器
            // move $toReg $oriReg
            Reg toReg = zextInst.getReg();
            Reg oriReg = ori.getReg();
            MoveMIPSInst moveMIPSInst = new MoveMIPSInst(toReg, oriReg);
            mipsBuilder.addAsm(moveMIPSInst);
        } else if (ori.inReg() && zextInst.notInReg()) {    // ori在寄存器，zext在内存
            // 添加zext到record
            int offset = mipsBuilder.addValueToCurRecord(zextInst);
            // sw $oriReg offset($sp)
            Reg oriReg = ori.getReg();
            SwInst swInst = new SwInst(oriReg, offset, Reg.sp);
            mipsBuilder.addAsm(swInst);
        } else {    // ori在内存，zext在寄存器
            // lw $toReg offset($sp)
            Reg toReg = zextInst.getReg();
            int offset = mipsBuilder.getOffsetOfValue(ori);
            LwInst lwInst = new LwInst(toReg, offset, Reg.sp);
            mipsBuilder.addAsm(lwInst);
        }
    }

    private void visitIcmpInst(IcmpInst icmpInst) {
        // 准备操作数
        Value op1 = icmpInst.getOperand1();
        Value op2 = icmpInst.getOperand2();

        // 准备寄存器
        Reg op1Reg = op1.inReg() ? op1.getReg() : Reg.t0;    // 操作数1的寄存器
        Reg op2Reg = op2.inReg() ? op2.getReg() : Reg.t1;    // 操作数2的寄存器
        Reg toReg = icmpInst.inReg() ? icmpInst.getReg() : Reg.t0;     // 保存结果的寄存器

        if (op1 instanceof Constant constant1 && op2 instanceof Constant constant2) {   // 如果两个都是常数，可以直接比较，优化为li
            IcmpInst.IcmpKind icmpKind = icmpInst.getIcmpKind();
            int v1 = constant1.getValue();
            int v2 = constant2.getValue();
            int cond;
            if (icmpKind == IcmpInst.IcmpKind.eq) {
                cond = v1 == v2 ? 1 : 0;
            } else if (icmpKind == IcmpInst.IcmpKind.ne) {
                cond = v1 != v2 ? 1 : 0;
            } else if (icmpKind == IcmpInst.IcmpKind.sgt) {
                cond = v1 > v2 ? 1 : 0;
            } else if (icmpKind == IcmpInst.IcmpKind.sge) {
                cond = v1 >= v2 ? 1 : 0;
            } else if (icmpKind == IcmpInst.IcmpKind.slt) {
                cond = v1 < v2 ? 1 : 0;
            } else {
                assert icmpKind == IcmpInst.IcmpKind.sle;
                cond = v1 <= v2 ? 1 : 0;
            }
            // li $toReg cond
            LiInst liInst = new LiInst(toReg, cond);
            mipsBuilder.addAsm(liInst);
        } else {    // 如果存在非常数
            // 获取操作数1
            if (op1 instanceof Constant constant) {    // 常数直接li
                // li $t0 constant
                LiInst liInst = new LiInst(Reg.t0, constant.getValue());
                mipsBuilder.addAsm(liInst);
            } else if (op1.notInReg()) {  // 不在寄存器中，从内存加载
                int offset = mipsBuilder.getOffsetOfValue(op1);
                LwInst lwInst = new LwInst(Reg.t0, offset, Reg.sp);
                mipsBuilder.addAsm(lwInst);
            }

            // 获取操作数2
            if (op2 instanceof Constant constant) {    // 常数直接li
                // li $t1 constant
                LiInst liInst = new LiInst(Reg.t1, constant.getValue());
                mipsBuilder.addAsm(liInst);
            } else if (op2.notInReg()) {  // 不在寄存器中，从内存加载
                // lw $t1 offset($sp)
                int offset = mipsBuilder.getOffsetOfValue(op2);
                LwInst lwInst = new LwInst(Reg.t1, offset, Reg.sp);
                mipsBuilder.addAsm(lwInst);

            }
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
            // sxx $toReg $op1Reg $op2Reg
            SetCmpInst setCmpInst = new SetCmpInst(opcode, toReg, op1Reg, op2Reg);
            mipsBuilder.addAsm(setCmpInst);
            if (icmpInst.notInReg()) {  // 若结果未分配寄存器，存到内存
                // 添加到record
                int resOffset = mipsBuilder.addValueToCurRecord(icmpInst);
                // sw $t0 resOffset($sp)
                SwInst swInst = new SwInst(Reg.t0, resOffset, Reg.sp);
                mipsBuilder.addAsm(swInst);
            }
        }
    }

    /**
     * 将value从内存加载到寄存器。
     * lw $reg offset($sp)
     */
    private void loadValueToReg(Value value, Reg reg) {
        int offset = mipsBuilder.getOffsetOfValue(value);
        LwInst lwInst = new LwInst(reg, offset, Reg.sp);
        mipsBuilder.addAsm(lwInst);
    }

    private void visitBinaryInst(BinaryInst binaryInst) {
        // 准备操作数
        Value op1 = binaryInst.getOperand1();
        Value op2 = binaryInst.getOperand2();

        // 准备寄存器
        Reg op1Reg = op1.inReg() ? op1.getReg() : Reg.t0;
        Reg op2Reg = op2.inReg() ? op2.getReg() : Reg.t1;
        Reg toReg = binaryInst.inReg() ? binaryInst.getReg() : Reg.t0;

        // 分情况解析并优化
        switch (binaryInst.getOpcode()) {
            case add -> {
                if (op1 instanceof Constant constant1 && op2 instanceof Constant constant2) {   // 两个加数都是常数 常数一定不会分配寄存器
                    // 直接计算和
                    int res = constant1.getValue() + constant2.getValue();
                    // li $toReg res
                    LiInst liInst = new LiInst(toReg, res);
                    mipsBuilder.addAsm(liInst);
                } else if (op1 instanceof Constant || op2 instanceof Constant) {    // 只有加数为一个常数
                    Constant constant = (Constant) (op1 instanceof Constant ? op1 : op2);
                    Value op = op1 instanceof Constant ? op2 : op1;
                    Reg opReg = op.equals(op1) ? op1Reg : op2Reg;   // 这里不要写错，不要调用getReg
                    if (constant.getValue() == 0) { // 如果这个常数是0 可以优化为lw或move
                        if (op.notInReg()) {
                            // lw $toReg offset($sp) 直接加载到toReg即可
                            loadValueToReg(op, toReg);
                        } else {
                            // move $toReg $opReg   使用move，增加合并机会(例如，若toReg和opReg相同，会被后端优化)
                            MoveMIPSInst moveMIPSInst = new MoveMIPSInst(toReg, opReg);
                            mipsBuilder.addAsm(moveMIPSInst);
                        }
                    } else {    // 如果这个常数不是0 可以优化为addiu
                        if (op.notInReg()) {
                            loadValueToReg(op, opReg);
                        }
                        // addiu $toReg $opReg constant
                        AddiuInst addiuInst = new AddiuInst(toReg, opReg, constant.getValue());
                        mipsBuilder.addAsm(addiuInst);
                    }
                } else {    // 两个加数都不是常数 无法优化
                    if (op1.notInReg()) {
                        loadValueToReg(op1, op1Reg);
                    }
                    if (op2.notInReg()) {
                        loadValueToReg(op2, op2Reg);
                    }
                    // addu $toReg $op1Reg $op2Reg
                    AdduInst adduInst = new AdduInst(toReg, op1Reg, op2Reg);
                    mipsBuilder.addAsm(adduInst);
                }
            }
            case sub -> {
                if (op1 instanceof Constant constant1 && op2 instanceof Constant constant2) {   // 两个都是常数
                    // 直接计算差
                    int res = constant1.getValue() - constant2.getValue();
                    // li $toReg res
                    LiInst liInst = new LiInst(toReg, res);
                    mipsBuilder.addAsm(liInst);
                } else if (!(op1 instanceof Constant) && op2 instanceof Constant constant) {    // 如果只有减数为常数
                    if (constant.getValue() == 0) { // 如果减数为0 可以优化为lw或move
                        if (op1.notInReg()) {   // 被减数在内存
                            // lw $toReg offset($sp) 直接加载到toReg即可
                            loadValueToReg(op1, toReg);
                        } else {    // 被减数在寄存器
                            // move $toReg $opReg
                            MoveMIPSInst moveMIPSInst = new MoveMIPSInst(toReg, op1Reg);
                            mipsBuilder.addAsm(moveMIPSInst);
                        }
                    } else {    // 如果减数不为0，可以将减数取反，将subu优化为addiu
                        if (op1.notInReg()) {
                            loadValueToReg(op1, op1Reg);
                        }
                        // addiu $toReg $op1Reg $constantOpposite
                        int constantOpposite = -constant.getValue();
                        AddiuInst addiuInst = new AddiuInst(toReg, op1Reg, constantOpposite);
                        mipsBuilder.addAsm(addiuInst);
                    }
                } else {    // 其他情况 即被减数为常数或不为常数，减数不为常数 无法优化
                    if (op1 instanceof Constant constant) { // 被减数为常数
                        // li $op1Reg constant
                        LiInst liInst = new LiInst(op1Reg, constant.getValue());
                        mipsBuilder.addAsm(liInst);
                    } else if (op1.notInReg()) {
                        loadValueToReg(op1, op1Reg);
                    }
                    if (op2.notInReg()) {
                        loadValueToReg(op2, op2Reg);
                    }
                    // subu $toReg $op1Reg $op2Reg
                    SubuInst subuInst = new SubuInst(toReg, op1Reg, op2Reg);
                    mipsBuilder.addAsm(subuInst);
                }
            }
            case mul -> {
                if (op1 instanceof Constant constant1 && op2 instanceof Constant constant2) {   // 两个因子都是常数
                    // 直接计算乘积
                    int res = constant1.getValue() * constant2.getValue();
                    // li $toReg res
                    LiInst liInst = new LiInst(toReg, res);
                    mipsBuilder.addAsm(liInst);
                } else if (op1 instanceof Constant || op2 instanceof Constant) {    // 只有一个因子为常数
                    Constant constant = (Constant) (op1 instanceof Constant ? op1 : op2);
                    Value op = op1 instanceof Constant ? op2 : op1;
                    Reg opReg = op.equals(op1) ? op1Reg : op2Reg;
                    Reg constantReg = op.equals(op1) ? op2Reg : op1Reg; // constantReg一定是t0或t1
                    int constantVal = constant.getValue();
                    if (constantVal == 0) { // 若常数因子为0 直接li
                        // li $toReg 0
                        LiInst liInst = new LiInst(toReg, 0);
                        mipsBuilder.addAsm(liInst);
                    } else if (constantVal == 1) {  // 若常数因子为1, 可以优化为lw或move
                        if (op.notInReg()) {
                            // lw $toReg offset($sp)
                            loadValueToReg(op, toReg);
                        } else {
                            // move $toReg $opReg
                            MoveMIPSInst moveMIPSInst = new MoveMIPSInst(toReg, opReg);
                            mipsBuilder.addAsm(moveMIPSInst);
                        }
                    } else if (constantVal == -1) { // 若常数因子为-1, 可以优化为lw+subu 或 subu
                        if (op.notInReg()) {
                            // lw $toReg offset($sp)
                            loadValueToReg(op, toReg);
                            // subu $toReg $zero $toReg
                            SubuInst subuInst = new SubuInst(toReg, Reg.zero, toReg);
                            mipsBuilder.addAsm(subuInst);
                        } else {
                            // subu $toReg $zero $opReg
                            SubuInst subuInst = new SubuInst(toReg, Reg.zero, opReg);
                            mipsBuilder.addAsm(subuInst);
                        }
                    } else if (constantVal >= 2 && isPowerOfTwo(constantVal)) { // a*(2^n) 优化成 a<<n (2^n >= 2)  比如a*4优化成a<<2
                        int n = getPowerOfTwo(constantVal);
                        if (op.notInReg()) {
                            loadValueToReg(op, opReg);
                        }
                        // sll $toReg $opReg n
                        SllInst sllInst = new SllInst(toReg, opReg, n);
                        mipsBuilder.addAsm(sllInst);
                    } else if (constantVal <= -2 && isPowerOfTwo(-constantVal)) {   // a*(-2^n) 优化成 -(a<<n)     比如a*(-4)优化成-(a<<2)
                        int n = getPowerOfTwo(-constantVal);
                        if (op.notInReg()) {
                            loadValueToReg(op, opReg);
                        }
                        // sll $toReg $opReg n
                        SllInst sllInst = new SllInst(toReg, opReg, n);
                        mipsBuilder.addAsm(sllInst);
                        // subu $toReg $zero $toReg
                        SubuInst subuInst = new SubuInst(toReg, Reg.zero, toReg);
                        mipsBuilder.addAsm(subuInst);
                    } else if (constantVal >= 3 && canSplitToTwoSllPlus(constantVal)) {     // a*(2^m+2^n)      优化成 (a<<m)+(a<<n) 若n为0，则是(a<<m)+a
                        int[] res = splitToTwoSllPlus(constantVal);
                        int m = res[0];
                        int n = res[1];
                        if (op.notInReg()) {
                            loadValueToReg(op, opReg);
                        }
                        // sll $t2 $opReg m     // 这里不得不使用t2，不能用toReg，toReg只能在最后储存结果，因为opReg可能和toReg分配到同一个寄存器
                        SllInst sllInst = new SllInst(Reg.t2, opReg, m);
                        mipsBuilder.addAsm(sllInst);

                        if (n > 0) {
                            // sll $constantReg $opReg n (n为0时则省略)      // 注意这里结果不能用opReg, 因为不能修改活跃寄存器
                            SllInst sllInst1 = new SllInst(constantReg, opReg, n);
                            mipsBuilder.addAsm(sllInst1);
                            // addu $toReg $t2 $constantReg
                            AdduInst adduInst = new AdduInst(toReg, Reg.t2, constantReg);
                            mipsBuilder.addAsm(adduInst);
                        } else {
                            assert n == 0;
                            // addu $toReg $t2 $opReg
                            AdduInst adduInst = new AdduInst(toReg, Reg.t2, opReg);
                            mipsBuilder.addAsm(adduInst);
                        }

                    } else if (constantVal <= -3 && canSplitToTwoSllPlus(-constantVal)) {   // a*(-(2^m+2^n))   优化成 -((a<<m)+(a<<n))
                        int[] res = splitToTwoSllPlus(-constantVal);
                        int m = res[0];
                        int n = res[1];
                        if (op.notInReg()) {
                            loadValueToReg(op, opReg);
                        }
                        // sll $t2 $opReg m
                        SllInst sllInst = new SllInst(Reg.t2, opReg, m);
                        mipsBuilder.addAsm(sllInst);

                        if (n > 0) {
                            // sll $constantReg $opReg n (n为0时则省略)      // 注意这里结果不能用opReg, 因为不能修改活跃寄存器
                            SllInst sllInst1 = new SllInst(constantReg, opReg, n);
                            mipsBuilder.addAsm(sllInst1);
                            // addu $toReg $t2 $constantReg
                            AdduInst adduInst = new AdduInst(toReg, Reg.t2, constantReg);
                            mipsBuilder.addAsm(adduInst);
                        } else {
                            assert n == 0;
                            // addu $toReg $t2 $opReg
                            AdduInst adduInst = new AdduInst(toReg, Reg.t2, opReg);
                            mipsBuilder.addAsm(adduInst);
                        }

                        // subu $toReg $zero $toReg
                        SubuInst subuInst = new SubuInst(toReg, Reg.zero, toReg);
                        mipsBuilder.addAsm(subuInst);

                    } else if (constantVal >= 3 && canSplitToTwoSllMinus(constantVal)) {    // a*(2^m-2^n)      优化成 (a<<m)-(a<<n) 若n为0，则是(a<<m)-a
                        int[] res = splitToTwoSllMinus(constantVal);
                        int m = res[0];
                        int n = res[1];
                        if (op.notInReg()) {
                            loadValueToReg(op, opReg);
                        }
                        // sll $t2 $opReg m
                        SllInst sllInst = new SllInst(Reg.t2, opReg, m);
                        mipsBuilder.addAsm(sllInst);

                        if (n > 0) {
                            // sll $constantReg $opReg n (n为0时则省略)      // 注意这里结果不能用opReg, 因为不能修改活跃寄存器
                            SllInst sllInst1 = new SllInst(constantReg, opReg, n);
                            mipsBuilder.addAsm(sllInst1);
                            // subu $toReg $t2 $constantReg
                            SubuInst subuInst = new SubuInst(toReg, Reg.t2, constantReg);
                            mipsBuilder.addAsm(subuInst);
                        } else {
                            assert n == 0;
                            // subu $toReg $t2 $opReg
                            SubuInst subuInst = new SubuInst(toReg, Reg.t2, opReg);
                            mipsBuilder.addAsm(subuInst);
                        }

                    } else if (constantVal <= -3 && canSplitToTwoSllMinus(constantVal)) {   // a*(-(2^m-2^n))   优化成 (a<<n)-(a<<m) 若n为0，则是a-(a<<m)
                        int[] res = splitToTwoSllMinus(constantVal);
                        int m = res[0];
                        int n = res[1];
                        if (op.notInReg()) {
                            loadValueToReg(op, opReg);
                        }
                        // sll $t2 $opReg m
                        SllInst sllInst = new SllInst(Reg.t2, opReg, m);
                        mipsBuilder.addAsm(sllInst);

                        if (n > 0) {
                            // sll $constantReg $opReg n (n为0时则省略)      // 注意这里结果不能用opReg, 因为不能修改活跃寄存器
                            SllInst sllInst1 = new SllInst(constantReg, opReg, n);
                            mipsBuilder.addAsm(sllInst1);
                            // subu $toReg $constantReg $t2
                            SubuInst subuInst = new SubuInst(toReg, constantReg, Reg.t2);
                            mipsBuilder.addAsm(subuInst);
                        } else {
                            assert n == 0;
                            // subu $toReg $opReg $t2
                            SubuInst subuInst = new SubuInst(toReg, opReg, Reg.t2);
                            mipsBuilder.addAsm(subuInst);
                        }
                    } else {    // 无法优化成移位
                        // 加载非常数因子
                        if (op.notInReg()) {
                            loadValueToReg(op, opReg);
                        }
                        // 加载常数因子
                        // li $constantReg constantVal
                        LiInst liInst = new LiInst(constantReg, constantVal);
                        mipsBuilder.addAsm(liInst);
                        // mult $op1Reg $op2Reg
                        MultInst multInst = new MultInst(op1Reg, op2Reg);
                        mipsBuilder.addAsm(multInst);
                        // mflo $toReg
                        MfHiloInst mfloInst = new MfHiloInst(Opcode.mflo, toReg);
                        mipsBuilder.addAsm(mfloInst);
                    }
                } else {    // 两个因子都不是常数 无法优化
                    if (op1.notInReg()) {
                        loadValueToReg(op1, op1Reg);
                    }
                    if (op2.notInReg()) {
                        loadValueToReg(op2, op2Reg);
                    }
                    // mult $op1Reg $op2Reg
                    MultInst multInst = new MultInst(op1Reg, op2Reg);
                    mipsBuilder.addAsm(multInst);
                    // mflo $toReg
                    MfHiloInst mfloInst = new MfHiloInst(Opcode.mflo, toReg);
                    mipsBuilder.addAsm(mfloInst);
                }
            }
            case sdiv -> {
                if (op1 instanceof Constant constant1 && op2 instanceof Constant constant2) {
                    // 直接计算商
                    int res = constant1.getValue() / constant2.getValue();
                    // li $toReg res
                    LiInst liInst = new LiInst(toReg, res);
                    mipsBuilder.addAsm(liInst);
                } else if (op2 instanceof Constant constantDivisor) {  // 如果只有除数是常数
                    int constantDivisorVal = constantDivisor.getValue();
                    if (constantDivisorVal == 1) {     // 除数为1
                        if (op1.notInReg()) {
                            // lw $toReg offset($sp)
                            loadValueToReg(op1, toReg);
                        } else {
                            // move $toReg $op1Reg
                            MoveMIPSInst moveMIPSInst = new MoveMIPSInst(toReg, op1Reg);
                            mipsBuilder.addAsm(moveMIPSInst);
                        }
                    } else if (constantDivisorVal == -1) {     // 除数为-1
                        if (op1.notInReg()) {
                            // lw $toReg offset($sp)
                            loadValueToReg(op1, toReg);
                            // subu $toReg $zero $toReg
                            SubuInst subuInst = new SubuInst(toReg, Reg.zero, toReg);
                            mipsBuilder.addAsm(subuInst);
                        } else {
                            // subu $toReg $zero $op1Reg
                            SubuInst subuInst = new SubuInst(toReg, Reg.zero, op1Reg);
                            mipsBuilder.addAsm(subuInst);
                        }
                    } else if (constantDivisorVal >= 2 && isPowerOfTwo(constantDivisorVal)) {     // a/(2^n) 优化成 a>>n
                        int n = getPowerOfTwo(constantDivisorVal);
                        if (op1.notInReg()) {
                            loadValueToReg(op1, op1Reg);
                        }
                        // sra $t2 $op1Reg 31   加数 = 原被除数 >> 31（算数右移）
                        SraInst sraInst = new SraInst(Reg.t2, op1Reg, 31);
                        mipsBuilder.addAsm(sraInst);
                        // srl $t2 $t2 (32-n)        加数 = 加数 >> (32-n) （逻辑右移）
                        SrlInst srlInst = new SrlInst(Reg.t2, Reg.t2, 32 - n);
                        mipsBuilder.addAsm(srlInst);
                        // addu $t2 $op1Reg $t2 新被除数 = 原被除数 + 加数
                        AdduInst adduInst = new AdduInst(Reg.t2, op1Reg, Reg.t2);
                        mipsBuilder.addAsm(adduInst);
                        // sra $toReg $t2 n     商 = 新被除数 >> n (算数右移)
                        SraInst sraInst1 = new SraInst(toReg, Reg.t2, n);
                        mipsBuilder.addAsm(sraInst1);
                    } else if (constantDivisorVal <= -2 && isPowerOfTwo(-constantDivisorVal)) {   // a/(-2^n) 优化成 -(a>>n)
                        int n = getPowerOfTwo(-constantDivisorVal);
                        if (op1.notInReg()) {
                            loadValueToReg(op1, op1Reg);
                        }
                        // sra $t2 $op1Reg 31   加数 = 原被除数 >> 31（算数右移）
                        SraInst sraInst = new SraInst(Reg.t2, op1Reg, 31);
                        mipsBuilder.addAsm(sraInst);
                        // srl $t2 $t2 (32-n)        加数 = 加数 >> (32-n) （逻辑右移）
                        SrlInst srlInst = new SrlInst(Reg.t2, Reg.t2, 32 - n);
                        mipsBuilder.addAsm(srlInst);
                        // addu $t2 $op1Reg $t2 新被除数 = 原被除数 + 加数
                        AdduInst adduInst = new AdduInst(Reg.t2, op1Reg, Reg.t2);
                        mipsBuilder.addAsm(adduInst);
                        // sra $toReg $t2 n     商 = 新被除数 >> n (算数右移)
                        SraInst sraInst1 = new SraInst(toReg, Reg.t2, n);
                        mipsBuilder.addAsm(sraInst1);
                        // subu $toReg $zero $toReg
                        SubuInst subuInst = new SubuInst(toReg, Reg.zero, toReg);
                        mipsBuilder.addAsm(subuInst);
                    } else {    // 进行除常数优化
                        int abs = constantDivisorVal >= 0 ? constantDivisorVal : -constantDivisorVal;
                        // nc = 2^31 - 2^31 % abs - 1
                        long nc = (1L << 31) - ((1L << 31) % abs) - 1;
                        long p = 32;
                        // 2^p > (2^31 - 2^31 % abs - 1) * (abs - 2^p % abs)
                        while ((1L << p) <= nc * (abs - (1L << p) % abs)) {
                            p++;
                        }
                        // m = (2^p + abs - 2^p % abs) / abs    即m是2^p/abs向上取整
                        long m = ((1L << p) + abs - (1L << p) % abs) / abs;
                        // n = m[31:0]
                        int n = (int) ((m << 32) >>> 32);
                        int shift = (int) (p - 32);

                        Reg temp0 = Reg.t1;
                        Reg temp1 = Reg.t2;

                        // li $tmp0 n
                        LiInst liInst = new LiInst(temp0, n);
                        mipsBuilder.addAsm(liInst);

                        if (m >= 0x80000000L) { // temp1 = op1 + (op1*n)[63:32]
                            // mthi $op1Reg
                            MthiInst mthiInst = new MthiInst(op1Reg);
                            mipsBuilder.addAsm(mthiInst);

                            // madd $op1Reg $temp0
                            MaddInst maddInst = new MaddInst(op1Reg, temp0);
                            mipsBuilder.addAsm(maddInst);

                            // mfhi $temp1
                            MfHiloInst mfhiInst = new MfHiloInst(Opcode.mfhi, temp1);
                            mipsBuilder.addAsm(mfhiInst);
                        } else {    // temp1 = (op1*n)[63:32]
                            // mult $op1Reg $temp0
                            MultInst multInst = new MultInst(op1Reg, temp0);
                            mipsBuilder.addAsm(multInst);

                            // mfhi $temp1
                            MfHiloInst mfhiInst = new MfHiloInst(Opcode.mfhi, temp1);
                            mipsBuilder.addAsm(mfhiInst);
                        }
                        // 最后，to = (temp1>>shift) + (op1>>31)
                        // sra $temp1 $temp1 shift
                        SraInst sraInst = new SraInst(temp1, temp1, shift);
                        mipsBuilder.addAsm(sraInst);

                        // srl $temp0 $op1Reg 31
                        SrlInst srlInst = new SrlInst(temp0, op1Reg, 31);
                        mipsBuilder.addAsm(srlInst);

                        // addu $toReg $temp1 $temp0
                        AdduInst adduInst = new AdduInst(toReg, temp1, temp0);
                        mipsBuilder.addAsm(adduInst);

                        if (constantDivisorVal < 0) {
                            // subu $toReg $zero $toReg
                            SubuInst subuInst = new SubuInst(toReg, Reg.zero, toReg);
                            mipsBuilder.addAsm(subuInst);
                        }
                    }
                } else if (op1 instanceof Constant constant && constant.getValue() == 0) {  // 如果被除数是0，优化成li
                    // li $toReg 0
                    LiInst liInst = new LiInst(toReg, 0);
                    mipsBuilder.addAsm(liInst);
                } else {
                    if (op1 instanceof Constant constant) {
                        // li $op1Reg constant
                        LiInst liInst = new LiInst(op1Reg, constant.getValue());
                        mipsBuilder.addAsm(liInst);
                    } else if (op1.notInReg()) {
                        loadValueToReg(op1, op1Reg);
                    }
                    if (op2.notInReg()) {
                        loadValueToReg(op2, op2Reg);
                    }
                    // div $op1Reg $op2Reg
                    DivInst divInst = new DivInst(op1Reg, op2Reg);
                    mipsBuilder.addAsm(divInst);
                    // mflo $toReg
                    MfHiloInst mfloInst = new MfHiloInst(Opcode.mflo, toReg);
                    mipsBuilder.addAsm(mfloInst);
                }
            }
            case srem -> {
                if (op1 instanceof Constant constant1 && op2 instanceof Constant constant2) {
                    // 直接计算余数
                    int res = constant1.getValue() % constant2.getValue();
                    // li $toReg res
                    LiInst liInst = new LiInst(toReg, res);
                    mipsBuilder.addAsm(liInst);
                } else {
                    if (op1 instanceof Constant constant) {
                        // li $op1Reg constant
                        LiInst liInst = new LiInst(op1Reg, constant.getValue());
                        mipsBuilder.addAsm(liInst);
                    } else if (op1.notInReg()) {
                        loadValueToReg(op1, op1Reg);
                    }
                    if (op2 instanceof Constant constant) {
                        // li $op2Reg constant
                        LiInst liInst = new LiInst(op2Reg, constant.getValue());
                        mipsBuilder.addAsm(liInst);
                    } else if (op2.notInReg()) {
                        loadValueToReg(op2, op2Reg);
                    }
                    // div $op1Reg $op2Reg
                    DivInst divInst = new DivInst(op1Reg, op2Reg);
                    mipsBuilder.addAsm(divInst);
                    // mfhi $toReg
                    MfHiloInst mfhiInst = new MfHiloInst(Opcode.mfhi, toReg);
                    mipsBuilder.addAsm(mfhiInst);
                }
            }
        }
        if (binaryInst.notInReg()) { // 若未分配寄存器，将结果保存到栈帧
            // 添加到record
            int resOffset = mipsBuilder.addValueToCurRecord(binaryInst);
            // sw $t0 resOffset($sp)
            SwInst swInst = new SwInst(Reg.t0, resOffset, Reg.sp);
            mipsBuilder.addAsm(swInst);
        }
    }

    /**
     * 判断number是不是2^n。
     */
    private boolean isPowerOfTwo(int num) {
        return (num > 0) && ((num & (num - 1)) == 0);
    }

    /**
     * 若number==2^n，返回n。
     */
    private int getPowerOfTwo(int num) {
        assert isPowerOfTwo(num) : "错误，number不等于2^n";
        int count = 0;
        while (num > 1) {
            num >>= 1;
            count++;
        }
        return count;
    }

    /**
     * 判断一个正数能否拆成两次移位运算之和，即num==2^m+2^n，其中m>=0, n>=0。
     */
    private boolean canSplitToTwoSllPlus(int num) {
        String binaryString = Integer.toBinaryString(num);
        int oriLen = binaryString.length();
        binaryString = binaryString.replace("1", "");
        int newLen = binaryString.length();
        // 判断条件是，该正数二进制表示中，'1'出现了两次
        return num > 0 && (oriLen - newLen == 2);
    }

    /**
     * 若正数能被拆成两次移位运算之和，即num==2^m+2^n，则按降序返回m和n。
     */
    private int[] splitToTwoSllPlus(int num) {
        assert canSplitToTwoSllPlus(num) : "错误，num不能拆分为两次移位运算之和";
        int[] result = new int[2];
        String binaryString = Integer.toBinaryString(num);
        int len = binaryString.length();

        int m = len - 1 - binaryString.indexOf('1');
        int n = len - 1 - binaryString.lastIndexOf('1');

        result[0] = m;
        result[1] = n;

        return result;
    }

    /**
     * 判断一个正数能否拆成两次移位运算之差，即num==2^m-2^n，其中m>=0, n>=0。
     */
    private boolean canSplitToTwoSllMinus(int num) {
        if (num <= 0) {
            return false;
        }
        String binaryString = Integer.toBinaryString(num);
        ArrayList<Integer> oneIndexs = new ArrayList<>();
        // 统计'1'的下标
        for (int i = 0; i < binaryString.length(); i++) {
            if (binaryString.charAt(i) == '1') {
                oneIndexs.add(i);
            }
        }
        // 判断'1'的下标是否连续
        for (int i = 0; i < oneIndexs.size() - 1; i++) {
            if (oneIndexs.get(i) + 1 != oneIndexs.get(i + 1)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 若正数能被拆成两次移位运算之差，即num==2^m-2^n，则按降序返回m和n。
     */
    private int[] splitToTwoSllMinus(int num) {
        assert canSplitToTwoSllMinus(num) : "错误，num不能拆分为两次移位运算之差";
        int[] result = new int[2];
        String binaryString = Integer.toBinaryString(num);
        int len = binaryString.length();

        int m = len - binaryString.indexOf('1');
        int n = len - 1 - binaryString.lastIndexOf('1');

        result[0] = m;
        result[1] = n;

        return result;
    }

    private void visitMoveInst(MoveInst moveInst) {
        Value from = moveInst.getFrom();
        Value to = moveInst.getTo();
        if (from.inReg() && to.inReg()) {   // from和to都在寄存器
            // move $toReg $fromReg
            MoveMIPSInst moveMIPSInst = new MoveMIPSInst(to.getReg(), from.getReg());
            mipsBuilder.addAsm(moveMIPSInst);
        } else if (from.notInReg() && to.inReg()) { // from不在寄存器，to在寄存器
            if (from instanceof Constant constant) { // from是常数
                // li $toReg constant
                LiInst liInst = new LiInst(to.getReg(), constant.getValue());
                mipsBuilder.addAsm(liInst);
            } else {
                // lw $toReg offset($sp)
                if (mipsBuilder.isValueNotInCurRecord(from)) {
                    mipsBuilder.addValueToCurRecord(from);
                }
                int offset = mipsBuilder.getOffsetOfValue(from);
                LwInst lwInst = new LwInst(to.getReg(), offset, Reg.sp);
                mipsBuilder.addAsm(lwInst);
            }
        } else if (from.notInReg() && to.notInReg()) {  // from和to都不在寄存器
            if (from instanceof Constant constant) {
                // li $t0 constant
                LiInst liInst = new LiInst(Reg.t0, constant.getValue());
                mipsBuilder.addAsm(liInst);
                // sw $t0 offset($sp)
                if (mipsBuilder.isValueNotInCurRecord(to)) {
                    mipsBuilder.addValueToCurRecord(to);
                }
                int offset = mipsBuilder.getOffsetOfValue(to);
                SwInst swInst = new SwInst(Reg.t0, offset, Reg.sp);
                mipsBuilder.addAsm(swInst);
            } else {
                // lw $t0 offset0($sp)
                if (mipsBuilder.isValueNotInCurRecord(from)) {
                    mipsBuilder.addValueToCurRecord(from);
                }
                int offset0 = mipsBuilder.getOffsetOfValue(from);
                LwInst lwInst = new LwInst(Reg.t0, offset0, Reg.sp);
                mipsBuilder.addAsm(lwInst);
                // sw $t0 offset1($sp)
                if (mipsBuilder.isValueNotInCurRecord(to)) {
                    mipsBuilder.addValueToCurRecord(to);
                }
                int offset1 = mipsBuilder.getOffsetOfValue(to);
                SwInst swInst = new SwInst(Reg.t0, offset1, Reg.sp);
                mipsBuilder.addAsm(swInst);
            }
        } else {    // from在寄存器，to不在寄存器
            // sw $fromReg offset($sp)
            Reg fromReg = from.getReg();
            if (mipsBuilder.isValueNotInCurRecord(to)) {
                mipsBuilder.addValueToCurRecord(to);
            }
            int offset = mipsBuilder.getOffsetOfValue(to);
            SwInst swInst = new SwInst(fromReg, offset, Reg.sp);
            mipsBuilder.addAsm(swInst);
        }
    }
}
