#!/usr/bin/env bash

printf "Starting script\n"
chmod +x gradlew
./gradlew --quiet androidGitVersion
./gradlew lintReleaseTest testReleaseUnitTest assembleReleaseTest