package frontend.node;

public class ConstExpNode extends Node {
    private final AddExpNode addExpNode;

    public ConstExpNode(AddExpNode addExpNode) {
        this.addExpNode = addExpNode;
    }
}
