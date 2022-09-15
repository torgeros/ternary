package torgeros.connect3;

import torgeros.connect3.ConnectThree.PlayerColor;

public class Ternary {
    public static void main(String[] args) {
        if (args.length != 2) {
            exitWrongArgs();
        }
        String gamename = args[0];
        PlayerColor agentcolor;
        if (args[1].equals("w")) {
            agentcolor = PlayerColor.WHITE_PLAYER;
        } else if (args[1].equals("b")) {
            agentcolor = PlayerColor.BLACK_PLAYER;
        } else {
            exitWrongArgs();
            return;
        }
        (new ConnectThree(gamename, agentcolor)).play();
        System.out.println();
    }

    private static void exitWrongArgs() {
        System.out.println("Command line arguments are not valid.");
        System.out.println("Start the program with \"Ternary <gamename> <b|w>\"");
        System.exit(1);
    }
}
