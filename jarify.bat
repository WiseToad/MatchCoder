@echo off

cd build

jar cMvf MatchCoder.jar MatchCoder.class MatchCoder$*.class
jar uMvf MatchCoder.jar -C ../data/forest/target .
jar uMvf MatchCoder.jar -C ../data/phonetx .
jar uMvf MatchCoder.jar -C ../data/regexlib .
jar uMvf MatchCoder.jar -C ../data/scheme .
jar uMvf MatchCoder.jar -C ../data/vocab/target .

jar cMvf MatchCoderOra.jar MatchCoderPrivOra.class MatchCoderPrivOra$*.class MatchCoderOrgOra.class MatchCoderOrgOra$*.class
