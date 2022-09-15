#! /usr/bin/bash

sourcefiles=(
    src/torgeros/connect3/ConnectThree.java
    src/torgeros/connect3/Ternary.java
    src/torgeros/connect3/Board.java
)

javac -d . ${sourcefiles[*]}
