package torgeros.connect3.agent;

import java.util.ArrayList;
import java.util.HashMap;

import torgeros.connect3.ConnectThree.PlayerColor;
import torgeros.connect3.Board;
import torgeros.connect3.Board.Field;
import torgeros.connect3.Util;

public class Ai implements Agent {
    Field[][] currentBoard;
    int boardWidth;
    int boardHeight;
    String serializedCurrentBoard;

    final Field maximizingColor; // own color
    final Field minimizingColor; // opponents color
    /**
     * initialized node for the heuristic to work with, to safe processing time.
     * including the margin of two null-Fields as discussed in the heuristic method.
     */
    Field[][] nodeWithMargin;


    /**
     * depth for first iteration.
     * Has to be a range for which any initial state takes less than START_OF_CUTOFF_MS to completely expand.
     */
    protected int START_SEARCH_DEPTH = 4;

    /**
     * time in milliseconds at which predicting is stopped.
     * - ping to server is ~.15 ms.
     * - sucessfully topping everything once the timer ha run out: eval gives ~1 ms.
     */
    protected int START_OF_CUTOFF_MS = 9900;

    /**
     * score given (positive/negative) for a safe win.
     * high enough to outnumber every other combination of scores.
     * low enough to not overflow if if e.g. two wins occur at the same time (run of 4)
     */
    final int SCORE_SAFE_WIN = 0xFFFF;

    /**
     * run of three: safe win.
     * run of two: good, score 50. Max number of 2-runs is 6 for a 2x2 square.
     * positive score for max player, negative score for min player.
     */
    final int[] SCORE_FOR_RUNS = {0, 0, 50, SCORE_SAFE_WIN};

    long startOfCurrentOperationTimestamp;

    private static class BestChildNode {
        public int searchedDepth;
        public Field[][] node;
        public int value;
    }

    /**
     * keep tracks of the best child that has been discovered for a specific board(parent).
     * key is a serialized board, see #serializeNode(node).
     * value is a BestChildNode object
     * 
     * there is no need to be able to tell apart whether it is max's or min's turn.
     * that information is implicitly contained in the hashstring.
     * a bestChild can therefore be either "best" for max (high value) or for min (low value).
     */
    HashMap<String, BestChildNode> knownBestChildNodes;

    /**
     * used for the following stateCounter object.
     * extends the HashMap by a function to in-/decrease values that are not necessarily present in the map.
     */
    class StateCounter extends HashMap<String, Integer> {
        public StateCounter() {
            super();
        }

        public void increase(final String serializedNode) {
            if (super.containsKey(serializedNode)) {
                super.replace(serializedNode, super.get(serializedNode) + 1);
            } else {
                super.put(serializedNode, 1);
            }
        }

        public void decrease(final String serializedNode)  {
            super.replace(serializedNode, super.get(serializedNode) - 1);
        }

        public int get(final String serializedNode) {
            if (super.get(serializedNode) == null) {
                return 0;
            }
            return super.get(serializedNode);
        }
    }

    /**
     * counts the number of times a state has been visited.
     * used to check for threefold repitition-checking.
     * key is a serialized board, see #serializeNode(node).
     * value is a Integer denoting the time a state has been visited.
     * 
     * This stateCounter is used to keep track of the actual game history across moves.
     * THIS OBJECT IS MODIFIED DURING A RUN ON MINIMAX, BUT SHOULD RE-ENTER IT'S OLD STATE ON RETURN.
     */
    StateCounter stateCounter;

    public Ai(PlayerColor ownColor) {
        if (ownColor == PlayerColor.WHITE_PLAYER) {
            maximizingColor = Field.WHITE;
            minimizingColor = Field.BLACK;
        } else {
            maximizingColor = Field.BLACK;
            minimizingColor = Field.WHITE;
        }

        knownBestChildNodes = new HashMap<String, BestChildNode>();
        stateCounter = new StateCounter();
        System.out.printf("created new AI that plays %s (%c)%n", ownColor.getClientName(), maximizingColor.getChar());
    }

    /**
     * Set the given board as the current state.
     * Increases stateCounter, so this function should be called once FOR EACH OF BOTH PLAYER'S moves.
     * This function therefore also be rephrased as a enterGameState(board) function.
     * @param board
     */
    public void updateInternalBoard(final Board board) {
        currentBoard = board.getCopyOfBoard();
        boardWidth = board.width;
        boardHeight = board.height;
        serializedCurrentBoard = serializeNode(currentBoard);
        nodeWithMargin = new Field[boardWidth + 4][boardHeight + 4]; // set all fields to null

        // increase stateCounter for new board that opponent "created".
        stateCounter.increase(serializedCurrentBoard);
    }

