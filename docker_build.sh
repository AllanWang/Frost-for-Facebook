#!/usr/bin/env bash

printf "Starting script\n"
chmod +x gradlew

nodejs -v
echo $ANDROID_HOME
echo "Hello"
./gradlew -v

#./gradlew --quiet androidGitVersion
#./gradlew lintReleaseTest testReleaseUnitTest assembleReleaseTest