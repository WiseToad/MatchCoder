@echo off

setlocal
set NAME=IRBICON Organization

if not exist target md target

stage2.py "temp\%NAME%.stage1.edited.txt" >"target\%NAME%.vcb.txt"
