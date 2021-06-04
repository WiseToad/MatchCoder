    DIRECTORY CONTENTS

build               folder where compiled stuff finally goes
target              folder with prepared data used then by the algorithm
compile.bat         to compile *.class files from *.java source
run.bat             to start data preparation process
*.java              the source of the data preparation module

    HOW TO PREPARE DATA

The instructions below need to be done just once. If target folder already
contains nesessary data then there is no need to do anything.

1. Launch compile.bat to compile java source. The execution must end up 
absolutely with no messages in the case of success.

2. Launch run.bat file. The result is files named IRBICON Name.forest.txt
and IRBICON Organization.forest.txt in the target folder.
