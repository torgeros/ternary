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
            agentcolor = PlayerColor.WHITE;
        } else if (args[1].equals("b")) {
            agentcolor = PlayerColor.BLACK;
        } else {
            exitWrongArgs();
            return;
        }
        System.out.println("===========================================");
        System.out.printf("Starting game %s as color %s%n", gamename, agentcolor);
    }

    private static void exitWrongArgs() {
        System.out.println("Command line arguments are not valid.");
        System.out.println("Start the program with \"Ternary <gamename> <b|w>\"");
        System.exit(1);
    }
}
