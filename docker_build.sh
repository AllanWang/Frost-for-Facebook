#!/usr/bin/env bash

export ANDROID_HOME="/opt/android-sdk-linux"

printf "Starting script\n"
chmod +x gradlew
./gradlew --quiet androidGitVersion
./gradlew lintReleaseTest testReleaseUnitTest assembleReleaseTest