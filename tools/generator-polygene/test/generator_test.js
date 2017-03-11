var path = require('path');
var helpers = require('yeoman-test');
var assert = require('yeoman-assert');
var shell = require('shelljs');

//See http://yeoman.io/authoring/testing.html

describe('polygene-generator-defaults', function () {
    this.timeout(10000);
    it('generates a Gradle buildable Apache Polygene project', function () {
        return helpers.run(path.join(__dirname, '../app'))
            .withPrompts({
                name: 'test-project',
                packageName: 'org.apache.polygene.generator.test'
            })
            .then(buildAndVerify);
    });
});

[
    'Cassandra',
    'File',
    'Geode',
    'Hazelcast',
    'JClouds',
    'Jdbm',
    'LevelDB',
    'MongoDB',
    'Preferences',
    'Redis',
    'Riak',
    'SQL',
    'Memory'   // Somehow the last EntityStore is used in subsequent test arrays. Pick the fastest.
].forEach(function (entityStore) {
    describe('polygene-generator-default-and-' + entityStore.toLowerCase() + "-entitystore", function () {
        this.timeout(10000);
        it('generates a Gradle buildable Apache Polygene project with ' + entityStore + ' as the Entity Store', function () {
            return helpers.run(path.join(__dirname, '../app'))
                .withPrompts({
                    name: 'test-project',
                    packageName: 'org.apache.polygene.generator.test',
                    entitystore: entityStore
                })
                .then(buildAndVerify);
        });
    });
});

[
    'Rdf',
    'ElasticSearch',
    'Solr',
    'SQL'
].forEach(function (indexing) {
    describe('polygene-generator-default-and-' + indexing.toLowerCase() + '-indexing', function () {
        this.timeout(10000);
        it('generates a Gradle buildable Apache Polygene project with '+ indexing + ' as the Indexing system', function () {
            return helpers.run(path.join(__dirname, '../app'))
                .withPrompts({
                    name: 'test-project',
                    packageName: 'org.apache.polygene.generator.test',
                    indexing: indexing
                })
                .then(buildAndVerify);
        });
    });
});

[
    'None',
    'Memcache',
    'EhCache'
].forEach(function (caching) {
    describe('polygene-generator-default-and-' + caching.toLowerCase() + '-caching', function () {
        this.timeout(10000);
        it('generates a Gradle buildable Apache Polygene project with '+caching+' as the Caching system', function () {
            return helpers.run(path.join(__dirname, '../app'))
                .withPrompts({
                    name: 'test-project',
                    packageName: 'org.apache.polygene.generator.test',
                    caching: caching
                })
                .then(buildAndVerify);
        });
    });
});

[
    'Jackson',
    // 'Johnzon',
    'Stax'
].forEach(function (serialization) {
    describe('polygene-generator-default-and-' + serialization.toLowerCase() + '-caching', function () {
        this.timeout(10000);
        it('generates a Gradle buildable Apache Polygene project with '+serialization+' as the Serialization system', function () {
            return helpers.run(path.join(__dirname, '../app'))
                .withPrompts({
                    name: 'test-project',
                    packageName: 'org.apache.polygene.generator.test',
                    serialization: serialization
                })
                .then(buildAndVerify);
        });
    });
});

[
    'None',
    'Codahale'
].forEach(function (metrics) {
    describe('polygene-generator-default-and-' + metrics.toLowerCase() + '-caching', function () {
        this.timeout(10000);
        it('generates a Gradle buildable Apache Polygene project with '+metrics+' as the Metrics system', function () {
            return helpers.run(path.join(__dirname, '../app'))
                .withPrompts({
                    name: 'test-project',
                    packageName: 'org.apache.polygene.generator.test',
                    metrics: metrics
                })
                .then(buildAndVerify);
        });
    });
});


function buildAndVerify(dir) {
    assert.file(['gradlew', 'settings.gradle', 'build.gradle']);
    assert(shell.exec(path.join(dir, 'gradlew') + ' build').code == 0);
}