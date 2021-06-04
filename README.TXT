    PREAMBLE

Here is implementation of DataFlux QKB's matchcoding algorithm. In addition
to the algorithm itself the integration with the Oracle database included.

Matchcoding is the way to transform input strings into sort of "hashes". The
result can be then used to indirect comparison of different strings to find
matches among them.

This version of algorithm is designed primarily for Russian names and only 
for names encoded with Cyrillic character set.

Oracle integration makes it possible to use algorithm directly from database
via PL/SQL function call. Both regular and pipelined approaches available.

The algorithm itself and some parts of the Oracle database integration are 
written in Java, while the rest of the integration is written in PL/SQL.

The original algorithm is proprietary, implemented and to be executed in the
DataFlux Data Management Studio platform as part of so called Quality Knowledge
Base (QKB). The java implementation presented here was done from scratch and
by no means intended to violate any property, copyright or law.

    PROJECT STRUCTURE

The implementation (called later "project") is extremely minimalistic. It
consists of just source files (*.java, *.sql), followed by minimal set of
automation (*.bat) files to build them, to run tests or to load prepared java
stuff into the database. No IDE was used during development. All the work was
done in Far Manager "environment" (this is the file manager with in-built editor
and viewer; it has also the whole set of other tools, but they didn't used much
in this project though).

    DESCRIPTION STRUCTURE

Any meaningful folder in project directory structure contains README.TXT file
(including one you read just now). Each file provides minimal description to 
start with.

    DIRECTORY CONTENTS

build               folder where compiled and then jar-packed stuff finally goes
data                folder with essential data needed for the algorithm to work
test                folder with tests
compile.bat         to compile *.class files from *.java source
jarify.bat          to pack *.class and essential data stuff into *.jar files
load-ora.bat        to load *.jar files into Oracle database
MatchCoder.java     the main source file with matchcoding algo implementation
MatchCoderOra.java  the java-part of Oracle database integration
matchcoder-ora.sql  the script to create nesessary objects in Oracle database 

    HOW TO BIULD

    Prerequisites

1. JDK 8 Update 192
2. Oracle Database 12.2

ORACLE_HOME environment variable must be set to Oracle Database installation
location. Instead of Oracle Database you may use Oracle Database Client 
installation. But if so, then you need to copy the jar/CartridgeServices.jar 
file into %ORACLE_HOME%/rdbms/jlib folder first before running compile.bat.
This file got from Oracle Database installation.

    Instructions

1. Look into the data folder first and read all nesessary README.TXT files 
found in it, including subfolders. Some of these files may provide instructions
how to prepare essential algorithm data before further steps. Although some of
these data can be already used as-is with no need to be prepared in any way.

2. Launch compile.bat to compile java source. The execution must end up 
absolutely with no messages in the case of success.

3. Launch jarify.bat to pack all nesessary program and data files into set of
*.jar files. Then check script output for absence of errors.

As a result the following files will be created in the build folder:
MatchCoder.jar      the matchcoding algorithm implementation
MatchCoderOra.jar   the java-part of Oracle database integration

    HOW TO INTEGRATE INTO ORACLE DATABASE

1. Launch load-ora.bat to load *.jar files into Oracle database.
During execution you will be asked to enter password to connect to the database.
Look into load-ora.bat file to discover what schema and database targeted to load
the data into. You can change these settings before the bat file launch according
to your needs.

2. Connect to database and launch matchcoder-ora.sql script

    HOW TO USE

    In java code:

1. Include MatchCoder.jar into your CLASSPATH (via environment variable or via
java command line arguments). You may see the official Java documentation for
details about how to include external jars into your projects.

2. Use the following function in your code:

String MatchCoder.calcPriv(String fullName);
or:
String MatchCoder.calcOrg(String fullName);

Also you can find some usage examples in the test folder as well.

    With Oracle database:

See examples in matchcoder-ora-test.sql
