package midend.pass;

import midend.IRBuilder;
import midend.ir.Module;
import midend.ir.*;
import midend.ir.inst.*;
import midend.ir.type.IntegerType;

import java.util.*;

public class Mem2Reg implements IRPass {
    private final Module module;
    /***
     * 每个函数的alloca列表。
     */
    private final HashMap<Function, ArrayList<AllocaInst>> funcAllocaInstsMap;
    /***
     * 每个alloca变量的再定义基本块列表。
     */
    private final HashMap<AllocaInst, ArrayList<BasicBlock>> allocaDefBlocksMap;
    /***
     * 每条phi指令对应的alloca变量。
     */
    private final HashMap<PhiInst, AllocaInst> phiAllocaMap;
    /***
     * 每个alloca变量的incomingValue栈。
     */
    private final HashMap<AllocaInst, Stack<Value>> allocaIncomingValueMap;
    /***
     * 变量重命名dfs标记已访问基本块。
     */
    private final HashSet<BasicBlock> visited;

    /***
     * 插入phi指令，变量重命名。
     */
    public Mem2Reg() {
        this.module = Module.getInstance();
        this.funcAllocaInstsMap = new HashMap<>();
        this.allocaDefBlocksMap = new HashMap<>();
        this.phiAllocaMap = new HashMap<>();
        this.allocaIncomingValueMap = new HashMap<>();
        this.visited = new HashSet<>();
    }

    @Override
    public void run() {
        for (Function function : module.getNotLibFunctions()) {
            findDef(function);
            insertPhi(function);
            rename(function, function.getBasicBlocks().get(0));
        }
    }

    private void findDef(Function function) {
        funcAllocaInstsMap.put(function, new ArrayList<>());
        for (BasicBlock basicBlock : function.getBasicBlocks()) {
            for (Inst inst : basicBlock.getInsts()) {
                if (inst instanceof AllocaInst allocaInst && allocaInst.getTargetType() == IntegerType.i32) {   // 只处理普通变量alloca，不处理数组alloca
                    // 初始化map
                    funcAllocaInstsMap.get(function).add(allocaInst);
                    allocaDefBlocksMap.put(allocaInst, new ArrayList<>());
                    Stack<Value> stack = new Stack<>();
                    stack.push(new Constant(IntegerType.i32, 0));   // 需要对alloc变量初始化为0，防止peek()时栈内为空的情况
                    allocaIncomingValueMap.put(allocaInst, stack);
                    // 填充map
                    for (User user : allocaInst.getUserList()) {
                        if (user instanceof StoreInst storeInst) {  // store是该alloca变量的再定义点
                            allocaDefBlocksMap.get(allocaInst).add(storeInst.getParentBasicBlock());
                        }
                    }
                }
            }
        }
    }

    private void insertPhi(Function function) {
        // 遍历每个alloca变量v
        for (AllocaInst v : funcAllocaInstsMap.get(function)) {
            HashSet<BasicBlock> F = new HashSet<>();    // 记录变量v已插入phi的基本块，防止重复插入phi
            ArrayList<BasicBlock> W = new ArrayList<>(allocaDefBlocksMap.get(v));  // 包含变量v的再定义的基本块列表
            while (!W.isEmpty()) {
                BasicBlock X = W.remove(W.size() - 1);
                for (BasicBlock Y : X.getDFList()) {
                    if (!F.contains(Y)) {
                        // 插入phi到Y开头
                        PhiInst phiInst = new PhiInst(IRBuilder.getInstance().genLocalVarNameForFunc(function), Y.getCFGPreList());
                        Y.addInstAtFirst(phiInst);
                        // 记录该phi的alloca变量v
                        phiAllocaMap.put(phiInst, v);
                        // 标记Y已添加phi
                        F.add(Y);
                        if (!allocaDefBlocksMap.get(v).contains(Y)) {
                            W.add(Y);
                        }
                    }
                }
            }
        }
    }

    private void rename(Function function, BasicBlock entry) {
        visited.add(entry);
        HashMap<AllocaInst, Integer> pushCnt = new HashMap<>();    // 统计alloca变量在该基本块incomingValueStack入栈次数，处理结束后按入栈次数出栈
        Iterator<Inst> iterator = entry.getInsts().iterator();
        while (iterator.hasNext()) {
            Inst inst = iterator.next();
            if (inst instanceof AllocaInst allocaInst && funcAllocaInstsMap.get(function).contains(allocaInst)) {
                iterator.remove(); // 删除指令
            } else if (inst instanceof LoadInst loadInst) {
                if (!(loadInst.getPointer() instanceof AllocaInst allocaInst)) {
                    continue;
                }
                if (funcAllocaInstsMap.get(function).contains(allocaInst)) {
                    loadInst.replaceOperandOfAllUser(allocaIncomingValueMap.get(allocaInst).peek()); // 把后续对load使用更新为对对应alloca变量的最新到达定义的使用
                    iterator.remove(); // 删除指令
                }
            } else if (inst instanceof StoreInst storeInst) {
                if (!(storeInst.getTo() instanceof AllocaInst allocaInst)) {
                    continue;
                }
                if (funcAllocaInstsMap.get(function).contains(allocaInst)) {
                    allocaIncomingValueMap.get(allocaInst).push(storeInst.getFrom()); // 更新到达定义
                    pushCnt.put(allocaInst, pushCnt.getOrDefault(allocaInst, 0) + 1);   // 入栈计数++
                    iterator.remove(); // 删除指令
                }
            } else if (inst instanceof PhiInst phiInst) {
                if (phiAllocaMap.containsKey(phiInst)) {
                    AllocaInst allocaInst = phiAllocaMap.get(phiInst);
                    allocaIncomingValueMap.get(allocaInst).push(phiInst); // 更新到达定义
                    pushCnt.put(allocaInst, pushCnt.getOrDefault(allocaInst, 0) + 1);   // 入栈计数++
                }
            }
            for (BasicBlock suc : entry.getCFGSucList()) {
                for (Inst inst1 : suc.getInsts()) {
                    if (!(inst1 instanceof PhiInst phiInst)) {
                        break;
                    }
                    if (phiAllocaMap.containsKey(phiInst)) {
                        AllocaInst allocaInst = phiAllocaMap.get(phiInst);
                        phiInst.addOption(allocaIncomingValueMap.get(allocaInst).peek(), entry);
                    }
                }
            }
        }
        // dfs
        for (BasicBlock suc : entry.getCFGSucList()) {
            if (!visited.contains(suc)) {
                rename(function, suc);
            }
        }
        // 出栈
        for (AllocaInst allocaInst : pushCnt.keySet()) {
            for (int i = 0; i < pushCnt.get(allocaInst); i++) {
                allocaIncomingValueMap.get(allocaInst).pop();
            }
        }
    }
}