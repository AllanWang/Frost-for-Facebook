#!/usr/bin/env bash

git checkout dev
git branch -d dev-bak
git checkout -b dev-bak
git checkout master
git pull -f origin master
git branch -d dev
git checkout -b dev
git push --set-upstream origin dev