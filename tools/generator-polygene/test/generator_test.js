var path = require('path');
var helpers = require('yeoman-test');
var assert = require('yeoman-assert');
var shell = require('shelljs');

//See http://yeoman.io/authoring/testing.html

// test with all defaults first.
test();

var entityStores = [
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
];

var indexings = [
    'Rdf',
    'ElasticSearch',
    'Solr',
    'SQL'
];

var cachings = [
    'None',
    'Memcache',
    'EhCache'
];

var serializations = [
    'Jackson',
    // 'Johnzon',
    'Stax'
];

var metricses = [
    'None',
    'Codahale'
];

var featuresset = [
    [],
    ['rest api'],
    ['security'],
    ['rest api, security']
];

entityStores.forEach(function (entityStore) {
    test(entityStore, "Rdf", "Jackson", "Memcache", "Codahale", "[]");
});

indexings.forEach(function (indexing) {
    test("Memory", indexing, "Jackson", "Memcache", "Codahale", "[]");
});

serializations.forEach(function (serialization) {
    test("Memory", "Rdf", serialization, "Memcache", "Codahale", "[]");
});

cachings.forEach(function (caching) {
    test("Memory", "Rdf", "Jackson", caching, "Codahale", "[]");
});

metricses.forEach(function (metrics) {
    test("Memory", "Rdf", "Jackson", "Memcache", metrics, "[]");
});

featuresset.forEach(function (feature) {
    test("Memory", "Rdf", "Jackson", "Memcache", "Codahale", feature);
});

// All Tests !!!!
entityStores.forEach(function (entitystore) {
    indexings.forEach(function (indexing) {
        serializations.forEach(function (serialization) {
            cachings.forEach(function (caching) {
                metricses.forEach(function (metrics) {
                    featuresset.forEach(function (features) {
                        test(entitystore, indexing, serialization, caching, metrics, features)
                    });
                });
            });
        });
    });
});

function test(entityStore, indexing, serialization, caching, metrics, features) {
    describe('polygene-generator', function () {
        this.timeout(10000);
        it('generates a Gradle buildable Apache Polygene project with '
            + entityStore + 'EntityStore, '
            + indexing + 'Indexing, '
            + serialization + 'Serialzation, '
            + caching + 'Caching, '
            + metrics + 'Metrics, '
            + ' and ' + features + '.',
            function () {
                return helpers.run(path.join(__dirname, '../app'))
                    .inDir(path.join(__dirname, '../build/test-project'))
                    .withPrompts({
                        name: 'test-project',
                        packageName: 'org.apache.polygene.generator.test',
                        entitystore: entityStore,
                        serialization: serialization,
                        indexing: indexing,
                        caching: caching,
                        metrics: metrics,
                        features: features
                    })
                    .then(buildAndVerify);
            });
    });
}

function buildAndVerify(dir) {
    assert.file(['gradlew', 'settings.gradle', 'build.gradle']);
    assert(shell.exec(path.join(dir, 'gradlew') + ' build').code == 0);
}