package torgeros.connect3;

import java.sql.Struct;
import torgeros.connect3.Board.Field;
import torgeros.connect3.agent.Agent;
import torgeros.connect3.GameClient;

public class ConnectThree {
    public enum PlayerColor {
        WHITE_PLAYER (Field.WHITE, "white"),
        BLACK_PLAYER (Field.BLACK, "black");

        private final Field f;
        private final String clientName;
        private PlayerColor(Field f, String clientName) {
            this.f = f;
            this.clientName = clientName;
        }
        public Field getField() {
            return f;
        }
        public String getClientName() {
            return clientName;
        }
    }

    private Board board;
    private Agent agent;
    final private PlayerColor ownColor;
    final private PlayerColor opponentColor;
    private GameClient client;

    public ConnectThree(String gamename, PlayerColor color, Agent agent) {
        System.out.println("===========================================");
        System.out.printf("Starting game %s as color %s%n", gamename, color);
        System.out.printf("Game Representation: WHITE %c, BLACK %c%n", Field.WHITE.getChar(), Field.BLACK.getChar());
        board = new Board(5, 4);
        this.agent = agent;
        this.ownColor = color;
        this.opponentColor = color == PlayerColor.WHITE_PLAYER ? PlayerColor.BLACK_PLAYER : PlayerColor.WHITE_PLAYER;

        client = new GameClient(gamename, color.getClientName());
    }

    public void play() {
        setBoardStartPosition();
        board.print();

        client.connect();

        if (ownColor == PlayerColor.BLACK_PLAYER) {
            // if we start as black, we have to wait for one move.
            String move = client.getOpponentMove();
            // move is considered to be valid as it has passed through the server
            processMove(opponentColor, move);
            board.print();
        }

        while (true) {
            // update agent data
            agent.updateInternalBoard(board);
            // ask agent to create a move
            String move;
            boolean moveValid;
            do {
                move = agent.getBestMove();
                moveValid = processMove(ownColor, move);
            } while (!moveValid);
            // send the move to the client
            client.makeMove(move);
            // update the UI
            board.print();
            if (board.isTerminal()) {
                System.out.println("you win.");
                break;
            }
            // get opponents move
            System.out.println("waiting for opponents move");
            boolean opMoveCoorect = processMove(opponentColor, client.getOpponentMove());
            if (!opMoveCoorect) {
                System.err.println("Opponents move was incorrect.");
            }
            // update the UI
            board.print();
            if (board.isTerminal()) {
                System.out.println("opponent wins.");
                break;
            }
        }
    }

    private void setBoardStartPosition() {
        for (int y = 1; y <= board.height; y++) {
            for (int x = 1; x <= board.width; x++) {
                board.set(x, y, Field.EMPTY);
            }
        }
        board.set(1, 1, Field.WHITE);
        board.set(1, 3, Field.WHITE);
        board.set(5, 2, Field.WHITE);
        board.set(5, 4, Field.WHITE);
        board.set(1, 2, Field.BLACK);
        board.set(1, 4, Field.BLACK);
        board.set(5, 1, Field.BLACK);
        board.set(5, 3, Field.BLACK);
    }

    /**
     * If the return value is false, the board object has not been modified.
     * @param player WHITE_PLAYER or BLACK_PLAYER
     * @param cmd a move in the syntax "<x><y><dir>"
     * @return true if move was valid and has been executed
     */
    private boolean processMove(PlayerColor player, String cmd) {
        int x = cmd.charAt(0) - '0';
        int y = cmd.charAt(1) - '0';
        char direction = cmd.charAt(2);
        if (board.get(x, y) != player.getField()) {
            return false;
        }
        int targetX = x;
        int targetY = y;
        switch (direction) {
            case 'N':
                targetY--;
                break;
            case 'E':
                targetX++;
                break;
            case 'S':
                targetY++;
                break;
            case 'W':
                targetX--;
                break;
        }
        if (board.has(targetX, targetY)) {
            board.set(x, y, Field.EMPTY);
            board.set(targetX, targetY, player.getField());
            System.out.printf("🠯 %s: %s%n", player, cmd);
            return true;
        }
        return false;
    }
}