    /**
     * Perform minimax with alphabeta pruning for all the current node/board.
     * All children of the board are evaluated.
     * The "splitting" in subtrees or each child makes it easy to get the actual node out and not just the value/rating of the node.
     * @return the best of move in the defiend syntax
     */
    public String getBestMove() {
        startOfCurrentOperationTimestamp = System.currentTimeMillis();

        int bestNodesValue = Integer.MIN_VALUE;
        Field[][] bestNode = null;

        int depth;
        // if we already know the best child for the current board, we can start searching where we left off.
        if (knownBestChildNodes.containsKey(serializedCurrentBoard)) {
            depth = knownBestChildNodes.get(serializedCurrentBoard).searchedDepth + 1;
            System.out.printf("we have been in this state before, starting search at depth %d.%n", depth);
        } else {
            depth = START_SEARCH_DEPTH;
        }
        // start iterative deepening
        for ( ; ; depth++) {
            Field[][] bestNodeForThisDepth = null;

            /*
            rewritten max part of minimax,
            for getting the node instead of its value.
            This includes alpha-pruning (beta is irrelevant because we are maxing)
            */
            int bestValueForThisDepth = Integer.MIN_VALUE;
            int alpha = Integer.MIN_VALUE;
            for (Field[][] child : getChildren(currentBoard, maximizingColor)) {
                String serializedChild = serializeNode(child);
                stateCounter.increase(serializedChild);
                int mm = minimax(child, depth - 1, alpha, Integer.MAX_VALUE, false);
                stateCounter.decrease(serializedChild);
                if (shouldStop()) {
                    break;
                }
                if (mm > bestValueForThisDepth) {
                    // if current child is better than best known: replace.
                    bestValueForThisDepth = mm;
                    bestNodeForThisDepth = child;
                }
                alpha = Integer.max(alpha, bestValueForThisDepth);
            }

            // if we ran out of time during the search, skip storing the best result
            if (shouldStop()) {
                break;
            }
            // if the search at this depth was able to complete, overwrite bestNode
            bestNode = bestNodeForThisDepth;
            bestNodesValue = bestValueForThisDepth;
        }
        // complete search was to depth-1
        System.out.printf("completed search to depth %d.%n", depth-1);
        updateBestKnownChild(serializedCurrentBoard, bestNode, depth-1, bestNodesValue);
        String move = getMoveFromDiff(currentBoard, knownBestChildNodes.get(serializedCurrentBoard).node);
        System.out.printf("got minimax move %s%n", move);
        return move;
    }

    /**
     * get the best child for the given parent, including provious knowledge (i.e. the history hashmap).
     * the resulting BestChildNode is put/replaced in the history hashmap.
     * value is not needed for children comparison, because deeper is always better.
     * this method guarantees that there is an entry for serializedParent in history when it returns.
     * @param serializedParent see #serializeNode()
     * @param proposedBestChild 
     * @param searchedDepth the depth where the proposedBestChild was found
     * @param value the minimax value of proposedBestChild
     * @return
     */
    protected void updateBestKnownChild(final String serializedParent, final Field[][] proposedBestChild, int searchedDepth, int value) {
        if (knownBestChildNodes.containsKey(serializedParent)) {
            if (searchedDepth > knownBestChildNodes.get(serializedParent).searchedDepth) {
                // proposedBestChild is actually better/deeper than knownBest
                BestChildNode newBest = new BestChildNode();
                newBest.node = proposedBestChild;
                newBest.value = value;
                newBest.searchedDepth = searchedDepth;
                knownBestChildNodes.replace(serializedParent, newBest);
            } // else program failed to search deeper than known nodes
        } else {
            if (proposedBestChild == null) {
                System.err.println("no child found. halting");
                while(true);
            }
            BestChildNode newBest = new BestChildNode();
            newBest.node = proposedBestChild;
            newBest.value = value;
            newBest.searchedDepth = searchedDepth;
            knownBestChildNodes.put(serializedParent, newBest);
        }
    }

