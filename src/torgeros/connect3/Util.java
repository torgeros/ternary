package torgeros.connect3;

import torgeros.connect3.Board.Field;

public class Util {
    /**
     * create a deep copy of an Field[][].
     */
    public static Field[][] copyFieldArray(Field[][] original) {
        Field[][] copy = new Field[original.length][];
        for (int i = 0; i < original.length; i++) {
            copy[i] = original[i].clone();
        }
        return copy;
    }

    /**
     * print a Field[][] to System.out, with a border around it.
     */
    public static void printFieldArray(Field[][] board) {
        String formatTableTop;
        String formatTableBottom;

        // compile format strings for pretty print()
        formatTableTop = "┌";
        formatTableBottom = "└";
        for (int x = 0; x < board.length * 2 + 1; x++) {
            formatTableTop += "-";
            formatTableBottom += "-";
        }
        formatTableTop += "┐";
        formatTableBottom += "┘";
        String result = String.format("%s%n", formatTableTop);
        for (int y = 0; y < board[0].length; y++) {
            String line = "|";
            for (int x = 0; x < board.length; x++) {
                if (board[x][y] != null) {
                    line += " " + board[x][y].getChar();
                } else {
                    line += " 0";
                }
            }
            result += String.format("%s |%n", line);
        }
        result += formatTableBottom;
        System.out.println(result);
    }
}
