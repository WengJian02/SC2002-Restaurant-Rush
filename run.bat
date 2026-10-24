@echo off
rem compiles the game into the out folder and starts it (extra arguments such as --echo go to the game)
cd /d "%~dp0"
if not exist out mkdir out
javac -d out -sourcepath src src\restaurant\Main.java
if errorlevel 1 exit /b 1
java -cp out restaurant.Main %*
