package code;

import java.util.*;

public abstract class GenericSearch<S> {
    public abstract Node<S> getInitialNode();

    public abstract boolean isGoalState(Node<S> node);

    public abstract List<Node<S>> expandNode(Node<S> node, boolean compareByPathCostOnly, Set<String> explored);

    protected abstract String stateToString(S state);

    protected abstract void printNodeState(Node<S> node, String Strategy);

    protected abstract int calculateHeuristicOne(S state);

    protected abstract int calculateHeuristicTwo(S state);

    static int loops = 0;
    static int nodeCounter = 0;
    private boolean useHeuristic = false;
    private boolean useHeuristicOne = false;
    static int skippedNodes = 0;

    public Node<S> generalSearch(String strategy, boolean visualization) {
        /*
         * strategy: the search strategy used
         * visualization: flag that indicates whether additional information about
         * the search process should be printed to the console.
         */
        Queue<Node<S>> frontier;

        // this switch-case is used to determine which QING-FUN will be used
        switch (strategy) {
            case "ID": // ID: Iterative Depth that uses a queue
                return iterativeDepthSearch(visualization);
            case "UC": // Uniform Cost uses a priority queue
                // comparator for Node<S> objects that compares them based on their path cost.
                Comparator<Node<S>> UCnodeComparator = Comparator.comparingInt(Node<S>::getPathCost);
                // set the frontier to be a PriorityQueue that will be ordered based on the
                // nodeComparator prev implemented.
                frontier = new PriorityQueue<>(UCnodeComparator);
                break;
            case "AS1": // A* using Heuristic 1
                useHeuristic = true; // flag enables the use of heuristics
                useHeuristicOne = true; // flag enables the use of heuristic 1
                // Comparator for Node<S> objects that compares them based on the Evaluation
                // Function then the path cost
                Comparator<Node<S>> AS1nodeComparator = Comparator
                        .comparingInt(Node<S>::getEvaluationFunction) // Evaluation Function: PathCost + HeuristicValue
                        .thenComparingInt(Node<S>::getPathCost); // Then compares in case of two nodes having the same
                                                                 // evaluation value
                frontier = new PriorityQueue<>(AS1nodeComparator); // set the frontier to be a PQ that will be ordered
                                                                   // based on noceComparator
                break;
            case "AS2": // A* using Heuristic 2
                useHeuristic = true;
                useHeuristicOne = false; // this is what indicates the usage of Heuristic2
                Comparator<Node<S>> AS2nodeComparator = Comparator
                        .comparingInt(Node<S>::getEvaluationFunction)
                        .thenComparingInt(Node<S>::getPathCost);
                frontier = new PriorityQueue<>(AS2nodeComparator);
                break;
            case "GR1":
                useHeuristic = true;
                useHeuristicOne = true;
                // comparator for Node<S> objects that compares them based on their Heuritic
                // Value.
                Comparator<Node<S>> GR1nodeComparator = Comparator.comparingInt(Node<S>::getHeuristicValue);
                frontier = new PriorityQueue<>(GR1nodeComparator);
                break;
            case "GR2":
                useHeuristic = true;
                useHeuristicOne = false;
                Comparator<Node<S>> GR2nodeComparator = Comparator.comparingInt(Node<S>::getHeuristicValue);
                frontier = new PriorityQueue<>(GR2nodeComparator);
                break;
            default:
                // in case of DF or BF
                frontier = new LinkedList<>();
                break;
        }
        // getting the initial node (the root of the search tree) & adding it to the
        // queue
        Node<S> initialNode = getInitialNode();
        frontier.add(initialNode);

        // HashSet explored is for the explored nodes
        // ensures that state is marked as visited and will not be processed again
        // during the search.
        Set<String> explored = new HashSet<>();
        // stateToString(...) is a method that converts the state of the node into a
        // String
        explored.add(stateToString(initialNode.getState()));

        // while the data structure is not empty
        while (!frontier.isEmpty()) {

            Node<S> node = removeFront(frontier);
            nodeCounter++; // tracks the number of nodes processed throughout the search.

            String nodeStateHash = stateToString(node.getState()); // This line converts the current node's state into a
                                                                   // String representation, which can be used for
                                                                   // logging or storing in the explored set.
            // this block outputs information about the current node being processed
            if (visualization) {
                // printFrontier(frontier);
                System.out.println("Processing Node #" + nodeCounter);
                System.out.println("Total nodes skipped ===> " + skippedNodes);
                System.out.println("Node Hash: " + nodeStateHash);
                printNodeState(node, strategy);
            }
            // this block checks if the current node is the goal state by calling the
            // isGoalState method.
            if (isGoalState(node)) {
                System.out.println("Goal state reached!");
                System.out.println("Total nodes skipped ===> " + skippedNodes);
                printNodeState(node, strategy);
                return node;
            }
            // This line generates the successor nodes of the current node by calling
            // expandNode.
            List<Node<S>> successors = expandNode(node, !useHeuristic, explored);
            // loop iterates over each successor node generated.
            for (Node<S> successor : successors) {
                applyHeuristic(successor); // applies a heuristic function to potentially modify its properties or
                                           // evaluation.
                String successorStateHash = stateToString(successor.getState());
                explored.add(successorStateHash);
            }
            // updates the frontier by calling the qingFun
            frontier = qingFun(frontier, successors, strategy);
        }

        System.out.println("No solution found. Total nodes skipped due to being in explored: " + skippedNodes);
        return null;
    }

