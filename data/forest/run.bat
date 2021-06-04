@echo off

setlocal
set CLASSPATH=build
set CLASS=App

if not exist target md target

java -cp %CLASSPATH% %CLASS%
