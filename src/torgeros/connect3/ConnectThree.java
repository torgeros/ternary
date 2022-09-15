package torgeros.connect3;

import java.sql.Struct;

public class ConnectThree {
    public enum PlayerColor {
        WHITE_PLAYER,
        BLACK_PLAYER
    }

    private Board board;

    public ConnectThree(String gamename, PlayerColor color) {
        System.out.println("===========================================");
        System.out.printf("Starting game %s as color %s%n", gamename, color);
        System.out.printf("Game Representation: WHITE %c, BLACK %c%n", Board.Field.WHITE.getChar(), Board.Field.BLACK.getChar());
        board = new Board(5, 4);
    }

    public void play() {
        setBoardStartPosition();
        board.print();
    }

    private void setBoardStartPosition() {
        for (int y = 1; y <= board.height; y++) {
            for (int x = 1; x <= board.width; x++) {
                board.set(x, y, Board.Field.EMPTY);
            }
        }
        board.set(1, 1, Board.Field.WHITE);
        board.set(1, 3, Board.Field.WHITE);
        board.set(5, 2, Board.Field.WHITE);
        board.set(5, 4, Board.Field.WHITE);
        board.set(1, 2, Board.Field.BLACK);
        board.set(1, 4, Board.Field.BLACK);
        board.set(5, 1, Board.Field.BLACK);
        board.set(5, 3, Board.Field.BLACK);
    }
}
