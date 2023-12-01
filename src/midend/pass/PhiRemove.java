package midend.pass;

import midend.IRBuilder;
import midend.ir.BasicBlock;
import midend.ir.Function;
import midend.ir.Module;
import midend.ir.Value;
import midend.ir.inst.Inst;
import midend.ir.inst.MoveInst;
import midend.ir.inst.PhiInst;
import midend.ir.type.IntegerType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;

public class PhiRemove implements IRPass {
    private final Module module;

    /**
     * 移除phi指令。包括将phi指令转为并行的move，以及将并行的move串行化。
     */
    public PhiRemove() {
        this.module = Module.getInstance();
    }

    @Override
    public void run() {
        for (Function function : module.getFunctions()) {
            phi2ParallelCopy(function);
            parallelCopy2Sequential(function);
        }
    }

    /**
     * 将phi指令转换为并行的复制指令。
     * 采用SSA-Book中的算法21.1 (isolating phi-nodes)。
     */
    private void phi2ParallelCopy(Function function) {
        ArrayList<BasicBlock> basicBlocks = function.getBasicBlocks();
        for (BasicBlock B0 : basicBlocks) {
            Iterator<Inst> iterator = B0.getInsts().iterator();
            while (iterator.hasNext()) {
                Inst inst = iterator.next();
                if (!(inst instanceof PhiInst a0phi)) {
                    continue;
                }
                /*
                 * 消phi前：
                 * B0: a0 = phi(B1: a1, ..., Bn: an)
                 *
                 * 消phi后：
                 * B1: a0Temp = a1 (moveInsti位于B1.endMoves)
                 * B0: a0 = a0Temp (moveInst0位于B0.beginMoves)
                 */
                String a0TempName = IRBuilder.getInstance().genLocalVarNameForFunc(function);
                Value a0Temp = new Value(a0phi.getType(), a0TempName);
                for (int i = 0; i < a0phi.getOperandList().size(); i++) {
                    Value ai = a0phi.getOperandList().get(i);
                    BasicBlock Bi = a0phi.getCfgPreList().get(i);   // 只能用下标获取operand对应的preBlock，因为二者之间是多对一映射
                    MoveInst moveInsti = new MoveInst(a0Temp, ai);
                    Bi.getEndMoves().add(moveInsti);    // 向前驱基本块添加a0Temp = ai
                }
                MoveInst moveInst0 = new MoveInst(a0phi, a0Temp);
                B0.getBeginMoves().add(moveInst0);  // 向后继基本块添加a0 = a0Temp
                iterator.remove();  // 删除phi指令
                a0phi.replaceOperandOfAllUser(moveInst0.getTo());    // 把之后对a0phi的使用改为对moveInst0.to的使用
            }
        }
//        for (BasicBlock basicBlock : basicBlocks) {
//            // beginMoves插入到基本块开头
//            basicBlock.addInsts(0, basicBlock.getBeginMoves());
//            // endMoves插入到基本块最后一条指令之前
//            basicBlock.addInsts(basicBlock.getInstructions().size() - 1, basicBlock.getEndMoves());
//        }
    }

    private void parallelCopy2Sequential(Function function) {
        for (BasicBlock basicBlock : function.getBasicBlocks()) {
            parallelCopy2Sequential(function, basicBlock.getBeginMoves());
            // beginMoves插入到基本块开头
            basicBlock.addInsts(0, basicBlock.getBeginMoves());
            parallelCopy2Sequential(function, basicBlock.getEndMoves());
            // endMoves插入到基本块最后一条指令之前
            assert !basicBlock.getInsts().isEmpty() : "错误，基本块内无指令!";
            basicBlock.addInsts(basicBlock.getInsts().size() - 1, basicBlock.getEndMoves());
        }
    }

    /**
     * 将并行的复制指令串行化。
     * 对moves进行就地修改。
     */
    private void parallelCopy2Sequential(Function function, ArrayList<MoveInst> moves) {
        ArrayList<MoveInst> movesSeq = new ArrayList<>();
        Stack<Value> ready = new Stack<>();
        Stack<Value> to_do = new Stack<>();
        HashMap<Value, Value> loc = new HashMap<>();
        HashMap<Value, Value> pred = new HashMap<>();
        Value n = new Value(IntegerType.i32, IRBuilder.getInstance().genLocalVarNameForFunc(function));
        pred.put(n, null);
        for (MoveInst moveInst : moves) {
            Value a = moveInst.getFrom();
            Value b = moveInst.getTo();
            loc.put(b, null);
            pred.put(a, null);
        }
        for (MoveInst moveInst : moves) {
            Value a = moveInst.getFrom();
            Value b = moveInst.getTo();
            loc.put(a, a);
            pred.put(b, a);
            to_do.push(b);
        }
        for (MoveInst moveInst : moves) {
            Value b = moveInst.getTo();
            if (loc.get(b) == null) {
                ready.push(b);
            }
        }
        while (!to_do.isEmpty()) {
            while (!ready.isEmpty()) {
                Value b = ready.pop();
                Value a = pred.get(b);
                Value c = loc.get(a);
                movesSeq.add(new MoveInst(b, c));
                loc.put(a, b);
                if (a.equals(c) && pred.get(a) != null) {
                    ready.push(a);
                }
            }
            Value b = to_do.pop();
            if (!b.equals(loc.get(pred.get(b)))) {
                movesSeq.add(new MoveInst(n, b));
                loc.put(b, n);
                ready.push(b);
            }
        }
        // 就地修改moves
        moves.clear();
        moves.addAll(movesSeq);
    }
}
