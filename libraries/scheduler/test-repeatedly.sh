#!/bin/sh

# Run clean test once and test repeatedly
# Stop on first failure
# cat target/num-repeats to see how many times it ran
# Use time run-repeatedly.sh to get a time mesure
#
# Written because of milliseconds delays due to multithreading

set -e

mvn clean test 
echo "x "`date` > target/num-repeats

while ( true ) ; do
	mvn test
	echo "x "`date` >> target/num-repeats
done

exit 0
