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
    VERSION="$(date +%Y%m%d)GIT$(git show HEAD --abbrev-commit| head -1 | cut -d" " -f2)"
    DATEANDTIME="$(date +%Y%m%d%H%M%S)"
else
    VERSION="$(date +%Y%m%d)GIT$(git show HEAD --abbrev-commit| head -1 | cut -d" " -f2)"
    DATEANDTIME="$(date +%Y%m%d%H%M%S)"
fi

SERVER="jenna.fnf.hu"
RDIR="builds"
for MAIN in $(find src/main/java/hu/fnf/devel/forex/ -name *.java)
do
    sed -i "s/VERSION/$VERSION/g" $MAIN
    sed -i "s/DATEANDTIME/$DATEANDTIME/g" $MAIN
done
mvn  assembly:assembly -P Main
for MAIN in $(find src/main/java/hu/fnf/devel/forex/ -name *.java)
do
    sed -i "s/$VERSION/VERSION/g" $MAIN
    sed -i "s/$DATEANDTIME/DATEANDTIME/g" $MAIN
done

if [ "$CMT" != "no" ]
then
    git push
fi

if [ "$CPY" != "no" ]
then
    scp target/Main.jar $SERVER:./$RDIR/$DATEANDTIME.jar
    if [ "$RUN" != "no" ]
    then
        ssh $SERVER 'java -jar $(ls -t builds/*.jar | head -1) log4j.properties &'
    fi
fi
if [ "$RUN" != "no" ]
then
    java -jar /home/johnnym/git/forex-strategy/target/Main.jar res/log4j.properties  
fi
