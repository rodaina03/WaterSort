package code;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

//import javax.swing.DefaultBoundedRangeModel;

public class WaterSortSearch extends GenericSearch<Bottle[]> {
    public Bottle[] bottles;
    private Map<Color, Integer> totalUnitsAvailable;
    private int numberOfBottles;
    private int capacity;

    public WaterSortSearch(String initialState) {
        this.initializeBottles(initialState);
    }

    /* initialState= "numberOfBottles;bottleCapacity;color[n,k]" */
    private void initializeBottles(String initialState) {
        totalUnitsAvailable = new HashMap<>();
        String[] parts = initialState.split(";"); // splitting the parts into an array of string
        numberOfBottles = Integer.parseInt(parts[0]);
        capacity = Integer.parseInt(parts[1]);
        this.bottles = new Bottle[numberOfBottles];
        for (int i = 0; i < numberOfBottles; i++) {
            bottles[i] = new Bottle(capacity, parts[2 + i].split(",")); // colors is in position parts[2]
            for (Color color : bottles[i].colors) {
                int count = totalUnitsAvailable.getOrDefault(color, 0);
                totalUnitsAvailable.put(color, count + 1);
            }
        }

    }

    @Override
    public Node<Bottle[]> getInitialNode() {
        return new Node<>(bottles, null, "", 0, 0);
    }

    /*
     * @Override
     * public boolean isGoalState(Node<Bottle[]> node) {
     * Bottle[] state = node.getState();
     * Map<Color, Integer> totalUnitsUsed = new HashMap<>();
     * 
     * for (Bottle bottle : state) {
     * if (!bottle.isEmpty()) {
     * Color firstColor = bottle.colors.get(0);
     * 
     * for (Color color : bottle.colors) {
     * if (!color.equals(firstColor)) {
     * return false; // Bottle contains different colors
     * }
     * }
     * 
     * int unitsUsed = totalUnitsUsed.getOrDefault(firstColor, 0) +
     * bottle.colors.size();
     * totalUnitsUsed.put(firstColor, unitsUsed);
     * 
     * if (!bottle.isFull()) {
     * int unitsAvailable = totalUnitsAvailable.getOrDefault(firstColor, 0);
     * if (unitsUsed < unitsAvailable) {
     * return false;
     * }
     * }
     * }
     * }
     * return true;
     * }
     */
    public boolean isGoalState(Node<Bottle[]> node) {
        Bottle[] state = node.getState();
        Map<Color, Integer> totalUnitsUsed = new HashMap<>();

        // Step 1: Ensure each bottle contains only one color
        for (Bottle bottle : state) {
            if (!bottle.isEmpty()) {
                Color firstColor = bottle.colors.get(0);

                // Check if all colors in the bottle are the same
                for (Color color : bottle.colors) {
                    if (!color.equals(firstColor)) {
                        return false; // Bottle contains different colors
                    }
                }

                // Step 2: Accumulate the total units used per color
                totalUnitsUsed.put(firstColor, totalUnitsUsed.getOrDefault(firstColor, 0) + bottle.colors.size());
            }
        }
        /*
         * // Step 3: Verify that total units used match total units available for each
         * color
         * for (Map.Entry<Color, Integer> entry : totalUnitsUsed.entrySet()) {
         * Color color = entry.getKey();
         * int unitsUsed = entry.getValue();
         * int unitsAvailable = totalUnitsAvailable.getOrDefault(color, 0);
         * 
         * if (unitsUsed != unitsAvailable) {
         * return false; // Mismatch in units used vs. available
         * }
         * }
         */

        return true; // All conditions met; it's a goal state
    }

    @Override
    public List<Node<Bottle[]>> expandNode(Node<Bottle[]> node, boolean compareByPathCostOnly, Set<String> explored) {
        List<Node<Bottle[]>> successors = new ArrayList<>();
        Bottle[] currentState = node.getState();

        for (int i = 0; i < currentState.length; i++) {
            for (int j = 0; j < currentState.length; j++) {
                int cost = 0;
                if (i != j && currentState[i].canPourInto(currentState[j])) {
                    Bottle[] newState = copyState(currentState, i, j);
                    cost = newState[i].pour(newState[j]);
                    String successorStateHash = stateToString(newState);
                    if (!explored.contains(successorStateHash)) {
                        Node<Bottle[]> newNode = new Node<>(newState, node, "pour_" + i + "_" + j,
                                node.getPathCost() + cost, node.getDepth() + 1);
                        newNode.setCompareByPathCostOnly(compareByPathCostOnly);
                        successors.add(newNode);
                    } else {
                        GenericSearch.skippedNodes++;
                    }
                }
            }
        }
        return successors;
    }

