#!/usr/bin/env bash

# Add appropriate files for encryption

rm frost.tar.enc
cd ..
tar cvf frost.tar files/release.keystore files/release.properties files/test.keystore files/test.properties
travis encrypt-file frost.tar --add
rm frost.tar
mv frost.tar.enc files/