@echo off
rem compiles the game and the tests, then runs all the checks (the saved runs are replayed too)
cd /d "%~dp0"
if not exist out mkdir out
javac -d out -sourcepath src;test src\restaurant\Main.java test\restaurant\RulesTest.java
if errorlevel 1 exit /b 1
java -cp out restaurant.RulesTest
