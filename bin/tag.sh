#!/bin/sh

VERSION=$1
MESSAGE=$2
git tag -s -m $MESSAGE $VERSION
git push --tags
