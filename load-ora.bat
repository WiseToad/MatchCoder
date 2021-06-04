@echo off

setlocal
set JAR=MatchCoder.jar MatchCoderOra.jar
set USER=user@db

cd build

loadjava -user %USER% -verbose -resolve %JAR%
