package torgeros.connect3.agent;

import torgeros.connect3.agent.Ai;
import torgeros.connect3.ConnectThree.PlayerColor;
import torgeros.connect3.Board.Field;

public class EvaluatableAi extends Ai {

    int sumOfProcessingTime = 0;
    int numberOfMoves = 0; // number of own moves

    public EvaluatableAi(PlayerColor ownColor) {
        super(ownColor);
        System.out.printf("Started Evaluatable AI.%n");
    }

    @Override
    public String getBestMove() {
        final long startTimestamp = System.currentTimeMillis();
        String move = super.getBestMove();
        final long stopTimestamp = System.currentTimeMillis();
        numberOfMoves++;
        sumOfProcessingTime += stopTimestamp-startTimestamp;

        if (super.maximizingColor == Field.WHITE) {
            System.out.printf("move number %d.%n", numberOfMoves * 2 - 1);
        } else {
            System.out.printf("move number %d.%n", numberOfMoves * 2);
        }
        System.out.printf("average processing time for every move up until now was %d ms.%n", sumOfProcessingTime / numberOfMoves);
        return move;
    }
}
