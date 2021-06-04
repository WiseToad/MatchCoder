@echo off

setlocal
set CLASSPATH=build;../build/MatchCoder.jar
set CLASS=Test

java -cp %CLASSPATH% %CLASS%
