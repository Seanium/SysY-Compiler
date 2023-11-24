package midend.ir;

import midend.ir.inst.Inst;
import midend.ir.inst.MoveInst;
import midend.ir.type.OtherType;

import java.util.ArrayList;

public class BasicBlock extends Value {
    private final Function parentFunction;
    private final ArrayList<Inst> instructions;
    /***
     * CFG中，该基本块的前驱基本块列表。
     */
    private final ArrayList<BasicBlock> cfgPreList;
    /***
     * CFG中，该基本块的后继基本块列表。
     */
    private final ArrayList<BasicBlock> cfgSucList;
    /***
     * 该基本块支配的基本块列表。即该基本块的支配下级。
     */
    private final ArrayList<BasicBlock> domList;
    /***
     * 支配该基本块的基本块列表。即该基本块的支配上级。
     */
    private final ArrayList<BasicBlock> domByList;
    /***
     * 该基本块严格支配的基本块列表。即该基本块的严格支配下级。
     */
    private final ArrayList<BasicBlock> strictDomList;
    /***
     * 严格支配该基本块的基本块列表。即该基本块的严格支配上级。
     */
    private final ArrayList<BasicBlock> strictDomByList;
    /***
     * 该基本块直接支配的基本块。即该基本块的直接支配下级。
     */
    private final ArrayList<BasicBlock> immDomList;
    /***
     * 直接支配该基本块的基本块。即该基本块的直接支配上级。
     */
    private BasicBlock immDomBy;
    /***
     * 该基本块的严格支配边界。
     */
    private final ArrayList<BasicBlock> dfList;
    /***
     * 基本块开头的move指令集合。
     */
    private final ArrayList<MoveInst> beginMoves;
    /***
     * 基本块末尾的move指令集合。
     */
    private final ArrayList<MoveInst> endMoves;

    public BasicBlock(String name, Function parentFunction) {
        super(OtherType.basicBlock, name);  // 基本块的 name 就是其 label
        this.parentFunction = parentFunction;
        this.instructions = new ArrayList<>();
        this.cfgPreList = new ArrayList<>();
        this.cfgSucList = new ArrayList<>();
        this.domList = new ArrayList<>();
        this.domByList = new ArrayList<>();
        this.strictDomList = new ArrayList<>();
        this.strictDomByList = new ArrayList<>();
        this.immDomList = new ArrayList<>();
        this.immDomBy = null;
        this.dfList = new ArrayList<>();
        this.beginMoves = new ArrayList<>();
        this.endMoves = new ArrayList<>();
    }

    /***
     * 插入指令到指令列表尾部。
     */
    public void addInstAtLast(Inst inst) {
        instructions.add(inst);
        inst.setParentBasicBlock(this);
    }

    /***
     * 插入指令到指令列表头部。
     */
    public void addInstAtFirst(Inst inst) {
        instructions.add(0, inst);
        inst.setParentBasicBlock(this);
    }

    public void addInsts(int index, ArrayList<? extends Inst> insts) {
        instructions.addAll(index, insts);
        for (Inst inst : insts) {
            inst.setParentBasicBlock(this);
        }
    }

    /***
     * 禁止通过此方法向基本块中插入指令。(防止忘记设置指令的父基本块)
     * 如需插入，请调用IRBuilder中相关方法，或addInstAtFisrt()、addInsts()方法。
     */
    public ArrayList<Inst> getInstructions() {
        return instructions;
    }

    /***
     * 返回基本块的最后一条指令。若基本块内无指令，返回null。
     */
    public Inst getLastInst() {
        if (instructions.isEmpty()) {
            return null;
        }
        return instructions.get(instructions.size() - 1);
    }

    public ArrayList<BasicBlock> getCFGPreList() {
        return cfgPreList;
    }

    public ArrayList<BasicBlock> getCFGSucList() {
        return cfgSucList;
    }

    public ArrayList<BasicBlock> getStrictDomByList() {
        return strictDomByList;
    }

    public ArrayList<BasicBlock> getStrictDomList() {
        return strictDomList;
    }

    public BasicBlock getImmDomBy() {
        return immDomBy;
    }

    public ArrayList<BasicBlock> getDFList() {
        return dfList;
    }

    public ArrayList<BasicBlock> getImmDomList() {
        return immDomList;
    }

    public void setImmDomBy(BasicBlock immDomBy) {
        this.immDomBy = immDomBy;
    }

    public ArrayList<BasicBlock> getDomList() {
        return domList;
    }

    public ArrayList<BasicBlock> getDomByList() {
        return domByList;
    }

    public ArrayList<MoveInst> getBeginMoves() {
        return beginMoves;
    }

    public ArrayList<MoveInst> getEndMoves() {
        return endMoves;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append(":\n");
        for (Inst inst : instructions) {
            sb.append("    ").append(inst.toString()).append("\n");
        }
        return sb.toString();
    }
}