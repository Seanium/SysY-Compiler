package midend.pass;

import midend.ir.BasicBlock;
import midend.ir.Function;
import midend.ir.Module;
import midend.ir.Value;
import midend.ir.inst.Inst;
import midend.ir.inst.MoveInst;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;

public class LivenessAnalyze implements IRPass {
    private final Module module;

    public LivenessAnalyze() {
        this.module = Module.getInstance();
    }

    @Override
    public void run() {
        for (Function function : module.getNotLibFunctions()) {
            iterativeLivenessAnalyze(function);
            setInstId(function);
            buildLiveRange(function);
        }
    }

    /***
     * 反向遍历ir所有指令，生成每条指令的liveIn和liveOut。
     * 特别地。如果循环体头部遍历完，且其liveIn与循环体尾部liveOut所含元素不相等，需要将其liveIn加到该循环体尾部的liveOut，并跳回循环体尾部。每个循环头只返回尾部一次。
     */
    private void iterativeLivenessAnalyze(Function function) {
        HashSet<BasicBlock> loopHeadDone = new HashSet<>(); // 标记已处理完的循环头
        ArrayList<BasicBlock> basicBlocks = function.getBasicBlocks();
        for (int i = basicBlocks.size() - 1; i >= 0; i--) {
            BasicBlock basicBlock = basicBlocks.get(i);
//            System.out.println(basicBlock.getName()); //todo 调试信息
            ArrayList<Inst> insts = basicBlock.getInstructions();
            for (int j = insts.size() - 1; j >= 0; j--) {
                Inst inst = insts.get(j);
                // 定义每条指令的liveDef
                if (!(inst instanceof MoveInst)) { // move没有产生def
                    inst.getLiveDef().add(inst);
                }
                // 定义每条指令的liveUse
                inst.getLiveUse().addAll(inst.getVarOperandList()); // 对于move而言，添加的是非constant的from以及to
                // 函数的变量列表
                function.getVars().addAll(inst.getVarOperandList());
                if (j != insts.size() - 1) {    // 如果不是基本块最后一条指令
                    Inst sucInst = insts.get(j + 1);
                    inst.getLiveOut().addAll(sucInst.getLiveIn());  // 当前指令的liveOut是后继指令的liveOut 块内指令的后继是唯一的
                } else if (i != basicBlocks.size() - 1) {   // 如果是基本块最后一条指令，且不是末尾基本块
                    Inst sucInst = basicBlocks.get(i + 1).getInstructions().get(0);
                    inst.getLiveOut().addAll(sucInst.getLiveIn());
                }
                // in[S] = gen[S] ∪ (out[S] - kill[S]) = use[S] ∪ (out[S] - def[S])
                HashSet<Value> temp = new LinkedHashSet<>(inst.getLiveOut());  // 避免就地修改
                temp.removeAll(inst.getLiveDef());
                temp.addAll(inst.getLiveUse());
                inst.getLiveIn().addAll(temp);
                if (j == 0) {   // 如果是基本块第一条指令
                    for (BasicBlock pre : basicBlock.getCFGPreList()) {
                        int preIndex = basicBlocks.indexOf(pre);
                        if (preIndex > i) {     // 如果是pre循环尾
                            if (loopHeadDone.contains(basicBlock)) {    // 若该循环头已处理完，不重复处理
                                break;
                            }
                            pre.getLastInst().getLiveOut().addAll(inst.getLiveIn());    // 把循环体头部的活跃信息加入到循环体尾部

//                            //todo 调试信息
//                            StringBuilder liveIn = new StringBuilder();
//                            liveIn.append("[liveIn] ");
//                            for (Value value : inst.getLiveIn()) {
//                                liveIn.append(value.getName()).append("\t");
//                            }
//                            System.out.println(liveIn);
//
//                            StringBuilder liveOut = new StringBuilder();
//                            liveOut.append("[liveOut] ");
//                            for (Value value : pre.getLastInst().getLiveOut()) {
//                                liveOut.append(value.getName()).append("\t");
//                            }
//                            System.out.println(liveOut);

                            i = preIndex + 1;   // 返回到循环体尾部
                            loopHeadDone.add(basicBlock);   // 标记该循环头已处理
                            break;
                        }
                    }
                }
            }
        }
    }

    private void setInstId(Function function) {
        int id = 0;
        for (BasicBlock basicBlock : function.getBasicBlocks()) {
            for (Inst inst : basicBlock.getInstructions()) {
                inst.setId(id);
                id++;
            }
        }
    }

    private void buildLiveRange(Function function) {
        ArrayList<BasicBlock> basicBlocks = function.getBasicBlocks();
        for (BasicBlock basicBlock : basicBlocks) {
            ArrayList<Inst> insts = basicBlock.getInstructions();
            for (Inst inst : insts) {
                for (Value value : inst.getLiveIn()) {
                    value.getLiveRange().add(inst);
                }
            }
        }
    }
}
