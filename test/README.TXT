    DIRECTORY CONTENTS

build               folder where compiled stuff finally goes
data                folder with data for batch test
compile.bat         to compile *.class files from *.java source
run.bat             to launch the trivial test
run-batch.bat       to launch the batch test 
Test.java           the source of trivial test
TestBatch.java      the source of batch test

    HOW TO BUILD

1. Compile and "jarify" the main source and data files in the parent directory
first (see README.TXT file there for instructions).

2. Launch compile.bat to compile java source. The execution must end up 
absolutely with no messages in the case of success.

    HOW TO RUN TESTS

Launch nesessary run*.bat file(s).

    The batch test

It can take approximately 30 minutes or so for 10 M records, depending on
processing power of your CPU. Resulting output will be redirected into the
run-batch.log file.
