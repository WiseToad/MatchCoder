@echo off

setlocal
set NAME=IRBICON Name

if not exist target md target

stage2.py "temp\%NAME%.stage1.edited.txt" >"target\%NAME%.vcb.txt"
