#!/bin/sh

if [ -z $1 ] ; then 
  cd core
  echo "      core :\c"
  git branch | grep \* | sed 's/\*//'
  cd ..
  cd libraries
  echo " libraries :\c"
  git branch | grep \* | sed 's/\*//'
  cd ..
  cd extensions
  echo "extensions :\c"
  git branch | grep \* | sed 's/\*//'
  cd ..
  echo "      root :\c"
  git branch | grep \* | sed 's/\*//'
else
  cd core
  git checkout $1
  cd ..
  cd libraries
  git checkout $1
  cd ..
  cd extensions
  git checkout $1
  cd ..
  git checkout $1
fi
