package torgeros.connect3.agent;

import torgeros.connect3.agent.Ai;
import torgeros.connect3.ConnectThree.PlayerColor;
import torgeros.connect3.Board.Field;

public class EvaluatableAi extends Ai {

    int sumOfProcessingTime = 0;
    int numberOfMoves = 0; // number of own moves

    public EvaluatableAi(PlayerColor ownColor, int searchDepth) {
        super(ownColor);
        super.MAX_SEARCH_DEPTH = searchDepth;
        System.out.printf("Started Evaluatable AI with search depth %d.%n", searchDepth);
    }

    @Override
    public String getBestMove() {
        final long startTimestamp = System.currentTimeMillis();
        String move = super.getBestMove();
        final long stopTimestamp = System.currentTimeMillis();
        numberOfMoves++;
        sumOfProcessingTime += stopTimestamp-startTimestamp;

        System.out.printf("average processing time foe every move up until now was %d ms.%n", sumOfProcessingTime / numberOfMoves);
        return move;
    }
}