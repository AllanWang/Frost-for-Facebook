#!/usr/bin/env bash

# Add appropriate files for encryption
# https://docs.github.com/en/actions/reference/encrypted-secrets#limits-for-secrets

rm frost_github.tar.gpg
tar cvf frost_github.tar release.keystore release.properties test.keystore test.properties
gpg --symmetric --cipher-algo AES256 frost_github.tar
rm frost_github.tar