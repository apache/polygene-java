#!/bin/sh

VERSION=$1
cd core
git tag -s -m "Release Candidate 5" $VERSION
git push --tags
cd ..
cd libraries
git tag -s -m "Release Candidate 5" $VERSION
git push --tags
cd ..
cd extensions
git tag -s -m "Release Candidate 5" $VERSION
git push --tags
cd ..
git tag -s -m "Release Candidate 5" $VERSION
git push --tags
