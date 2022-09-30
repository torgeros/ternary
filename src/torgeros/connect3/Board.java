package torgeros.connect3;

import torgeros.connect3.Util;

public class Board {
    public enum Field {
        WHITE ('•'),
        BLACK ('◦'),
        EMPTY (' ');

        private final char c;
        private Field(final char c) {
            this.c = c;
        }
        public char getChar() {
            return c;
        }
    }

    /**
     * internal state representation
     */
    private Field[][] board;
    public final int width;
    public final int height;

    public Board(int width, int height) {
        this.width = width;
        this.height = height;
        board = new Field[width][height];
    }

    public Field get(int x, int y) {
        return board[x-1][y-1];
    }

    public Field getFromInfitite(int x, int y) {
        if (has(x, y)) {
            return get(x, y);
        }
        return Field.EMPTY;
    }

    public Field[][] getCopyOfBoard() {
        return board.clone();
    }

    public void set(int x, int y, Field value) {
        board[x-1][y-1] = value;
    }

    public boolean has(int x, int y) {
        return (x>0) && (y>0) && (x<=width) && (y<=height);
    }

    public boolean isTerminal() {
        //vertical
        //diagonal \
        //diagonal /
        for (int y = 1; y <= height - 2; y++) {
            for (int x = 1; x <= width; x++) {
                Field f = get(x, y);
                if (f == Field.EMPTY) {
                    continue;
                }
                if (f == get(x, y+1) && f == get(x, y+2)) {
                    return true;
                }
                if (f == getFromInfitite(x+1, y+1) && f == getFromInfitite(x+2, y+2)) {
                    return true;
                }
                if (f == getFromInfitite(x-1, y+1) && f == getFromInfitite(x-2, y+2)) {
                    return true;
                }
            }
        }

        //horizontal
        for (int y = 1; y <= height; y++) {
            for (int x = 1; x <= width - 2; x++) {
                Field f = get(x, y);
                if (f == Field.EMPTY) {
                    continue;
                }
                if (f == get(x+1, y) && f == get(x+2, y)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * prints the current game state using Util.printFieldArray(Field[][])
     */
    public void print() {
        Util.printFieldArray(board);
    }
}
