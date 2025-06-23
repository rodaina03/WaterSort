package code;

import java.util.*;

public class Node<S> implements Comparable<Node<S>> {
    private S state;
    private Node<S> parent;
    private String action; 
    private int pathCost;  
    private int depth;
    private int hN;
    private boolean compareByPathCostOnly = false;

    public Node(S state, Node<S> parent, String action, int pathCost,int depth) {
        this.state = state;
        this.parent = parent;
        this.action = action;
        this.pathCost = pathCost;
        this.depth = depth;
    }

    public S getState() {
        return state;
    }

    public Node<S> getParent() {
        return parent;
    }

    public String getAction() {
        return action;
    }

    public int getPathCost() {
        return pathCost;
    }

    public int getDepth() {
		return depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}

	public int getHeuristicValue() {
        return hN;
    }

    public void setHeuristicValue(int hN) {
        this.hN = hN;
    }

    public int getEvaluationFunction() {
        return pathCost + hN;
    }

    public void setCompareByPathCostOnly(boolean compareByPathCostOnly) {
        this.compareByPathCostOnly = compareByPathCostOnly;
    }

    @Override
    public int compareTo(Node<S> other) {
        if (compareByPathCostOnly)
            return Integer.compare(this.pathCost, other.pathCost);
        else 
            return Integer.compare(this.getEvaluationFunction(), other.getEvaluationFunction());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Node<?>)) return false;

        Node<?> other = (Node<?>) obj;
        return Objects.equals(this.state, other.state);
    }

    @Override
    public int hashCode() {
        return Objects.hash(state);
    }
}



