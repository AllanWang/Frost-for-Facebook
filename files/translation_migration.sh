#!/usr/bin/env bash

MODULE=Frost-for-Facebook

cd ..

current=${PWD##*/}

if [ "$current" != "$MODULE" ]; then
    echo "Not in $MODULE";
    return;
fi

# DANGEROUS! Removes all files matching regex

egrep -lir --include="*.xml" "<resources.*></resources>" "./" | tr '\n' '\0' | xargs -0 -n1 rm

# Delete empty directories

find . -type d -empty -delete