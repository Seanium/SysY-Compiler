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
                // li $t1 eleOffset
                int eleOffset = 4 * constant.getValue(); // 下标小的元素在低地址
                LiInst liInst = new LiInst(Reg.t1, eleOffset);
                mipsBuilder.addAsm(liInst);
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
            }
            if (gepInst.notInReg()) {   // 若gep结果未分配寄存器
                // addu $t1 $baseReg $t1 目标元素地址存入t1
                AdduInst adduInst = new AdduInst(Reg.t1, baseReg, Reg.t1);
                mipsBuilder.addAsm(adduInst);
                // sw $t1 offset1($sp) 目标元素地址存入栈帧
                int offset1 = mipsBuilder.addValueToCurRecord(gepInst);
                SwInst swInst = new SwInst(Reg.t1, offset1, Reg.sp);
                mipsBuilder.addAsm(swInst);
            } else {    // 若gep结果分配了寄存器
                // addu $toReg $baseReg $t1
                Reg toReg = gepInst.getReg();
                AdduInst adduInst = new AdduInst(toReg, baseReg, Reg.t1);
                mipsBuilder.addAsm(adduInst);
            }
        } else if (gepInst.getBasePointer() instanceof GlobalArray globalArray) {   // 全局数组 下标小的元素在低地址
            Value gepOffset = gepInst.getOffset();  // 目标元素地址偏移量 gep偏移量
            Reg gepOffsetReg = Reg.t1;  // gep偏移量寄存器 默认为t1，若已分配寄存器再更改
            // 目标元素内存地址偏移量，存到t1
            if (gepOffset instanceof Constant constant) { // 若偏移量为常数
                // li $t1 eleOffset
                int eleOffset = 4 * constant.getValue();
                LiInst liInst = new LiInst(Reg.t1, eleOffset);
                mipsBuilder.addAsm(liInst);
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
            }
            if (gepInst.notInReg()) {   // 若gep结果即目标元素地址未分配寄存器
                // la $t0 arrayLabel($t1) 目标元素地址存到t0
                LaInst laInst = new LaInst(Reg.t0, globalArray.getName().substring(1), Reg.t1);
                mipsBuilder.addAsm(laInst);
                // sw $t0 offset($sp) 目标元素地址存到栈帧
                int offset = mipsBuilder.addValueToCurRecord(gepInst);
                SwInst swInst = new SwInst(Reg.t0, offset, Reg.sp);
                mipsBuilder.addAsm(swInst);
            } else {    // 若gep结果分配了寄存器
                // la $toReg arrayLabel($t1)
                Reg toReg = gepInst.getReg();
                LaInst laInst = new LaInst(toReg, globalArray.getName().substring(1), Reg.t1);
                mipsBuilder.addAsm(laInst);
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
            ArrayList<Inst> insts = callInst.getParentBasicBlock().getInsts();
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
        if (cond.notInReg()) {  // cond在内存中
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
        Value operand1 = icmpInst.getOperand1();
        Value operand2 = icmpInst.getOperand2();
        Reg op1Reg = Reg.t0;    // 操作数1的寄存器，默认为t0，若已分配则需修改
        Reg op2Reg = Reg.t1;    // 操作数2的寄存器，默认为t1，若已分配则需修改
        Reg toReg = Reg.t0;     // 保存结果的寄存器
        // 获取操作数1
        if (operand1 instanceof Constant constant) {    // 常数直接li
            // li $t0 constant
            LiInst liInst = new LiInst(Reg.t0, constant.getValue());
            mipsBuilder.addAsm(liInst);
        } else {
            if (operand1.notInReg()) {  // 不在寄存器中，从内存加载
                // lw $t0 offset($sp)
//                if (mipsBuilder.isValueNotInCurRecord(operand1)) {
//                    System.out.println(icmpInst);
//                }
                int offset = mipsBuilder.getOffsetOfValue(operand1);
                LwInst lwInst = new LwInst(Reg.t0, offset, Reg.sp);
                mipsBuilder.addAsm(lwInst);
            } else {    // 在寄存器中，修改其所在寄存器
                op1Reg = operand1.getReg();
            }
        }
        // 获取操作数2
        if (operand2 instanceof Constant constant) {    // 常数直接li
            // li $t1 constant
            LiInst liInst = new LiInst(Reg.t1, constant.getValue());
            mipsBuilder.addAsm(liInst);
        } else {
            if (operand2.notInReg()) {  // 不在寄存器中，从内存加载
                // lw $t1 offset($sp)
                int offset = mipsBuilder.getOffsetOfValue(operand2);
                LwInst lwInst = new LwInst(Reg.t1, offset, Reg.sp);
                mipsBuilder.addAsm(lwInst);
            } else {    // 在寄存器中，修改其所在寄存器
                op2Reg = operand2.getReg();
            }
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
        if (icmpInst.inReg()) { // 若结果分配了寄存器，存到寄存器
            toReg = icmpInst.getReg();
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

    private void visitBinaryInst(BinaryInst binaryInst) {
        Value operand1 = binaryInst.getOperand1();
        Value operand2 = binaryInst.getOperand2();

        Reg op1Reg = Reg.t0;   // 默认为t0，若已分配寄存器再修改
        Reg op2Reg = Reg.t1;   // 默认为t1，若已分配寄存器再修改
        Reg toReg = binaryInst.inReg() ? binaryInst.getReg() : Reg.t0;

        boolean adduToAddiu = false;  // 标记addu能优化为addiu
        // 先对含常数的加法先进行特判，优化为addiu
        Constant addiuConstant = null;
        if (binaryInst.getOpcode() == midend.ir.inst.Opcode.add) {
            if (operand1 instanceof Constant constant && !(operand2 instanceof Constant)) {
                adduToAddiu = true; // 能够优化为addiu
                addiuConstant = constant;
                if (operand2.notInReg()) {
                    // lw $t0 offset($sp)
                    int offset = mipsBuilder.getOffsetOfValue(operand2);
                    LwInst lwInst = new LwInst(Reg.t0, offset, Reg.sp);
                    mipsBuilder.addAsm(lwInst);
                } else {
                    op1Reg = operand2.getReg(); // 前面的op1Reg指的是addiu的第一个操作数寄存器
                }
            } else if (!(operand1 instanceof Constant) && operand2 instanceof Constant constant) {
                adduToAddiu = true; // 能够优化为addiu
                addiuConstant = constant;
                if (operand1.notInReg()) {
                    // lw $t0 offset($sp)
                    int offset = mipsBuilder.getOffsetOfValue(operand1);
                    LwInst lwInst = new LwInst(Reg.t0, offset, Reg.sp);
                    mipsBuilder.addAsm(lwInst);
                } else {
                    op1Reg = operand1.getReg();
                }
            } else if (operand1 instanceof Constant constant1 && operand2 instanceof Constant constant2) {  // 如果两个都是常数，让小的作为addiu的立即数
                adduToAddiu = true; // 能够优化为addiu
                addiuConstant = constant1.getValue() < constant2.getValue() ? constant1 : constant2;
                Constant liConstant = constant1.getValue() < constant2.getValue() ? constant2 : constant1;
                // li $op1Reg liConstant
                LiInst liInst = new LiInst(op1Reg, liConstant.getValue());
                mipsBuilder.addAsm(liInst);
            }
        }
        if (!adduToAddiu) {
            // 获取操作数1
            if (operand1 instanceof Constant constant) {
                // li $t0 constant
                LiInst liInst = new LiInst(Reg.t0, constant.getValue());
                mipsBuilder.addAsm(liInst);
            } else {
                if (operand1.notInReg()) {   // 未分配寄存器，从内存中取
                    // lw $t0 offset($sp)
                    int offset = mipsBuilder.getOffsetOfValue(operand1);
                    LwInst lwInst = new LwInst(Reg.t0, offset, Reg.sp);
                    mipsBuilder.addAsm(lwInst);
                } else {    // 已分配寄存器，从寄存器中取
                    op1Reg = operand1.getReg();
                }
            }
            // 获取操作数2
            if (operand2 instanceof Constant constant) {
                // li $t1 constant
                LiInst liInst = new LiInst(Reg.t1, constant.getValue());
                mipsBuilder.addAsm(liInst);
            } else {
                if (operand2.notInReg()) {   // 未分配寄存器，从内存中取
                    // lw $t1 offset($sp)
                    int offset = mipsBuilder.getOffsetOfValue(operand2);
                    LwInst lwInst = new LwInst(Reg.t1, offset, Reg.sp);
                    mipsBuilder.addAsm(lwInst);
                } else {    // 已分配寄存器，从寄存器中取
                    op2Reg = operand2.getReg();
                }
            }
        }
        switch (binaryInst.getOpcode()) {
            case add -> {
                if (!adduToAddiu) { // 如果addu不能优化为addiu
                    // addu $toReg $op1Reg $op2Reg
                    AdduInst adduInst = new AdduInst(toReg, op1Reg, op2Reg);
                    mipsBuilder.addAsm(adduInst);
                } else {    // 如果addu能优化为addiu
                    // addiu $toReg $op1Reg addiuConstant
                    AddiuInst addiuInst = new AddiuInst(toReg, op1Reg, addiuConstant.getValue());
                    mipsBuilder.addAsm(addiuInst);
                }
            }
            case sub -> {
                // subu $toReg $op1Reg $op2Reg
                SubuInst subuInst = new SubuInst(toReg, op1Reg, op2Reg);
                mipsBuilder.addAsm(subuInst);
            }
            case mul -> {
                // mult $op1Reg $op2Reg
                MultInst multInst = new MultInst(op1Reg, op2Reg);
                mipsBuilder.addAsm(multInst);
                // mflo $toReg
                MfHiloInst mfloInst = new MfHiloInst(Opcode.mflo, toReg);
                mipsBuilder.addAsm(mfloInst);
            }
            case sdiv -> {
                // div $op1Reg $op2Reg
                DivInst divInst = new DivInst(op1Reg, op2Reg);
                mipsBuilder.addAsm(divInst);
                // mflo $toReg
                MfHiloInst mfloInst = new MfHiloInst(Opcode.mflo, toReg);
                mipsBuilder.addAsm(mfloInst);
            }
            case srem -> {
                // div $op1Reg $op2Reg
                DivInst divInst = new DivInst(op1Reg, op2Reg);
                mipsBuilder.addAsm(divInst);
                // mfhi $toReg
                MfHiloInst mfhiInst = new MfHiloInst(Opcode.mfhi, toReg);
                mipsBuilder.addAsm(mfhiInst);
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
