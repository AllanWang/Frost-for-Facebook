#!/usr/bin/env bash

git checkout dev
git cmp hard rebase
git branch -f dev-bak dev
git checkout master
git pull -f origin master
git branch -f dev master
git checkout dev
git push -u -f origin dev
