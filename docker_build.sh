#!/usr/bin/env bash

printf "Starting docker build\n"

npm -v
./gradlew -v

./gradlew --quiet androidGitVersion
./gradlew lintReleaseTest testReleaseUnitTest assembleReleaseTest