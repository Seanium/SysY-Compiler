package midend.pass;

import midend.IRBuilder;
import midend.ir.Module;
import midend.ir.*;
import midend.ir.inst.*;
import midend.ir.type.IntegerType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Stack;

public class Mem2RegPass implements Pass {
    private final Module module;
    /***
     * 某个alloca变量的store/phi指令列表。
     */
    private final ArrayList<Inst> defInstList;
    /***
     * 某个alloca变量的load/phi指令集合。
     */
    private final ArrayList<Inst> useInstList;
    /***
     * 用于变量重命名的value栈。
     */
    private final Stack<Value> incomingValues;
    /***
     * 记录当前的allocaInst。
     */
    private AllocaInst curAllocaInst;
    /***
     * 记录当前的function。
     */
    private Function curFunc;

    public Mem2RegPass() {
        this.module = Module.getInstance();
        this.defInstList = new ArrayList<>();
        this.useInstList = new ArrayList<>();
        this.incomingValues = new Stack<>();
        this.curAllocaInst = null;
        curFunc = null;
    }

    @Override
    public void run() {
        for (Function function : module.getNotLibFunctions()) {
            this.curFunc = function;
            for (BasicBlock basicBlock : function.getBasicBlocks()) {
                ArrayList<Inst> insts = new ArrayList<>(basicBlock.getInstructions());
                for (Inst inst : insts) {
                    if (inst instanceof AllocaInst allocaInst && allocaInst.getTargetType() == IntegerType.i32) {   // allocaInst是某个变量的第一次定义点
                        curAllocaInst = allocaInst;
                        // 找出所有包含该变量定义的基本块
                        ArrayList<BasicBlock> defBasicBlocks = getDefBasicBlocks(allocaInst);
                        // 插入phi指令
                        insertPhi(defBasicBlocks);
                        // 变量重命名
                        incomingValues.clear();
                        rename(function.getBasicBlocks().get(0));
                    }
                }
            }
        }
    }

    /***
     * 找到某变量的所有定义点的所属基本块。
     */
    private ArrayList<BasicBlock> getDefBasicBlocks(AllocaInst allocaInst) {
        ArrayList<BasicBlock> defBasicBlocks = new ArrayList<>();
        defInstList.clear();
        useInstList.clear();
        for (User user : allocaInst.getUserList()) {
            if (user instanceof StoreInst storeInst) {  // store指令是某个变量的重定义点
                defBasicBlocks.add(storeInst.getParentBasicBlock());
                defInstList.add(storeInst);
            } else if (user instanceof LoadInst loadInst) {
                useInstList.add(loadInst);
            }
        }
        return defBasicBlocks;
    }

    private void insertPhi(ArrayList<BasicBlock> defBasicBlocks) {
        HashSet<BasicBlock> F = new HashSet<>();    // 已添加phi的基本块
        ArrayList<BasicBlock> W = new ArrayList<>(defBasicBlocks);    // 包含变量v的定义的基本块
        while (!W.isEmpty()) {
            BasicBlock X = W.remove(W.size() - 1);
            for (BasicBlock Y : X.getDFList()) {
                if (!F.contains(Y)) {
                    // 插入phi到Y开头
                    PhiInst phiInst = new PhiInst(IRBuilder.getInstance().genLocalVarNameForFunc(curFunc), Y.getCFGPreList());
                    Y.addInstAtFirst(phiInst);
                    useInstList.add(phiInst);
                    defInstList.add(phiInst);
                    // 标记Y已添加phi
                    F.add(Y);
                    if (!defBasicBlocks.contains(Y)) {
                        W.add(Y);
                    }
                }
            }
        }
    }

    private void rename(BasicBlock entry) {
        int cnt = 0;
        Iterator<Inst> iterator = entry.getInstructions().iterator();
        while (iterator.hasNext()) {
            Inst inst = iterator.next();
            if (inst instanceof LoadInst && useInstList.contains(inst)) {
                Value newOperand = incomingValues.empty() ? new Constant(IntegerType.i32, 0) : incomingValues.peek();
                inst.replaceUserOperandWith(newOperand);
                iterator.remove();
            } else if (inst instanceof StoreInst storeInst && defInstList.contains(inst)) {
                Value from = storeInst.getFrom();
                incomingValues.push(from);  // 更新到达定义
                cnt++;
                iterator.remove();
            } else if (inst instanceof PhiInst phiInst && defInstList.contains(inst)) {
                incomingValues.push(phiInst);   // 更新到达定义
                cnt++;
            } else if (inst.equals(curAllocaInst)) {
                iterator.remove();
            }
        }
        // dfs
        for (BasicBlock suc : entry.getCFGSucList()) {
            Inst firstInst = suc.getInstructions().get(0);
            if (firstInst instanceof PhiInst phiInst && useInstList.contains(phiInst)) {
                phiInst.addOption(incomingValues.empty() ? new Constant(IntegerType.i32, 0) : incomingValues.peek(), entry);
            }
        }
        for (BasicBlock child : entry.getImmDomList()) {
            rename(child);
        }
        // 将本轮dfs入栈元素清除
        for (int i = 0; i < cnt; i++) {
            incomingValues.pop();
        }
    }
}
