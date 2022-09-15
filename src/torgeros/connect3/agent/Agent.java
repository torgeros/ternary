package torgeros.connect3.agent;

import torgeros.connect3.Board;

public interface Agent {
    public void updateInternalBoard(Board board);
    public String getBestMove();
}
