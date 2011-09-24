#!/bin/sh

VERSION=$1
MESSAGE=$2
cd core
git tag -s -m $MESSAGE $VERSION
git push --tags
cd ..
cd libraries
git tag -s -m $MESSAGE $VERSION
git push --tags
cd ..
cd extensions
git tag -s -m $MESSAGE $VERSION
git push --tags
cd ..
git tag -s -m $MESSAGE $VERSION
git push --tags
