#/bin/bash
TIME="$(date +%Y%m%d%H%M%S)"
MAIN="src/main/java/hu/fnf/devel/forex/Main.java"
SERVER="jenna.fnf.hu"

sed -i "s/TIMEE/$TIME/g" $MAIN
mvn  assembly:assembly -P Main
sed -i "s/$TIME/TIMEE/g" $MAIN
scp target/Main.jar $SERVER:./$TIME.jar