    private void applyHeuristic(Node<S> node) {
        // checks if heuristic function is used
        if (useHeuristic) {
            // checks if heuristic One or Two is used
            int heuristicValue = useHeuristicOne ? calculateHeuristicOne(node.getState())
                    : calculateHeuristicTwo(node.getState());
            node.setHeuristicValue(heuristicValue);
        }
    }

    private Node<S> iterativeDepthSearch(boolean visualization) {
        int depthLimit = 0;

        while (true) {
            System.out.println("Running Depth-Limited Search with depth limit: " + depthLimit);

            Set<String> explored = new HashSet<>();
            Node<S> result = depthLimitedSearch(getInitialNode(), depthLimit, visualization, new LinkedList<S>(),
                    explored);

            if (result != null) {
                System.out.println("Solution found at depth limit: " + depthLimit);
                return result;
            }

            depthLimit++;
        }
    }

    // DLS applies the same technique as DFS
    private Node<S> depthLimitedSearch(Node<S> initialNode, int depthLimit, boolean visualization,
            LinkedList<S> currentPath, Set<String> explored) {
        // Frontier Initialization
        LinkedList<Node<S>> frontier = new LinkedList<>();
        frontier.addFirst(initialNode);
        currentPath.add(initialNode.getState());

        while (!frontier.isEmpty()) {
            // A node is popped from the front of the frontier, and the corresponding state
            // is removed from currentPath.
            Node<S> node = frontier.pop();
            currentPath.removeLast(); // hence the removeLast
            nodeCounter++; // keeping track of how many nodes have been processed.
            if (visualization) {
                System.out.println("Processing Node #" + nodeCounter);
                System.out.println("Total nodes skipped due to being in explored: " + skippedNodes);
                printNodeState(node, "ID");
            }

            if (isGoalState(node)) {
                System.out.println("Goal state reached at depth limit: " + depthLimit);
                return node;
            }
            List<Node<S>> successors = expandNode(node, true, explored);
            for (Node<S> successor : successors) {
                // checks if the successor's depth is less than or equal to the depth limit and
                // that it does not create a cycle in the current path.
                if (successor.getDepth() <= depthLimit && !isCycle(successor, node, currentPath)) {
                    // the successor is added to the front of the frontier
                    // its state is added to the current path.
                    frontier.addFirst(successor);
                    currentPath.add(successor.getState());
                } else {
                    // rack how many nodes were not explored due to depth or cycle constraints.
                    GenericSearch.skippedNodes++;
                }
            }

        }

        return null;
    }

    private boolean isCycle(Node<S> child, Node<S> currentNode, LinkedList<S> currentPath) {
        // checks if the state of the child node already exists in the currentPath
        // If found in the path, it means that the algorithm has previously visited this
        // state, indicating a cycle.
        return currentPath.contains(child.getState());
    }

    private Node<S> removeFront(Queue<Node<S>> frontier) {
        return frontier.poll();
    }

    private Queue<Node<S>> qingFun(Queue<Node<S>> frontier, List<Node<S>> successors, String strategy) {
        switch (strategy) {
            case "BF":
                // Adds all successor nodes to the end of the frontier. This preserves the FIFO
                // (First In, First Out) order of BFS.
                frontier.addAll(successors);
                break;
            case "DF":
                // Checks if the frontier is a LinkedList
                if (!(frontier instanceof LinkedList)) {
                    frontier = new LinkedList<>(frontier);
                }
                for (Node<S> child : successors) {
                    // Successors are added to the front of the list, maintaining the LIFO (Last In,
                    // First Out) order of DFS.
                    ((LinkedList<Node<S>>) frontier).addFirst(child);
                }
                break;
            case "UC":
            case "AS1":
            case "AS2":
            case "GR1":
            case "GR2":
                PriorityQueue<Node<S>> priorityQueue;
                // Checks if the frontier is not a PriorityQueue
                if (!(frontier instanceof PriorityQueue)) {
                    priorityQueue = new PriorityQueue<>(frontier);
                } else {
                    priorityQueue = (PriorityQueue<Node<S>>) frontier;
                }
                // Adds all successors to the priority queue, allowing nodes to be prioritized
                // based on their cost or heuristic values.
                priorityQueue.addAll(successors);
                return priorityQueue;
        }
        return frontier;
    }

    // allowing to initiate a search based on a strategy and optional visualization.
    // returns a Node<S>, which represents the result of the search.
    public Node<S> search(String strategy, boolean visualize) {
        return generalSearch(strategy, visualize);
    }
}
