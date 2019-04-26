#!/usr/bin/env bash

printf "Starting script\n"
chmod +x gradlew

npm -v
echo $ANDROID_HOME
echo "Hello world"
./gradlew -v

#./gradlew --quiet androidGitVersion
#./gradlew lintReleaseTest testReleaseUnitTest assembleReleaseTest