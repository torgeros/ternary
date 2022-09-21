package torgeros.connect3.agent;

import java.util.ArrayList;

import torgeros.connect3.ConnectThree.PlayerColor;
import torgeros.connect3.Board;
import torgeros.connect3.Board.Field;
import torgeros.connect3.Util;

public class Ai implements Agent {
    Field[][] currentBoard;
    int boardWidth;
    int boardHeight;

    final Field maximizingColor; // own color
    final Field minimizingColor; // opponents color

    /**
     * depth for first iteration.
     * Has to be a range for which any initial state takes less than START_OF_CUTOFF_MS to completely expand.
     */
    protected int START_SEARCH_DEPTH = 9;

    /**
     * time in milliseconds at which predicting is stopped.
     * - ping to server is ~.15 ms.
     * - sucessfully topping everything once the timer ha run out: eval gives ~1 ms.
     */
    protected int START_OF_CUTOFF_MS = 9900;

    /**
     * the score for a definite win.
     * This is used to permaturely break from the current.children loop.
     */
    protected int SCORE_SURE_WIN = 1000;

    long startOfCurrentOperationTimestamp;

    public Ai(PlayerColor ownColor) {
        if (ownColor == PlayerColor.WHITE_PLAYER) {
            maximizingColor = Field.WHITE;
            minimizingColor = Field.BLACK;
        } else {
            maximizingColor = Field.BLACK;
            minimizingColor = Field.WHITE;
        }
        System.out.printf("created new AI that plays %s (%c)%n", ownColor.getClientName(), maximizingColor.getChar());
    }

    public void updateInternalBoard(Board board) {
        currentBoard = board.getCopyOfBoard();
        boardWidth = board.width;
        boardHeight = board.height;
    }

    /**
     * Perform minimax with alphabeta pruning for all the current node/board.
     * All children of the board are evaluated.
     * The "splitting" in subtrees or each child makes it easy to get the actual node out and not just the value/rating of the node.
     * @return the best of move in the defiend syntax
     */
    public String getBestMove() {
        startOfCurrentOperationTimestamp = System.currentTimeMillis();

        int bestScore = Integer.MIN_VALUE;
        Field[][] bestNode = null;

        // start iterative deepening
        for (int depth = START_SEARCH_DEPTH; ; depth++) {
            Field[][] bestNodeForThisDepth = null;
            
            /*
            rewritten max part of minimax,
            for getting the node instead of its value.
            This includes alpha-pruning (beta is irrelevant because we are maxing)
            */
            int value = Integer.MIN_VALUE;
            int alpha = Integer.MIN_VALUE;
            for (Field[][] child : getChildren(currentBoard, maximizingColor)) {
                int mm = minimax(child, depth - 1, alpha, Integer.MAX_VALUE, false);
                if (shouldStop()) {
                    break;
                }
                if (mm > value) {
                    // if current child is better than best known: replace.
                    value = mm;
                    bestNodeForThisDepth = child;
                    if (value >= SCORE_SURE_WIN) {
                        // SCORE_SURE_WIN is a MAX-winning leaf node.
                        // if we definitely win on this node, we can skip the rest
                        break;
                    }
                }
                alpha = Integer.max(alpha, value);
            }
            if (shouldStop()) {
                break;
            }

            //if the search was able to complete, overwrite bestNode
            bestNode = bestNodeForThisDepth;
            if (value >= SCORE_SURE_WIN) {
                // SCORE_SURE_WIN is a MAX-winning leaf node.
                // if we definitely win on this node, we can skip the rest
                break;
            }
        }
        if (bestNode == null) {
            System.err.println("no move found. halting");
            while(true);
        }
        String move = getMoveFromDiff(currentBoard, bestNode);
        System.out.printf("got minimax move %s%n", move);
        return move;
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
        if (depth == 0 || isTerminal(node)) {
            return heuristic(node);
        }
        int value;
        if (maximizingPlayer) {
            value = Integer.MIN_VALUE;
            for (Field[][] child : getChildren(node, maximizingColor)) {
                value = Integer.max(value, minimax(child, depth - 1, alpha, beta, false));
                alpha = Integer.max(alpha, value);
                if (value >= beta) {
                    break;
                }
            }
            return value;
        } else {
            value = Integer.MAX_VALUE;
            for (Field[][] child : getChildren(node, minimizingColor)) {
                value = Integer.min(value, minimax(child, depth - 1, alpha, beta, true));
                beta = Integer.min(beta, value);
                if (value <= alpha) {
                    break;
                }
            }
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

    protected int heuristic(final Field[][] node) {
        // find rows of three
        Field potentialWinner = getWinner(node);
        if (potentialWinner == maximizingColor) {
            return SCORE_SURE_WIN; // best rating
        }
        if (potentialWinner == minimizingColor) {
            return -SCORE_SURE_WIN; // worst rating
        }
        // find rows of two
        int rating = 0; // MAX's runs of two - MIN's runs of 2
        //vertical
        //diagonal \
        //diagonal /
        for (int y = 0; y < boardHeight - 1; y++) {
            for (int x = 0; x < boardWidth; x++) {
                if (node[x][y] == Field.EMPTY) {
                    continue;
                }
                if (node[x][y] == node[x][y+1]) {
                    //found one: if max-run: add 1, else subtract 1
                    rating += node[x][y] == maximizingColor ? 1 : -1;
                }
                if (x+1 < boardWidth && node[x][y] == node[x+1][y+1]) {
                    rating += node[x][y] == maximizingColor ? 1 : -1;
                }
                if (x-1 > 0 && node[x][y] == node[x-1][y+1]) {
                    rating += node[x][y] == maximizingColor ? 1 : -1;
                }
            }
        }

        //horizontal
        for (int y = 0; y < boardHeight; y++) {
            for (int x = 0; x < boardWidth - 1; x++) {
                if (node[x][y] == Field.EMPTY) {
                    continue;
                }
                if (node[x][y] == node[x+1][y]) {
                    rating += node[x][y] == maximizingColor ? 1 : -1;
                }
            }
        }
        return rating;
    }
}
