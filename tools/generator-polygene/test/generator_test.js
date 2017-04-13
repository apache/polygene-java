var path = require('path');
var helpers = require('yeoman-test');
var assert = require('yeoman-assert');
var shell = require('shelljs');

//See http://yeoman.io/authoring/testing.html

// test with all defaults first.
test();

var appTypes = [
    'Command Line',
    "Rest API"
];

var entityStores = [
    'Cassandra',
    'File',
    'DerbySQL',
    'Geode',
    'H2SQL',
    'Hazelcast',
    'JClouds',
    'Jdbm',
    'LevelDB',
    'MongoDB',
    'MySQL',
    'Preferences',
    'Redis',
    'Riak',
    'PostgreSQL',
    'SQLite',
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
    'JavaxJson',
    'JavaxXml',
    'MessagePack'
];

var metricses = [
    'None',
    'Codahale'
];

var featuresset = [
    [],
    ['jmx'],
    ['mixin scripting'],
    ['security'],
    ['jmx', 'mixin scripting'],
    ['jmx', 'scripting'],
    ['mixin scripting', 'scripting'],
    ['jmx', 'mixin scripting', 'scripting']
];

appTypes.forEach(function (appType) {
    test(appType, "Memory", "Rdf", "JavaxJson", "Memcache", "Codahale", "[]");
});

entityStores.forEach(function (entityStore) {
    test("Rest API", entityStore, "Rdf", "JavaxJson", "Memcache", "Codahale", "[]");
});

indexings.forEach(function (indexing) {
    test("Rest API", "Memory", indexing, "JavaxJson", "Memcache", "Codahale", "[]");
});

serializations.forEach(function (serialization) {
    test("Rest API", "Memory", "Rdf", serialization, "Memcache", "Codahale", "[]");
});

cachings.forEach(function (caching) {
    test("Rest API", "Memory", "Rdf", "JavaxJson", caching, "Codahale", "[]");
});

metricses.forEach(function (metrics) {
    test("Rest API", "Memory", "Rdf", "JavaxJson", "Memcache", metrics, "[]");
});

featuresset.forEach(function (feature) {
    test("Rest API", "Memory", "Rdf", "JavaxJson", "Memcache", "Codahale", feature);
});

// All Tests !!!!
appTypes.forEach(function (appType) {
    entityStores.forEach(function (entitystore) {
        indexings.forEach(function (indexing) {
            serializations.forEach(function (serialization) {
                cachings.forEach(function (caching) {
                    metricses.forEach(function (metrics) {
                        featuresset.forEach(function (features) {
                            test(appType, entitystore, indexing, serialization, caching, metrics, features)
                        });
                    });
                });
            });
        });
    });
});

function test(appType, entityStore, indexing, serialization, caching, metrics, features) {
    describe('polygene-generator', function () {
        this.timeout(10000);
        it('generates a Gradle buildable ' + appType + ' Apache Polygene project with '
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
                        applicationtype: appType,
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
