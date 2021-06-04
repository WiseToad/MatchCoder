@echo off

setlocal
set CLASSPATH=
set SRC=App.java Tokenizer.java

set FLAGS=-Xlint:deprecation -Xlint:unchecked

if not exist build md build

javac -cp %CLASSPATH% %FLAGS% -d build %SRC%
