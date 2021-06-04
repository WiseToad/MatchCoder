@echo off

setlocal
set CLASSPATH=build;../build/MatchCoder.jar
set CLASS=TestBatch

set LOG=run-batch.log

java -cp %CLASSPATH% %CLASS% >%LOG% 2>&1
