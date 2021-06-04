@echo off

if not [%ORACLE_HOME%] == [] goto START

echo ERROR! Environment variable ORACLE_HOME doesn't specified
exit 1

:START

setlocal
set CLASSPATH=%ORACLE_HOME%/jdbc/lib/*;%ORACLE_HOME%/rdbms/jlib/*
set SRC=MatchCoder.java MatchCoderPrivOra.java MatchCoderOrgOra.java

set FLAGS=-Xlint:deprecation -Xlint:unchecked -encoding utf8

if not exist build md build

javac -cp %CLASSPATH% %FLAGS% -d build %SRC%
