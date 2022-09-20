package torgeros.connect3;

import torgeros.connect3.ConnectThree.PlayerColor;
import torgeros.connect3.agent.Agent;
import torgeros.connect3.agent.Human;
import torgeros.connect3.agent.Ai;
import torgeros.connect3.agent.EvaluatableAi;

public class Ternary {
    public static void main(String[] args) {
        if (args.length != 3) {
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
        Agent agent;
        if (args[2].equals("human")) {
            agent = new Human();
        } else if (args[2].equals("ai")) {
            agent = new Ai(agentcolor);
        } else if (args[2].startsWith("eval")) {
            agent = new EvaluatableAi(agentcolor,
                    Integer.parseInt(args[2].substring(4)));
        } else {
            exitWrongArgs();
            return;
        }
        (new ConnectThree(gamename, agentcolor, agent)).play();
        System.out.println();
    }

    private static void exitWrongArgs() {
        System.out.println("Command line arguments are not valid.");
        System.out.println("Start the program with \"Ternary <gamename> <b|w> <human|ai|eval<DEPTH>>\"");
        System.exit(1);
    }
}
