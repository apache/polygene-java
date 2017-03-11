var path = require('path');
var helpers = require('yeoman-test');
var assert = require('yeoman-assert');
var shell = require('shelljs');

// See http://yeoman.io/authoring/testing.html
describe('polygene-generator', function () {
  it('generates a buildable gradle project', function () {
      return helpers.run(path.join(__dirname, '../app'))
        .withPrompts({
            name: 'test-project',
            packageName: 'org.apache.polygene.generator.test'
        })
        .then(function(dir) {
            assert.file(['gradlew', 'settings.gradle', 'build.gradle']);
            assert(shell.exec(path.join(dir, 'gradlew') + ' build').code == 0);
        });
  });
});
