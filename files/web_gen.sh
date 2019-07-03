#!/usr/bin/env bash

echo "Starting Frost Web Gen"

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

cd "$DIR/../app/src/web"

if [[ ! -f "package.json" ]]; then
    echo "package.json not found at $(pwd)"
    exit 1
fi

echo "Installing dependencies at $(pwd)"

npm install

echo "Compiling web content"

npm run compile

echo "Finished; checking content"

if [[ -z "$(ls -A assets/css)" ]]; then
    echo "Could not find css content"
    exit 1
fi

if [[ -z "$(ls -A assets/js)" ]]; then
    echo "Could not find js content"
    exit 1
fi

echo "Done!"