#!/usr/bin/env bash

printf "Starting script\n"

npm -v
./gradlew -v

npm install --prefix app/src/web
npm run --prefix app/src/web compile

./gradlew --quiet androidGitVersion
./gradlew lintReleaseTest testReleaseUnitTest assembleReleaseTest