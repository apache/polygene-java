#!/bin/sh

# Run clean test once and test repeatedly
# Stop on first failure
# cat build/num-repeats to see how many times it ran
# Use time run-repeatedly.sh to get a time mesure
#
# Written because of milliseconds delays due to multithreading

set -e

../../gradlew clean test -Dversion=2.0-SNAPSHOT
echo "x "`date` > build/num-repeats

while ( true ) ; do
	../../gradlew test -Dversion=2.0-SNAPSHOT
	echo "x "`date` >> build/num-repeats
done

exit 0
