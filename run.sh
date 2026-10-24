#!/bin/sh
# compiles the game into ./out and starts it (extra arguments such as --echo go to the game)
cd "$(dirname "$0")" || exit 1
mkdir -p out
javac -d out -sourcepath src src/restaurant/Main.java || exit 1
java -cp out restaurant.Main "$@"
