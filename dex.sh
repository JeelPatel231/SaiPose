#!/bin/bash

PACKAGE_NAME="tel.jeelpa.saipose"

INPUT=parser/build/libs/parser.jar

OUTPUT=parser/build/libs/parserdex.jar

CACHE_PUSH_DIR=/data/data/$PACKAGE_NAME/cache/parserdex.jar
#EXTERNAL_CACHE_PUSH_DIR=/storage/emulated/0/Android/data/$PACKAGE_NAME/cache/parserdex.jar
#EXTERNAL_CACHE_PUSH_DIR=/sdcard/Android/data/$PACKAGE_NAME/cache/parserdex.jar
EXTERNAL_CACHE_PUSH_DIR=/sdcard/parserdex.jar

./gradlew :parser:shadowJar
"$ANDROID_HOME"/build-tools/$(ls "$ANDROID_HOME"/build-tools | tail -n1)/d8 --classpath reference/build/libs/reference.jar $INPUT --release --output $OUTPUT
adb push $OUTPUT /data/local/tmp
adb shell run-as $PACKAGE_NAME cp /data/local/tmp/parserdex.jar $CACHE_PUSH_DIR

#adb shell cp /data/local/tmp/parserdex.jar $EXTERNAL_CACHE_PUSH_DIR
adb push $OUTPUT $EXTERNAL_CACHE_PUSH_DIR
