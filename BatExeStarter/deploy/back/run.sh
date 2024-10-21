#!/bin/sh
cd /default;
java -Xms1G -Xmx2G -Dfile.encoding=UTF-8 -Djline.terminal=jline.UnsupportedTerminal -jar zakaz-0.0.1-SNAPSHOT.jar;