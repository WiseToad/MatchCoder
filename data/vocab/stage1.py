import sys

if len(sys.argv) < 2:
  print("Please specify input filename")
  exit(1)

token = ""
with open(sys.argv[1], "rb") as f:
  byte = f.read(1)
  while byte:
    if byte == " ":
      if token != "":
        print(token)
      token = ""
    else:
      token = token + byte
    byte = f.read(1)
if token != "":
  print(token)
