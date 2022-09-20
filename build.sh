#! /usr/bin/bash

sourcefiles=(
    src/torgeros/connect3/ConnectThree.java
    src/torgeros/connect3/Ternary.java
    src/torgeros/connect3/Board.java
    src/torgeros/connect3/GameClient.java
    src/torgeros/connect3/agent/Agent.java
    src/torgeros/connect3/agent/Human.java
    src/torgeros/connect3/agent/Ai.java
    src/torgeros/connect3/agent/EvaluatableAi.java
    src/torgeros/connect3/Util.java
)

javac -d . ${sourcefiles[*]} $@
