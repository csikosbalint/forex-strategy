#/bin/bash
while [ $# -gt 0 ]
do
    case $1 in
        --run)
            RUN=$2
            shift 2
        ;;
        --copy)
            CPY=$2
            shift 2
        ;;
        --commit)
            CMT=$2
            shift 2
        ;;
        *)
        ;;
    esac
done

if [ "$RUN" == "" ]
then
    read -p "Run after compile? [yes/NO]: " RUN
fi
if [ "$CPY" == "" ]
then
    read -p "Copy to server?    [yes/NO]: " CPY
fi
if [ "$CMT" == "" ]
then
    read -p "Commit to git?     [yes/NO]: " CMT
fi

RUN=${RUN:-no}
CPY=${CPY:-no}
CMT=${CMT:-no}

if [ "$CMT" != "no" ]
then
    read -p "Change log: " CHANGE
    git commit -a -m "$CHANGE"
    TIME="$(date +%Y%m%d)GIT$(git show HEAD --abbrev-commit| head -1 | cut -d" " -f2)"
else
    TIME="$(date +%Y%m%d%H%M%S)"
fi

MAIN="src/main/java/hu/fnf/devel/forex/Main.java"
SERVER="jenna.fnf.hu"
RDIR="builds"

sed -i "s/DATE/$TIME/g" $MAIN
mvn  assembly:assembly -P Main
sed -i "s/$TIME/DATE/g" $MAIN
if [ "$CPY" != "no" ]
then
    scp target/Main.jar $SERVER:./$RDIR/$TIME.jar
    if [ "$RUN" != "no" ]
    then
        ssh $SERVER 'java -jar $(ls -t builds/*.jar | head -1) log4j.properties &'
    fi
fi
if [ "$RUN" != "no" ]
then
    java -jar /home/johnnym/git/forex-strategy/target/Main.jar res/log4j.properties  
fi
