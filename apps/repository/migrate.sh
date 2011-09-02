#!/bin/bash

ant scripts-compile

SUCCESS_STATUS="0"
EXIT_STATUS=$?

if [ $EXIT_STATUS -ne $SUCCESS_STATUS ]
then
    echo "Build failed. Exiting with status = $EXIT_STATUS."
    exit $EXIT_STATUS
fi

CLASSPATH="build/test/lib/scripts.jar"

BUILD_JARS="build/jars"
for i in `ls $BUILD_JARS`
do
    CLASSPATH=$CLASSPATH:$BUILD_JARS/$i
done

TEST_JARS="build/test/lib"
for i in `ls $TEST_JARS`
do
    CLASSPATH=$CLASSPATH:$TEST_JARS/$i
done


java -cp $CLASSPATH org.cnx.repository.scripts.programs.MigratorMain $*