    private Bottle[] copyState(Bottle[] state, int bottleIndex1, int bottleIndex2) {
        Bottle[] newState = state.clone();
        newState[bottleIndex1] = new Bottle(state[bottleIndex1].capacity, new String[0]);
        newState[bottleIndex1].colors.addAll(state[bottleIndex1].colors);

        newState[bottleIndex2] = new Bottle(state[bottleIndex2].capacity, new String[0]);
        newState[bottleIndex2].colors.addAll(state[bottleIndex2].colors);

        return newState;
    }

    private void deleteState(Bottle[] state, int bottleIndex1, int bottleIndex2) {

        state[bottleIndex1] = null;
        state[bottleIndex2] = null;
        state = null;
        return;
    }

    private Bottle[] copyState(Bottle[] state) {
        Bottle[] newState = new Bottle[state.length];
        for (int i = 0; i < state.length; i++) {
            newState[i] = new Bottle(state[i].capacity, new String[0]);
            newState[i].colors.addAll(state[i].colors);
        }
        return newState;
    }

    private boolean checkBottleComplete(Bottle bottle) {
        if (!bottle.isEmpty()) {
            Color firstColor = bottle.colors.get(0);
            if (!bottle.isFull()) {
                return false;
            }
            for (Color color : bottle.colors) {
                if (color != firstColor) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    protected int calculateHeuristicOne(Bottle[] state) {
        // return
        // Math.max(Math.max(groupingHeuristic(state),misplacedColorsHeuristic(state)),
        // Math.max(linearConflictHeuristic(state),bottleFillHeuristic(state)));
        return bottleFillHeuristic(state);
    }

    protected int colorDisplacementHeuristic(Bottle[] state) {
        int totalMoves = 0;

        for (Bottle bottle : state) {
            if (!bottle.isEmpty()) {
                Color firstColor = bottle.colors.get(0);

                // Count how many colors in this bottle are not the same as the first color
                int mismatchedColors = 0;
                for (Color color : bottle.colors) {
                    if (color != firstColor) {
                        mismatchedColors++;
                    }
                }

                // Each mismatched color will need at least one move to resolve
                totalMoves += mismatchedColors;
            }
        }

        return totalMoves;
    }

    @Override
    protected int calculateHeuristicTwo(Bottle[] state) {
        return misplacedColorsHeuristic(state);
    }

    protected int misplacedColorsHeuristic(Bottle[] state) {
        int misplacedCount = 0;
        for (Bottle bottle : state) {
            if (bottle.isEmpty())
                continue;
            Color topColor = bottle.topColor();
            for (Color color : bottle.colors) {
                if (color != topColor && color != Color.e) {
                    misplacedCount++;
                }
            }
        }
        return misplacedCount;
    }

    protected int bottleFillHeuristic(Bottle[] state) {
        int incorrectBottles = 0;
        for (Bottle bottle : state) {
            if (!bottle.isEmpty()) {
                Color firstColor = bottle.topColor();
                boolean correctlyFilled = true;
                for (Color color : bottle.colors) {
                    if (color != firstColor && color != Color.e) {
                        correctlyFilled = false;
                        break;
                    }
                }
                if (!correctlyFilled) {
                    incorrectBottles++;
                }
            }
        }
        return incorrectBottles;
    }

    /*
     * @Override
     * protected long stateToInt(Bottle[] state) {
     * long hash = 0;
     * int prime = 31;
     * for (Bottle bottle : state) {
     * for (Color color : bottle.colors) {
     * hash = prime * hash + colorToInt(color);
     * }
     * hash = prime * hash + -1; // Separator between bottles
     * }
     * return hash;
     * }
     */

    protected String stateToString(Bottle[] state) {
        StringBuilder stateStr = new StringBuilder();
        for (Bottle bottle : state) {
            for (Color color : bottle.colors) {
                stateStr.append(colorToChar(color)).append(",");
            }
            stateStr.append("|"); // Separator between bottles
        }
        return stateStr.toString();
    }

    private char colorToChar(Color color) {
        switch (color) {
            case red:
                return 'r';
            case green:
                return 'g';
            case blue:
                return 'b';
            case yellow:
                return 'y';
            case orange:
                return 'o';
            default:
                return 'e';
        }
    }

    private int colorToInt(Color color) {
        switch (color) {
            case red:
                return 1;
            case green:
                return 2;
            case blue:
                return 3;
            case yellow:
                return 4;
            case orange:
                return 5;
            default:
                return 0;
        }
    }

    @Override
    protected void printNodeState(Node<Bottle[]> node, String Strategy) {
        System.out.println("Action: " + node.getAction());
        for (Bottle bottle : node.getState()) {
            System.out.println(bottle.colors);
        }
        switch (Strategy) {
            case "AS1":
            case "AS2":
                System.out.println("Evaluation value ==> " + node.getEvaluationFunction());
                break;
            case "GR1":
            case "GR2":
                System.out.println("Heuristic value ==> " + node.getHeuristicValue());
                break;
            default:
                System.out.println(
                        "Depth value ==> " + node.getDepth() + "\n" + "Path Cost value ==> " + node.getPathCost());
        }

        System.out.println("-----------------------------------------------------------------");
    }

    public static String solve(String initialState, String strategy, boolean visualize) {
        WaterSortSearch problem = new WaterSortSearch(initialState);

        if (!problem.checkSolvability())
            return "NOSOLUTION";
        Node<Bottle[]> solution = problem.search(strategy, visualize);
        if (solution != null) {
            StringBuilder finalSolution = new StringBuilder();
            Node<Bottle[]> currentNode = solution;
            while (currentNode != null) {
                if (!currentNode.getAction().isEmpty()) {
                    finalSolution.insert(0, currentNode.getAction() + ",");
                }
                currentNode = currentNode.getParent();
            }
            if (finalSolution.length() > 0 && finalSolution.charAt(finalSolution.length() - 1) == ',') {
                finalSolution.deleteCharAt(finalSolution.length() - 1);
            }
            finalSolution.append(";");
            finalSolution.append(solution.getPathCost()).append(";");
            finalSolution.append(GenericSearch.nodeCounter).append(";");

            return finalSolution.toString();

        } else
            return "NOSOLUTION";
    }

    private boolean checkSolvability() {
        int totalRequiredBottles = 0;
        for (Map.Entry<Color, Integer> entry : totalUnitsAvailable.entrySet()) {
            int count = entry.getValue();
            int required = (int) Math.ceil((double) count / capacity);
            totalRequiredBottles += required;
        }
        if (totalRequiredBottles > numberOfBottles) {
            return false;
        }
        return true;
    }

    public static void main(String[] args) {
        Runtime runtime = Runtime.getRuntime();
        long usedMemoryBefore = runtime.totalMemory() - runtime.freeMemory();

        long startTime = System.currentTimeMillis();

        // String initialState = "6;4;" + "r,b,y,g;" + "b,r,o,y;" + "y,o,r,b;" +
        // "o,g,g,o;" + "e,e,e,e;" + "e,e,e,e;";
        /*
         * BF : 13 ;
         * pour_0_4,pour_1_0,pour_1_4,pour_1_5,pour_2_1,pour_2_5,pour_2_4,pour_0_2,
         * pour_0_1,pour_3_5,pour_3_0;13;768;
         * UC : 13 ;
         * pour_0_5,pour_1_0,pour_1_5,pour_1_4,pour_2_1,pour_2_4,pour_2_5,pour_3_4,
         * pour_0_2,pour_0_1,pour_3_0;13;680;
         * AS1: 13 ;
         * pour_0_4,pour_1_0,pour_1_4,pour_1_5,pour_2_1,pour_2_5,pour_3_5,pour_2_4,
         * pour_0_2,pour_0_1,pour_3_0;13;250;
         * AS2: 13 ;
         * pour_0_4,pour_1_0,pour_1_4,pour_1_5,pour_2_1,pour_2_5,pour_2_4,pour_3_5,
         * pour_0_2,pour_0_1,pour_3_0;13;413;
         * GR1: 18 ;
         * pour_0_4,pour_1_0,pour_1_4,pour_0_5,pour_2_0,pour_1_2,pour_1_0,pour_2_1,
         * pour_2_4,pour_2_5,pour_0_2,pour_3_1,pour_3_0;18;19;
         * GR2: 16 ;
         * pour_0_4,pour_0_5,pour_1_5,pour_1_4,pour_2_0,pour_2_1,pour_2_4,pour_3_1,
         * pour_2_5,pour_0_2,pour_3_0,pour_1_3;16;15;
         */

        // String initialState = "7;4;" + "r,b,o,g;" + "b,g,r,o;" + "o,r,b,y;" +
        // "y,g,y,r;" + "y,b,g,o;" + "e,e,e,e;" + "e,e,e,e;";
        /*
         * BF : 17 ;
         * pour_0_5,pour_0_6,pour_1_6,pour_2_0,pour_2_5,pour_2_6,pour_3_2,pour_1_3,
         * pour_1_5,pour_0_1,pour_3_0,pour_3_2,pour_4_2,pour_4_6,pour_4_0;17;16680;
         * UC : 17 ;
         * pour_4_6,pour_4_5,pour_3_6,pour_1_5,pour_3_4,pour_3_6,pour_1_4,pour_1_3,
         * pour_0_3,pour_0_5,pour_0_1,pour_4_0,pour_2_4,pour_2_3,pour_2_5;17;11439;
         * AS1: 17 ;
         * pour_4_6,pour_3_6,pour_4_5,pour_3_4,pour_1_5,pour_1_4,pour_3_6,pour_1_3,
         * pour_2_1,pour_0_3,pour_0_5,pour_0_1,pour_2_3,pour_2_5,pour_2_6,pour_4_0;18;
         * 1788;
         * AS2: 17 ;
         * pour_4_5,pour_4_6,pour_1_6,pour_3_5,pour_3_4,pour_3_5,pour_0_3,pour_0_6,
         * pour_1_4,pour_1_3,pour_0_1,pour_4_0,pour_2_4,pour_2_3,pour_2_6;17;3893;
         * GR1: 21 ;
         * pour_0_5,pour_1_0,pour_0_6,pour_2_0,pour_2_5,pour_2_6,pour_4_2,pour_4_6,
         * pour_1_4,pour_1_5,pour_0_1,pour_4_0,pour_1_4,pour_3_2,pour_3_0,pour_3_2;21;
         * 22;
         * GR2: 17 ;
         * pour_0_5,pour_0_6,pour_1_6,pour_2_0,pour_2_5,pour_2_6,pour_4_2,pour_4_6,
         * pour_1_4,pour_1_5,pour_0_1,pour_4_0,pour_3_2,pour_3_0,pour_3_2;17;17;
         */

        // String initialState = "7;3;" + "g,y,r;" + "b,o,g;" + "r,b,o;" + "y,g,r;" +
        // "o,r,y;" + "e,e,e;" + "e,e,e;";
        /*
         * BF : 11 ;
         * pour_0_5,pour_1_6,pour_3_0,pour_3_5,pour_2_3,pour_2_6,pour_1_2,pour_4_2,
         * pour_4_3,pour_0_4;11;4614;
         * UC : 10 ;
         * pour_2_6,pour_2_5,pour_4_2,pour_4_6,pour_3_4,pour_1_5,pour_1_2,pour_0_1,
         * pour_0_4,pour_3_1;10;3264;
         * AS1: 10 ;
         * pour_1_5,pour_2_6,pour_2_5,pour_4_2,pour_4_6,pour_3_4,pour_1_2,pour_3_1,
         * pour_3_6,pour_0_1,pour_0_4;11;115;
         * AS2: 10 ;
         * pour_2_5,pour_2_6,pour_4_2,pour_4_5,pour_3_4,pour_1_6,pour_1_2,pour_0_1,
         * pour_0_4,pour_3_1;10;599;
         * GR1: 16 ;
         * pour_0_5,pour_3_0,pour_3_5,pour_2_3,pour_1_2,pour_4_1,pour_4_3,pour_0_4,
         * pour_5_6,pour_1_5,pour_1_6,pour_2_1;16;18;
         * GR2: 13 ;
         * pour_0_5,pour_0_6,pour_2_0,pour_1_2,pour_3_6,pour_3_5,pour_4_1,pour_4_0,
         * pour_4_6,pour_2_4,pour_1_2;13;15;
         */

        // String initialState = "6;3;" + "b,r,g;" + "y,o,b;" + "g,y,r;" + "o,b,y;" +
        // "r,o,g;" + "e,e,e;";
        /*
         * BF : 13 ;
         * pour_3_5,pour_0_3,pour_4_0,pour_4_5,pour_2_4,pour_1_2,pour_1_5,pour_3_1,
         * pour_2_3,pour_0_2;13;38;
         * UC : 13 ;
         * pour_3_5,pour_0_3,pour_4_0,pour_4_5,pour_2_4,pour_1_2,pour_1_5,pour_3_1,
         * pour_2_3,pour_0_2;13;38;
         * AS1: 13 ;
         * pour_0_5,pour_4_0,pour_3_4,pour_5_3,pour_4_5,pour_2_4,pour_1_2,pour_1_5,
         * pour_3_1,pour_2_3,pour_0_2;15;38;
         * AS2: 13 ;
         * pour_3_5,pour_0_3,pour_4_0,pour_4_5,pour_2_4,pour_1_2,pour_1_5,pour_3_1,
         * pour_2_3,pour_0_2;13;38;
         * GR1: 15 ;
         * pour_0_5,pour_4_0,pour_3_4,pour_5_3,pour_4_5,pour_2_4,pour_1_2,pour_1_5,
         * pour_3_1,pour_2_3,pour_0_2;15;19;
         * GR2: 15 ;
         * pour_0_5,pour_4_0,pour_3_4,pour_5_3,pour_4_5,pour_2_4,pour_1_2,pour_1_5,
         * pour_3_1,pour_2_3,pour_0_2;15;19;
         */

        // String initialState = "5;4;" + "b,y,r,b;" + "b,y,r,r;" +"y,r,b,y;" +
        // "e,e,e,e;" + "e,e,e,e;";
        /*
         * BF : 8 ;
         * pour_0_3,pour_0_4,pour_1_3,pour_1_4,pour_0_1,pour_2_4,pour_2_1,pour_2_0;8;
         * 537;
         * UC : 8 ;
         * pour_0_4,pour_0_3,pour_1_4,pour_1_3,pour_0_1,pour_2_3,pour_2_1,pour_2_0;8;
         * 401;
         * AS1: 8 ;
         * pour_1_4,pour_1_3,pour_2_3,pour_0_4,pour_0_3,pour_0_1,pour_2_1,pour_2_0;8;
         * 122;
         * AS2: 8 ;
         * pour_1_4,pour_1_3,pour_2_3,pour_2_1,pour_2_4,pour_0_4,pour_0_2,pour_0_1;8;
         * 103;
         * GR1: 12 ;
         * pour_1_4,pour_0_4,pour_0_1,pour_0_3,pour_0_4,pour_1_0,pour_1_3,pour_2_0,
         * pour_2_3,pour_2_4;12;18;
         * GR2: 8 ;
         * pour_1_3,pour_1_4,pour_0_3,pour_0_4,pour_0_1,pour_2_4,pour_2_1,pour_2_0;8;10
         */

        // NOSOLUTION
        // String initialState = "8;4;" + "r,o,y,b;" + "g,g,r,o;" + "b,y,g,r;" +
        // "o,y,b,g;" + "r,b,o,y;" + "y,g,r,b;" + "e,e,e,e;" + "e,e,e,e;";
        // String initialState = "6;5;" + "r,b,g,y,o;" + "o,y,g,r,b;" + "b,r,o,g,y;" +
        // "y,g,o,r,b;" + "g,r,b,y,o;" + "e,e,e,e,e;";
        // String initialState = "5;3;" + "g,b,r;" + "y,o,g;" + "b,y,r;" + "o,b,g;" +
        // "e,e,e;";
        // String initialState = "8;5;" + "r,b,y,g,o;" + "b,o,r,y,g;" + "y,g,o,r,b;" +
        // "o,r,y,b,g;" + "g,b,o,y,r;" + "r,g,b,o,y;" + "e,e,e,e,e;" + "e,e,e,e,e;";

        /*
         * String initialState = "9;5;" +
         * "r,b,o,g,y;" + // Bottle 0
         * "b,g,r,o,y;" + // Bottle 1
         * "o,r,b,g,y;" + // Bottle 2
         * "y,g,o,b,r;" + // Bottle 3
         * "r,y,g,b,o;" + // Bottle 4
         * "b,o,r,r,r;" + // Bottle 5
         * "e,e,e,e,e;" + // Bottle 6
         * "e,e,e,e,e;" + // Bottle 7
         * "e,e,e,e,e;"; // Bottle 8
         */

        String initialState = "5;4;" + "b,y,r,b;" + "b,y,r,r;" +
                "y,r,b,y;" + "e,e,e,e;" + "e,e,e,e;";
        System.out.println(solve(initialState, "BF", true));

        long endTime = System.currentTimeMillis();

        long usedMemoryAfter = runtime.totalMemory() - runtime.freeMemory();
        long timeTaken = endTime - startTime;

        System.out.println("Time taken: " + timeTaken + " milliseconds");
        System.out.println("Memory used: " + ((usedMemoryAfter - usedMemoryBefore) / 1024) + " KB");

    }

}
/*
 * [green, green, green, green]
 * [orange]
 * [yellow, yellow, yellow, yellow]
 * [red, red]
 * [orange, orange, orange]
 * [blue, blue, blue, blue]
 * [red, red]
 */
