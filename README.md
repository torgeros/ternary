# Ternary

## Run Instructions

First, compile the program by running 

```bash
./build.sh
```

Then, start the program by running

```bash
java torgeros.connect3.Ternary <gamename> <b|w> <human|ai|eval>
```

The `eval` mode returns stats after each move. In the backend, `eval` itself starts an `ai` agent.

As a `human` agent you must not enter an inavlid move, they are only checked for syntax, not for correctness!

In `human` mode moves can be entered as `"23e"` or `"23E"`.
