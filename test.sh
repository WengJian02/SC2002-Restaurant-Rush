#!/bin/sh
# compiles the game and the tests, then runs all the checks (the saved runs are replayed too)
cd "$(dirname "$0")" || exit 1
mkdir -p out
javac -d out -sourcepath src:test src/restaurant/Main.java test/restaurant/RulesTest.java || exit 1
java -cp out restaurant.RulesTest