    /**
     * https://en.wikipedia.org/wiki/Minimax#Pseudocode
     * https://en.wikipedia.org/wiki/Alpha%E2%80%93beta_pruning#Pseudocode (soft-fail)
     * MAX player always is this agent. The opponent is MIN.
     * the higher the return value, the better for this agent.
     */
    protected int minimax(Field[][] node, int depth, int alpha, int beta, boolean maximizingPlayer) {
        if (shouldStop()) {
            return 0;
        }
        // if this state leads to a direct draw, return utility of 0
        if (stateCounter.get(serializeNode(node)) == 3) {
            return 0;
        }
        // if we already know this node at a deeper search level: use the known value
        if (knownBestChildNodes.containsKey(serializeNode(node))) {
            if (knownBestChildNodes.get(serializeNode(node)).searchedDepth >= depth) {
                return knownBestChildNodes.get(serializeNode(node)).value;
            }
        }
        if (depth == 0 || isTerminal(node)) {
            return heuristic(node);
        }
        int value;
        if (maximizingPlayer) {
            value = Integer.MIN_VALUE;
            Field[][] bestChild = null;
            for (Field[][] child : getChildren(node, maximizingColor)) {
                String serializedChild = serializeNode(child);
                stateCounter.increase(serializedChild);
                int childValue = minimax(child, depth - 1, alpha, beta, false);
                stateCounter.decrease(serializedChild);
                if (childValue > value) {
                    value = childValue; // replacement for max function
                    bestChild = child;
                }
                alpha = Integer.max(alpha, value);
                if (value >= beta) {
                    break;
                }
            }
            updateBestKnownChild(serializeNode(node), bestChild, depth, value);
            return value;
        } else {
            value = Integer.MAX_VALUE;
            Field[][] bestChild = null;
            for (Field[][] child : getChildren(node, minimizingColor)) {
                String serializedChild = serializeNode(child);
                stateCounter.increase(serializedChild);
                int childValue = minimax(child, depth - 1, alpha, beta, true);
                stateCounter.decrease(serializedChild);
                if (childValue < value) {
                    value = childValue; // replacement for min function
                    bestChild = child;
                }
                beta = Integer.min(beta, value);
                if (value <= alpha) {
                    break;
                }
            }
            updateBestKnownChild(serializeNode(node), bestChild, depth, value);
            return value;
        }
    }

    protected boolean shouldStop() {
        return (System.currentTimeMillis() - startOfCurrentOperationTimestamp) > START_OF_CUTOFF_MS;
    }

    protected String getMoveFromDiff(final Field[][] current, final Field[][] next) {
        int oldX = -1;
        int oldY = -1;
        int newX = -1;
        int newY = -1;
        for (int y = 0; y < boardHeight; y++) {
            for (int x = 0; x < boardWidth; x++) {
                if (current[x][y] != Field.EMPTY && next[x][y] == Field.EMPTY) {
                    // x,y is the field that stone moved away from
                    oldX = x;
                    oldY = y;
                }
                if (next[x][y] != Field.EMPTY && current[x][y] == Field.EMPTY) {
                    // x,y is the new field
                    newX = x;
                    newY = y;
                }
            }
        }
        if (newX == oldX) {
            if (newY > oldY) {
                return String.format("%d%dS", oldX+1, oldY+1);
            } else {
                return String.format("%d%dN", oldX+1, oldY+1);
            }
        } else {
            if (newX > oldX) {
                return String.format("%d%dE", oldX+1, oldY+1);
            } else {
                return String.format("%d%dW", oldX+1, oldY+1);
            }
        }
    }

    /**
     * generates all children of a node. all children are new objects.
     */
    protected ArrayList<Field[][]> getChildren(final Field[][] node, final Field movableColor) {
        ArrayList<Field[][]> list = new ArrayList<Field[][]>();
        for (int y = 0; y < boardHeight; y++) {
            for (int x = 0; x < boardWidth; x++) {
                if (node[x][y] != movableColor) {
                    continue;
                }
                /*
                for each direction:
                if target is within bounds and target is empty:
                set field to empty
                set target to color
                add 
                */
                //WEST
                if (x > 0 && node[x-1][y] == Field.EMPTY) {
                    Field[][] child = Util.copyFieldArray(node);
                    child[x][y] = Field.EMPTY;
                    child[x-1][y] = movableColor;
                    list.add(child);
                }
                //EAST
                if (x + 1 < boardWidth && node[x+1][y] == Field.EMPTY) {
                    Field[][] child = Util.copyFieldArray(node);
                    child[x][y] = Field.EMPTY;
                    child[x+1][y] = movableColor;
                    list.add(child);
                }
                //NORTH
                if (y > 0 && node[x][y-1] == Field.EMPTY) {
                    Field[][] child = Util.copyFieldArray(node);
                    child[x][y] = Field.EMPTY;
                    child[x][y-1] = movableColor;
                    list.add(child);
                }
                //SOUTH
                if (y + 1 < boardHeight && node[x][y+1] == Field.EMPTY) {
                    Field[][] child = Util.copyFieldArray(node);
                    child[x][y] = Field.EMPTY;
                    child[x][y+1] = movableColor;
                    list.add(child);
                }
            }
        }
        return list;
    }

    protected boolean isTerminal(final Field[][] node) {
        return getWinner(node) != Field.EMPTY;
    }

