#!/usr/bin/env bash

cd ..

current=${PWD##*/}

if [ "$current" != "Frost-for-Facebook" ]; then
    echo "Not in Frost";
    return;
fi

declare -a folders=("es" "fr" "de")

cd app/src/main/res

for d in $(find ./values-* -type d) ; do
	key=$(echo $d | cut -d '-' -f2)
	
	if ! [[ ${folders[*]} =~ "$key" ]]; then
		rm -r "$d"
	else
		mv "$d" "./values-$key"
	fi
done