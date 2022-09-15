package torgeros.connect3.agent;

import torgeros.connect3.Board;
import java.util.Scanner;
import java.util.regex.Pattern;

public class Human implements Agent {
    final Scanner scanner;
    Board currentBoard;

    public Human() {
        scanner = new Scanner(System.in);
    }

    public void updateInternalBoard(final Board board) {
        currentBoard = board;
    }

    // TODO if this returns an invalid move, the program behaves unexpectedly and probably crashes
    public String getBestMove() {
        String move;
        Pattern pattern = Pattern.compile(String.format(
                "^[1-%d][1-%d][NSEW]$", currentBoard.width, currentBoard.height
                ));
        do {
            System.out.print("put in your best move (<x><y><direction>): ");
            move = scanner.nextLine();
        } while (!pattern.matcher(move).find());
        return move;
    }
}
