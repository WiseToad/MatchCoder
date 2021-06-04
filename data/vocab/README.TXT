    DIRECTORY CONTENTS

source              folder with QKB Vocabulary source data
target              folder with converted data used then by the algorithm
temp                folder with intermediate data used by conversion process
*.bat               to start nesessary stage of conversion process (see below)
*.py                the Python source launched by corresponding *.bat file
    
    HOW TO PREPARE DATA

    Prerequisites

1. Python 2.7

    Instructions

The instructions below need to be done just once if QKB source files has not been 
changed. If every *.vcb.qkv file within archive in source folder has corresponding
up-to-date *.vcb.txt file in target folder then there is no need to do anything.

1. Unpack archive in source folder.

2. Launch appropriate *.stage1.bat file. It will take corresponding QKB data
file named *.vcb.qkv from the source folder and then convert it to intermediate
file named *.stage1.txt that will be created in temp folder.

3. Copy obtained *.stage1.txt file found in temp folder to *.stage1.edited.txt. 
Then edit the latter to manually remove header and footer that do not follow
overall data format within the file.

4. Launch *.stage1.bat file. The result is file named *.vcb.txt in the target folder.
