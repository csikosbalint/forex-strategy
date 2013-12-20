#/bin/bash
read -p "Commit changes? [yes/NO]" ANS
if [ "$ANS" != "" ]
then
    read -p "Change log: " CHANGE
    git commit -a -m "$CHANGE"
    TIME="$(date +%Y%m%d)GIT$(git show HEAD --abbrev-commit| head -1 | cut -d' '-f2)"
else
    TIME="$(date +%Y%m%d%H%M%S)"
fi

if

MAIN="src/main/java/hu/fnf/devel/forex/Main.java"
SERVER="jenna.fnf.hu"
RDIR="builds"

sed -i "s/DATE/$TIME/g" $MAIN
mvn  assembly:assembly -P Main
sed -i "s/$TIME/DATE/g" $MAIN
read -p "Copy to $SERVER [YES/no]" ANS
if [ "$ANS" == "" ]
then
    scp target/Main.jar $SERVER:./$RDIR/$TIME.jar
fi
