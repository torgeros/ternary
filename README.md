# Ternary

## Run Instructions

First, compile the program by running 

```bash
./build.sh
```

Then, start the program by running

```bash
java torgeros.connect3.Ternary <gamename> <b|w> <human|ai|eval> [large-grid]
```

`<b|w>` selects playing WHITE or BLACK.

The `eval` mode returns stats after each move. In the backend, `eval` itself starts an `ai` agent.

As a `human` agent you must not enter an invalid move, they are only checked for syntax, not for correctness!

In `human` mode moves can be entered as `"23e"` or `"23E"`.

The optional `large-grid`-flag enableds the 7x6 board (default 4x5).

## Code Modularity, Classes

Sorted by filename in `src/torgeros/connect3`. The main game AI logic is in `src/torgeros/connect3/agent/Ai.java`.

### agent/Agent.java

Common Interface for Human and AI players.

### agent/Human.java

Agent that asks for user input for each move, so one can play manually with organized game state output. This is easier than using `netcat`, because the game state (board) is visualized before and after each move.

### agent/Ai.java

Contains the complete game playing agent-logic

### agent/EvaluatableAi.java

Wrapper around the Ai class that adds some extra evaluative (one might say debug) println statements.

### Board.java

Represents a game state, mostly used for visualization.

Contains an enum `Field`, that itself holds the three values a field can take and the chars used for visualization.

```java
enum Field {
    WHITE ('•'),
    BLACK ('◦'),
    EMPTY (' ');
}
```

`Board` can be seen as a wrapper around a `Field[][]` with some utilitary functions like printing and accessing by the server index that starts at 1,1.

### ConnectThree.java

Represents the game itself, holding an instance of one Board, one Agent and one GameClient

Contains an enum `PlayerColor`, used to make the code easier to read. Every PlayerColor holds the representation the server communication reserves for that player and the associated `Field` color.

```java
enum PlayerColor {
    WHITE_PLAYER (Field.WHITE, "white"),
    BLACK_PLAYER (Field.BLACK, "black");
}
```

### GameClient

Handles communication with the game server. Checks for successful transmission of each command and halts the game in unexpected states (e.g. server timeouts)

### Ternary.java

Main class. Handles command line arguments and initiates the game.

### util.java

Contains some auxiliary code, more specifically a function that creates a deep-copy of a `Field[][]` and one for printing a `Field[][]` with borders around it.

## License

You are obviously not allowed to use this code while you are taking McGill's ECSE 526 (AI).

For every other use case, the GPLv3 applies.
