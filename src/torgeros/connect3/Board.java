package torgeros.connect3;

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

    private Field[][] board;
    public final int width;
    public final int height;

    private String formatTableTop;
    private String formatTableBottom;

    public Board(int width, int height) {
        this.width = width;
        this.height = height;
        board = new Field[width][height];

        // compile format strings for pretty print()
        formatTableTop = "┌";
        formatTableBottom = "└";
        for (int x = 0; x < width * 2 + 1; x++) {
            formatTableTop += "-";
            formatTableBottom += "-";
        }
        formatTableTop += "┐";
        formatTableBottom += "┘";
    }

    public Field get(int x, int y) {
        return board[x-1][y-1];
    }

    public void set(int x, int y, Field value) {
        board[x-1][y-1] = value;
    }

    public boolean has(int x, int y) {
        return (x>0) && (y>0) && (x<=width) && (y<=height);
    }

    public void print() {
        String result = String.format("%s%n", formatTableTop);
        for (int y = 0; y < height; y++) {
            String line = "|";
            for (int x = 0; x < width; x++) {
                line += " " + board[x][y].getChar();
            }
            result += String.format("%s |%n", line);
        }
        result += formatTableBottom;
        System.out.println(result);
    }
}
