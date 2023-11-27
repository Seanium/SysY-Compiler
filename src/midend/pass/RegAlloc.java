package midend.pass;

import backend.mips.Reg;
import midend.ir.BasicBlock;
import midend.ir.Function;
import midend.ir.Module;
import midend.ir.Value;
import midend.ir.inst.Inst;
import utils.Config;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class RegAlloc implements Pass {
    private final Module module;
    /***
     * 当前reg中存放的value。
     */
    private final LinkedHashMap<Reg, Value> regVarHashMap;

    public RegAlloc() {
        this.module = Module.getInstance();
        this.regVarHashMap = new LinkedHashMap<>();
    }


    @Override
    public void run() {
        initRegPool();
        for (Function function : module.getNotLibFunctions()) {
            regAlloc(function);
        }
        if (Config.getMode() == Config.Mode.DEBUG) {
            for (Function function : module.getNotLibFunctions()) {
                printLivenessRegInfo(function);
            }
        }
    }

    /***
     * 初始化可用寄存器。
     */
    private void initRegPool() {
        Reg[] regs = Reg.values();
        // 可用寄存器范围不能包括t0和t1
        for (int i = Reg.t2.ordinal(); i <= Reg.t9.ordinal(); i++) {
            regVarHashMap.put(regs[i], null);
        }
    }

    /***
     * 遍历函数每条语句，调用regDeAlloc和regAlloc。
     */
    private void regAlloc(Function function) {
        ArrayList<BasicBlock> basicBlocks = function.getBasicBlocks();
        for (BasicBlock basicBlock : basicBlocks) {
            for (Inst inst : basicBlock.getInstructions()) {
                regDeAlloc(inst);
                regAlloc(inst);
            }
        }
    }

    /***
     * 为失活的变量取消分配寄存器。
     */
    private void regDeAlloc(Inst inst) {
        for (Reg reg : regVarHashMap.keySet()) {
            Value var = regVarHashMap.get(reg);
            if (!inst.getLiveIn().contains(var)) {
                regVarHashMap.put(reg, null);
            }
        }
    }

    /***
     * 为未决策的活跃变量分配寄存器。
     * 只决策一次分配或者不分配。
     */
    private void regAlloc(Inst inst) {
        for (Value var : inst.getLiveIn()) {
            if (var.getReg() != null) {   // 已处理的变量，不重复处理
                continue;
            }
            for (Reg reg : regVarHashMap.keySet()) {
                if (regVarHashMap.get(reg) == null) {   // 若有空余寄存器reg，则分配给var
                    regVarHashMap.put(reg, var);
                    var.setReg(reg);
                    break;
                }
            }
            // 若无空余寄存器，则该变量永远不分配寄存器
            if (var.getReg() == null) {
                var.setReg(Reg.NOREG);
            }
        }
        // 设置每条指令的活跃寄存器列表
        for (Reg reg : regVarHashMap.keySet()) {
            if (regVarHashMap.get(reg) != null) {
                inst.getActiveRegs().add(reg);
            }
        }
    }

    /***
     * 打印每条指令的liveness信息和每条指令的活跃寄存器、每个变量的寄存器分配信息。
     */
    private void printLivenessRegInfo(Function function) {
        System.out.println(function.getName());
        for (BasicBlock basicBlock : function.getBasicBlocks()) {
            System.out.println(basicBlock.getName());
            for (Inst inst : basicBlock.getInstructions()) {
                // activeReg
                StringBuilder activeRegInfo = new StringBuilder();
                activeRegInfo.append("[activeReg] ");
                for (Reg reg : inst.getActiveRegs()) {
                    activeRegInfo.append(reg).append(" ");
                }
                System.out.println(activeRegInfo);
                // regAlloc
                StringBuilder regAllocInfo = new StringBuilder();
                regAllocInfo.append("[regAlloc] ");
                if (inst.getReg() != null) {
                    regAllocInfo.append(inst.getName()).append(":").append(inst.getReg()).append(" ");
                }
                for (Value op : inst.getVarOperandList()) {
                    if (op.getReg() != null) {
                        regAllocInfo.append(op.getName()).append(":").append(op.getReg()).append(" ");
                    }
                }
                System.out.println(regAllocInfo);
                // liveIn
                StringBuilder liveInInfo = new StringBuilder();
                liveInInfo.append("[liveIn] ");
                for (Value value : inst.getLiveIn()) {
                    liveInInfo.append(value.getName()).append("\t");
                }
                System.out.println(liveInInfo);
                // inst
                System.out.println("[inst " + inst.getId() + "] " + inst);
                // liveOut
                StringBuilder liveOutInfo = new StringBuilder();
                liveOutInfo.append("[liveOut] ");
                for (Value value : inst.getLiveOut()) {
                    liveOutInfo.append(value.getName()).append("\t");
                }
                System.out.println(liveOutInfo);
                System.out.println();
            }
        }
        // liveRange
        StringBuilder liveRangeInfo = new StringBuilder();
        liveRangeInfo.append(function.getName()).append(" [liveRange of vars]\n");
        for (Value var : function.getVars()) {
            liveRangeInfo.append(var.getName()).append(": ");
            for (Inst inst : var.getLiveRange()) {
                liveRangeInfo.append(inst.getId()).append(" ");
            }
            liveRangeInfo.append("\n");
        }
        System.out.println(liveRangeInfo);
        // regAlloc
        StringBuilder regAllocInfo = new StringBuilder();
        regAllocInfo.append(function.getName()).append(" [regAlloc of vars]\n");
        for (Value var : function.getVars()) {
            regAllocInfo.append(var.getName()).append(": ").append(var.getReg()).append("\n");
        }
        System.out.println(regAllocInfo);
    }
}
