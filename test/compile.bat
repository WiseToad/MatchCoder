@echo off

setlocal
set CLASSPATH=../build/MatchCoder.jar
set SRC=Test.java TestBatch.java

set FLAGS=-Xlint:deprecation -Xlint:unchecked -encoding utf8

if not exist build md build

javac -cp %CLASSPATH% %FLAGS% -d build %SRC%