    protected Field getWinner(final Field[][] node) {
        //vertical
        //diagonal \
        //diagonal /
        for (int y = 0; y < boardHeight - 2; y++) {
            for (int x = 0; x < boardWidth; x++) {
                if (node[x][y] == Field.EMPTY) {
                    continue;
                }
                if (node[x][y] == node[x][y+1] && node[x][y] == node[x][y+2]) {
                    return node[x][y];
                }
                if (x+2 < boardWidth
                        // x+2 < width means index x+2 is within the array
                        && node[x][y] == node[x+1][y+1]
                        && node[x][y] == node[x+2][y+2]) {
                            return node[x][y];
                }
                if (x-2 > 0
                        // x-2 > 0 means index x-2 is within the array
                        && node[x][y] == node[x-1][y+1]
                        && node[x][y] == node[x-2][y+2]) {
                            return node[x][y];
                }
            }
        }

        //horizontal
        for (int y = 0; y < boardHeight; y++) {
            for (int x = 0; x < boardWidth - 2; x++) {
                if (node[x][y] == Field.EMPTY) {
                    continue;
                }
                if (node[x][y] == node[x+1][y] && node[x][y] == node[x+2][y]) {
                    return node[x][y];
                }
            }
        }
        return Field.EMPTY;
    }
    /**
     * heuristic function for minimax
     * 
     * Concept:
     * For each direction (vertical, horizontal, 2x diagonal):
     * For every field check if there is xxx starting here: safe win
     * For every field check if there is xx? or x?x (run of 2) starting here
     * 
     * @param input_node
     */
    protected int heuristic(final Field[][] input_node) {
        //create node with margin 2, to be able to run checks with caring about board borders.
        //margin is initialized with null, so checking with == is safe
        for (int y = 0; y < boardHeight; y++) {
            for (int x = 0; x < boardWidth; x++) {
                nodeWithMargin[x+2][y+2] = input_node[x][y];
            }
        }
        int rating = 0;
        /*
        looping from 2 to len+2 exclusive, i.e. every field of the original board.
        the code is checking lots of ?-null-null runs, but that saves a lot of branching and reiteration.
        furthermore, most of the ?-null-null runs are skipped because they are EMPTY-null-null
        */
        for (int y = 2; y < boardHeight + 2; y++) {
            for (int x = 2; x < boardWidth + 2; x++) {
                if (nodeWithMargin[x][y] == Field.EMPTY) {
                    continue;
                }
                int runLength; //count of stones in the interesting three fields
                boolean isMaxPlayer = nodeWithMargin[x][y] == maximizingColor;
                //vertical score
                runLength = 1 + ((nodeWithMargin[x][y] == nodeWithMargin[x][y+1])?1:0) + ((nodeWithMargin[x][y] == nodeWithMargin[x][y+2])?1:0);
                if (runLength == 3) {
                    return isMaxPlayer ? SCORE_SAFE_WIN : -SCORE_SAFE_WIN;
                }
                rating += isMaxPlayer ? SCORE_FOR_RUNS[runLength] : -SCORE_FOR_RUNS[runLength];
                //diagonal \ score
                runLength = 1 + ((nodeWithMargin[x][y] == nodeWithMargin[x+1][y+1])?1:0) + ((nodeWithMargin[x][y] == nodeWithMargin[x+2][y+2])?1:0);
                if (runLength == 3) {
                    return isMaxPlayer ? SCORE_SAFE_WIN : -SCORE_SAFE_WIN;
                }
                rating += isMaxPlayer ? SCORE_FOR_RUNS[runLength] : -SCORE_FOR_RUNS[runLength];
                //diagonal / score
                runLength = 1 + ((nodeWithMargin[x][y] == nodeWithMargin[x-1][y+1])?1:0) + ((nodeWithMargin[x][y] == nodeWithMargin[x-2][y+2])?1:0);
                if (runLength == 3) {
                    return isMaxPlayer ? SCORE_SAFE_WIN : -SCORE_SAFE_WIN;
                }
                rating += isMaxPlayer ? SCORE_FOR_RUNS[runLength] : -SCORE_FOR_RUNS[runLength];
                //horizontal score
                runLength = 1 + ((nodeWithMargin[x][y] == nodeWithMargin[x+1][y])?1:0) + ((nodeWithMargin[x][y] == nodeWithMargin[x+2][y])?1:0);
                if (runLength == 3) {
                    return isMaxPlayer ? SCORE_SAFE_WIN : -SCORE_SAFE_WIN;
                }
                rating += isMaxPlayer ? SCORE_FOR_RUNS[runLength] : -SCORE_FOR_RUNS[runLength];
            }
        }
        return rating;
    }

    private String serializeNode(Field[][] node) {
        String s = "";
        for (int y = 0; y < boardHeight; y++) {
            for (int x = 0; x < boardWidth; x++) {
                s += node[x][y].getChar();
            }
        }
        return s;
    }
}
