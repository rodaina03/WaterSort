package code;

import java.util.List;

public interface Problem<S> {
    Node<S> getInitialNode();

    boolean isGoalState(Node<S> state);

    List<Node<S>> expandNode(Node<S> node, boolean compareByPathCostOnly);

    void printNodeState(Node<S> node, String Strategy);

    int calculateHeuristicOne(S state);

    int calculateHeuristicTwo(S state);

    String stateToString(S state);
}