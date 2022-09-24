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
