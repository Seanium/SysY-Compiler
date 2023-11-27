package midend.pass;

import midend.ir.BasicBlock;
import midend.ir.Function;
import midend.ir.Module;
import midend.ir.inst.BranchInst;
import midend.ir.inst.Inst;
import midend.ir.inst.JumpInst;

import java.util.ArrayList;
import java.util.HashSet;

public class DFBuild implements IRPass {
    private final Module module;

    public DFBuild() {
        this.module = Module.getInstance();
    }

    @Override
    public void run() {
        for (Function function : module.getNotLibFunctions()) {
            buildCFG(function);
            buildDOM(function);
            buildDF(function);
        }
    }

    /***
     * 构建CFG(控制流图)。
     */
    private void buildCFG(Function function) {
        for (BasicBlock basicBlock : function.getBasicBlocks()) {
            Inst lastInst = basicBlock.getLastInst();
            // 根据jump和branch添加前驱和后继基本块
            if (lastInst instanceof JumpInst jumpInst) {
                BasicBlock targetBasicBlock = jumpInst.getTargetBasicBlock();
                basicBlock.getCFGSucList().add(targetBasicBlock);
                targetBasicBlock.getCFGPreList().add(basicBlock);
            } else if (lastInst instanceof BranchInst branchInst) {
                BasicBlock trueBlock = branchInst.getTrueBlock();
                BasicBlock falseBlock = branchInst.getFalseBlock();
                basicBlock.getCFGSucList().add(trueBlock);
                basicBlock.getCFGSucList().add(falseBlock);
                trueBlock.getCFGPreList().add(basicBlock);
                falseBlock.getCFGPreList().add(basicBlock);
            }
        }
    }

    /***
     * 计算不经过dominator结点时，能访问到的所有结点。
     * @param entry 入口基本块。
     * @param dominator 待计算其dom列表的dominator，即dfs中要“删除”的结点。
     * @param visited CFG中，去掉dominator结点后，能访问到的节点集合。
     */
    private void visit(BasicBlock entry, BasicBlock dominator, HashSet<BasicBlock> visited) {
        if (entry.equals(dominator)) {
            return;
        }
        visited.add(entry);
        for (BasicBlock suc : entry.getCFGSucList()) {
            if (!visited.contains(suc)) {
                visit(suc, dominator, visited);
            }
        }
    }

    private void buildDOM(Function function) {
        ArrayList<BasicBlock> basicBlocks = function.getBasicBlocks();
        // 一、计算支配关系
        // 采用顶点删除法计算，这里不用真的删除，只需在dfs遍历到目标顶点时return
        BasicBlock entry = basicBlocks.get(0);
        for (BasicBlock dominator : basicBlocks) {
            HashSet<BasicBlock> visited = new HashSet<>();
            visit(entry, dominator, visited);
            for (BasicBlock b : basicBlocks) {
                // dominator支配结点b
                if (!visited.contains(b)) {
                    dominator.getDomList().add(b);
                    b.getDomByList().add(dominator);
                }
            }
        }
        // 二、计算严格支配关系，即从支配关系中排除自身到自身的映射
        for (BasicBlock basicBlock : basicBlocks) {
            basicBlock.getStrictDomList().addAll(basicBlock.getDomList());
            basicBlock.getStrictDomList().remove(basicBlock);
            basicBlock.getStrictDomByList().addAll(basicBlock.getDomByList());
            basicBlock.getStrictDomByList().remove(basicBlock);
        }
        // 三、计算直接支配关系
        // 若m严格支配n，且m不严格支配任何严格支配n的节点p，则m直接支配n
        for (BasicBlock m : basicBlocks) {
            for (BasicBlock n : m.getStrictDomList()) {
                boolean satisfied = true;   // 是否满足直接支配的条件
                for (BasicBlock p : n.getStrictDomByList()) {
                    if (m.getStrictDomList().contains(p)) {
                        satisfied = false;
                        break;
                    }
                }
                if (satisfied) {
                    m.getImmDomList().add(n);
                    n.setImmDomBy(m);
                }
            }
        }
    }

    /***
     * 构建每个结点的支配边界列表(dfList)。
     */
    private void buildDF(Function function) {
        ArrayList<BasicBlock> basicBlocks = function.getBasicBlocks();
        for (BasicBlock a : basicBlocks) {
            for (BasicBlock b : a.getCFGSucList()) {
                BasicBlock x = a;
                while (!x.getStrictDomList().contains(b)) {
                    x.getDFList().add(b);
                    x = x.getImmDomBy();
                }
            }
        }
    }
}
