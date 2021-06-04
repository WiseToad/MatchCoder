import sys
import base64

if len(sys.argv) < 2:
  print("Please specify input filename")
  exit(1)

with open(sys.argv[1]) as f:
  for line in f:
    token1 = line[:8]
    line = line[8:]
    pos = line.find("\0")
    if pos >= 0:
      token2 = line[:pos]
      token3 = line[pos+1:].rstrip("\r\n\0")
    else:
      token2 = line.rstrip("\r\n\0")
      token3 = ""
    print(base64.b64decode(token2) + "\t" +
      base64.b64decode(token3) + "\t" + 
      str(int(token1, 16)))
