#!/usr/bin/env bash

# Add appropriate files for encryption

rm frost.tar.enc
cd ..
tar cvf frost.tar files/gplay-keys.json files/play.keystore files/play.properties files/test.keystore app/fabric.properties
travis encrypt-file frost.tar --add
rm frost.tar
mv frost.tar.enc files/