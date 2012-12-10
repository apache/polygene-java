
Regression Test reporting follows the following steps;

1. Go to http://ops4j1.jira.com/browse/QI and create a new JIRA issue about the problem.

2. Create a package named org.qi4j.tests.regression.qi123 (for QI-123) in $QI4J/tests/regression/src/main/java.
   NOTE: observe that the test MUST sit in the src/MAIN/java and not under src/test

3. Create a JUnit or TestNG test capturing the issue described in JIRA.

4. Commit and push this to the 'origin develop' branch.

5. Ping the community on the qi4j-dev forum at Google Groups.


Once the reported problem has been solved, the test will be moved to the sub-project's unit test area.
