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
    private GameClient client;

    public ConnectThree(String gamename, PlayerColor color, Agent agent) {
        System.out.println("===========================================");
        System.out.printf("Starting game %s as color %s%n", gamename, color);
        System.out.printf("Game Representation: WHITE %c, BLACK %c%n", Field.WHITE.getChar(), Field.BLACK.getChar());
        board = new Board(5, 4);
        this.agent = agent;
        this.ownColor = color;

        client = new GameClient(gamename, color.getClientName());
    }

    public void play() {
        setBoardStartPosition();
        board.print();
        processMove(PlayerColor.BLACK_PLAYER, "14E");
        board.print();

        client.connect();

        while (!board.isTerminal()) {
            agent.updateInternalBoard(board);
            processMove(ownColor, agent.getBestMove());
            board.print();
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
