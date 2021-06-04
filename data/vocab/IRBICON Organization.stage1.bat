@echo off

setlocal
set NAME=IRBICON Organization

if not exist temp md temp

stage1.py "source\%NAME%.vcb.qkv" >"temp\%NAME%.stage1.txt"
