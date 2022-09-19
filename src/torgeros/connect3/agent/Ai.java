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

    final Field maximizingColor;
    final Field minimizingColor;

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

    public String getBestMove() {
        minimax(currentBoard, 2, true);
        return "";
    }

    /**
     * https://en.wikipedia.org/wiki/Minimax#Pseudocode
     */
    private int minimax(Field[][] node, int depth, boolean maximizingPlayer) {
        if (depth == 0 || isTerminal(node)) {
            return heuristic(node);
        }
        int value;
        if (maximizingPlayer) {
            value = Integer.MIN_VALUE;
            for (Field[][] child : getChildren(node, maximizingColor)) {
                value = Integer.max(value, minimax(child, depth - 1, false));
            }
            return value;
        } else {
            value = Integer.MAX_VALUE;
            for (Field[][] child : getChildren(node, minimizingColor)) {
                value = Integer.min(value, minimax(child, depth - 1, true));
            }
            return value;
        }
    }

    private int heuristic(Field[][] node) {
        return 0;
    }

    private ArrayList<Field[][]> getChildren(final Field[][] node, final Field movableColor) {
        ArrayList<Field[][]> list = new ArrayList<Field[][]>();
        System.out.printf("started getChildren for %s%n", movableColor.toString());
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
                    System.out.printf("added move W for %d %d that is %s%n", x, y, node[x][y].toString());
                    list.add(child);
                }
                //EAST
                if (x + 1 < boardWidth && node[x+1][y] == Field.EMPTY) {
                    Field[][] child = Util.copyFieldArray(node);
                    child[x][y] = Field.EMPTY;
                    child[x+1][y] = movableColor;
                    System.out.printf("added move E for %d %d that is %s%n", x, y, node[x][y].toString());
                    list.add(child);
                }
                //NORTH
                if (y > 0 && node[x][y-1] == Field.EMPTY) {
                    Field[][] child = Util.copyFieldArray(node);
                    child[x][y] = Field.EMPTY;
                    child[x][y-1] = movableColor;
                    System.out.printf("added move N for %d %d that is %s%n", x, y, node[x][y].toString());
                    list.add(child);
                }
                //SOUTH
                if (y + 1 < boardHeight && node[x][y+1] == Field.EMPTY) {
                    Field[][] child = Util.copyFieldArray(node);
                    child[x][y] = Field.EMPTY;
                    child[x][y+1] = movableColor;
                    System.out.printf("added move S for %d %d that is %s%n", x, y, node[x][y].toString());
                    list.add(child);
                }
            }
        }
        System.out.printf("generated list of possible moves: %d entries%n", list.size());
        return list;
    }

    private boolean isTerminal(Field[][] node) {
        //vertical
        //diagonal \
        //diagonal /
        for (int y = 0; y < boardHeight - 2; y++) {
            for (int x = 0; x < boardWidth; x++) {
                if (node[x][y] == Field.EMPTY) {
                    continue;
                }
                if (node[x][y] == node[x][y+1] && node[x][y] == node[x][y+2]) {
                    return true;
                }
                if (x+2 < boardWidth
                        // x+2 < width means index x+2 is within the array
                        && node[x][y] == node[x+1][y+1]
                        && node[x][y] == node[x+2][y+2]) {
                    return true;
                }
                if (x-2 > 0
                        // x-2 > 0 means index x-2 is within the array
                        && node[x][y] == node[x-1][y+1]
                        && node[x][y] == node[x-2][y+2]) {
                    return true;
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
                    return true;
                }
            }
        }
        return false;
    }
}
