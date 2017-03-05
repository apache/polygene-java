/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

var generators = require('yeoman-generator');
var fs = require('fs');

var polygene = {};

module.exports = generators.Base.extend(
    {
        // The name `constructor` is important here
        constructor: function () {
            // Calling the super constructor is important so our generator is correctly set up
            generators.Base.apply(this, arguments);

            this.option('import-model'); // --import-model reads model.json in current directory and creates the domain model for it.

            if (this.options.import != null) {
                polygene = importModel(this, './imported-model.json');
                polygene.name = polygene.name ? polygene.name : firstUpper(this.appname);
                polygene.packageName = polygene.packageName ? polygene.packageName : ("com.acme." + this.appname);
                polygene.singletonApp = false;  // not supported yet
                polygene.features = polygene.features ? polygene.features : ['rest api'];
                polygene.modules = polygene.modules ? polygene.modules : {};
                polygene.indexing = polygene.indexing ? polygene.indexing : null;
                polygene.entitystore = polygene.entitystore ? polygene.entitystore : null;
                polygene.caching = polygene.caching ? polygene.caching : null;
                polygene.serialization = polygene.serialization ? polygene.serialization : null;
                console.log(JSON.stringify(polygene,null,4));
            }
            else {
                polygene = {
                    name: firstUpper(this.appname),
                    packageName: "com.acme." + this.appname,
                    singletonApp: false,
                    entitystore: null,
                    indexing: null,
                    serialization: null,
                    caching: null,
                    features: ['rest api'],
                    modules: {}
                };
            }
        },

        prompting: function () {
            if (this.options.noPrompt != null) {
                return this.prompt([]);
            }
            else {
                return this.prompt(
                    [
                        {
                            type: 'input',
                            name: 'name',
                            message: 'Your project name',
                            default: polygene.name
                        },
                        {
                            type: 'input',
                            name: 'packageName',
                            message: 'Java package name',
                            default: polygene.packageName
                        },
                        {
                            type: 'list',
                            name: 'entitystore',
                            choices: [
                                'Cassandra',
                                'File',
                                'DerbySQL',
                                'Geode',
                                'H2SQL',
                                'Hazelcast',
                                'JClouds',
                                'Jdbm',
                                'LevelDB',
                                'Memory',
                                'MongoDB',
                                'MySQL',
                                'Preferences',
                                'Redis',
                                'Riak',
                                'PostgresSQL',
                                'SQLite'
                            ],
                            message: 'Which entity store do you want to use?',
                            default: polygene.entitystore
                        },
                        {
                            type: 'list',
                            name: 'indexing',
                            choices: [
                                'Rdf',
                                'ElasticSearch',
                                'Solr',
                                'SQL'
                            ],
                            message: 'Which indexing system do you want to use?'
                        },
                        {
                            type: 'list',
                            name: 'caching',
                            choices: [
                                'none',
                                'memcache',
                                'ehcache'
                            ],
                            message: 'Which caching system do you want to use?'
                        },
                        {
                            type: 'list',
                            name: 'serialization',
                            choices: [
                                'Jackson',
                                // 'Johnzon',
                                'Stax'
                            ],
                            message: 'Which serialization system do you want to use?'
                        },
                        {
                            type: 'list',
                            name: 'metrics',
                            choices: [
                                'none',
                                'codahale'
                            ],
                            message: 'Which metrics capturing system do you want to use?'
                        },
                        {
                            type: 'checkbox',
                            name: 'features',
                            choices: [
                                'rest api',
                                // 'jmx',
                                // 'version migration',
                                'sample (heroes) web application'
                            ],
                            message: 'Other features?'
                        }
                    ]
                ).then(function (answers) {
                        this.log('app name', answers.name);
                        this.log('Entity Stores:', answers.entitystore);
                        this.log('Indexing:', answers.indexing);
                        this.log('Caching:', answers.caching);
                        this.log('Serialization:', answers.serialization);
                        this.log('Features:', answers.features);
                        polygene.name = answers.name;
                        polygene.entitystore = answers.entitystore;
                        polygene.indexing = answers.indexing;
                        polygene.caching = answers.caching;
                        polygene.serialization = answers.serialization;
                        polygene.metrics = answers.metrics;
                        polygene.packageName = answers.packageName;
                        answers.features.forEach(function (f) {
                            polygene.features.push(f);
                        });
                        polygene.javaPackageDir = polygene.javaPackageDir ? polygene.javaPackageDir : polygene.packageName.replace(/[.]/g, '/');
                        polygene.singletonApp = false;
                        if (hasFeature('sample (heroes) web application')) {
                            polygene.features.push('rest api');
                        }
                    }.bind(this)
                );
            }
        },

        writing: function () {
            copyPolygeneBootstrap(this, "config", "ConfigurationLayer", !polygene.singeltonApp);
            copyPolygeneBootstrap(this, "infrastructure", "InfrastructureLayer", !polygene.singeltonApp);
            copyPolygeneBootstrap(this, "domain", "DomainLayer", !polygene.singeltonApp);
            copyPolygeneBootstrap(this, "connectivity", "ConnectivityLayer", !polygene.singeltonApp);

            copyPolygeneBootstrap(this, "config", "ConfigModule", true);

            copyPolygeneBootstrap(this, "infrastructure", "FileConfigurationModule", true);

            copyEntityStore(this, polygene.entitystore);

            copyPolygeneBootstrap(this, "infrastructure", "RdfIndexingModule", hasIndexing('Rdf'));
            copyPolygeneBootstrap(this, "infrastructure", "ElasticSearchIndexingModule", hasIndexing('Elasticsearch'));
            copyPolygeneBootstrap(this, "infrastructure", "SolrIndexingModule", hasIndexing('Solr'));
            copyPolygeneBootstrap(this, "infrastructure", "SqlIndexingModule", hasIndexing('Sql'));

            copyPolygeneBootstrap(this, "infrastructure", "NoCachingModule", hasCaching('none'));
            copyPolygeneBootstrap(this, "infrastructure", "MemcacheCachingModule", hasCaching('Memcache'));
            copyPolygeneBootstrap(this, "infrastructure", "EhCacheCachingModule", hasCaching('Ehcache'));

            copyPolygeneBootstrap(this, "infrastructure", "JacksonSerializationModule", hasSerialization('Jackson'));
            copyPolygeneBootstrap(this, "infrastructure", "StaxSerializationModule", hasSerialization('Stax'));
            copyPolygeneBootstrap(this, "infrastructure", "OrgJsonSerializationModule", hasSerialization('Orgjson'));

            copyPolygeneBootstrap(this, "connectivity", "RestApiModule", hasFeature('rest api'));
            copyPolygeneBootstrap(this, "infrastructure", "ReindexerModule", hasFeature('reindexer'));
            copyPolygeneBootstrap(this, "infrastructure", "MetricsModule", hasFeature('metrics'));
            copyPolygeneBootstrap(this, "infrastructure", "JmxModule", hasFeature('jmx'));
            copyPolygeneBootstrap(this, "infrastructure", "MigrationModule", hasFeature('version migration'));

            copyPolygeneBootstrap(this, "domain", "CrudModule", true);
            var ctx = this;
            Object.keys(polygene.modules).forEach(function (moduleName, index) {
                copyPolygeneDomainModule(ctx, moduleName, polygene.modules[moduleName]);
            });
            copyPolygeneBootstrap(this, "domain", "CrudModule", hasFeature('rest api'));
            // copyPolygeneBootstrap(this, "domain", "SecurityModule", true);
            copyHeroesSampleApp(this);
            copyPolygeneDomain(this, "security", "RestApiModule", "SecurityRepository", hasFeature('rest api'));

            copyRestFeature(this, hasFeature('rest api'));

            copyTemplate(this, 'buildtool/gradle-app.tmpl', 'app/build.gradle');
            copyTemplate(this, 'buildtool/gradle-bootstrap.tmpl', 'bootstrap/build.gradle');
            copyTemplate(this, 'buildtool/gradle-model.tmpl', 'model/build.gradle');
            copyTemplate(this, 'buildtool/gradle-rest.tmpl', 'rest/build.gradle');
            copyTemplate(this, 'buildtool/gradle-root.tmpl', 'build.gradle');
            copyTemplate(this, 'buildtool/settings.tmpl', 'settings.gradle');
            copyTemplate(this, 'buildtool/gradlew.tmpl', 'gradlew');
            copyTemplate(this, 'buildtool/gradlew-bat.tmpl', 'gradlew.bat');
            this.fs.copy(this.templatePath('buildtool/gradle-wrapper.jar_'), this.destinationPath('gradle/wrapper/gradle-wrapper.jar'));
            this.fs.copy(this.templatePath('buildtool/gradle-wrapper.properties_'), this.destinationPath('gradle/wrapper/gradle-wrapper.properties'));

            if (this.options.export != null) {
                exportModel(this, "exported-model.json");
            }
        }
    }
);

function copyPolygeneBootstrap(ctx, layer, moduleName, condition) {
    if (condition) {
        copyTemplate(ctx,
            moduleName + '/bootstrap.tmpl',
            'bootstrap/src/main/java/' + polygene.javaPackageDir + '/bootstrap/' + layer + '/' + moduleName + '.java');
    }
}

function copyEntityStore(ctx, entityStoreName) {
    copyTemplate(ctx,
        'StorageModule/bootstrap.tmpl',
        'bootstrap/src/main/java/' + polygene.javaPackageDir + '/bootstrap/infrastructure/' + entityStoreName + 'StorageModule.java');
}

function copyPolygeneApp(ctx, name, condition) {
    if (condition) {
        copyTemplate(ctx,
            name + '/bootstrap.tmpl',
            'bootstrap/src/main/java/' + polygene.javaPackageDir + '/bootstrap/' + name + 'ApplicationAssembler.java');

        copyTemplate(ctx,
            name + '/app.tmpl',
            'app/src/main/java/' + polygene.javaPackageDir + '/app/' + name + '.java');

        copyTemplate(ctx,
            name + '/webapp/',
            'app/src/main/webapp/');
    }
}

function copyPolygeneDomain(ctx, model, module, clazz, condition) {
    if (condition) {
        copyTemplate(ctx,
            module + '/' + clazz + '.tmpl',
            'model/src/main/java/' + polygene.javaPackageDir + '/model/' + model + '/' + clazz + '.java');
    }
}

function copyPolygeneDomainModule(ctx, moduleName, moduleDef) {
    var clazz = firstUpper(moduleName) + "Module";
    polygene.current = moduleDef;
    polygene.current.name = moduleName;
    copyTemplate(ctx,
        'DomainModule/bootstrap.tmpl',
        'bootstrap/src/main/java/' + polygene.javaPackageDir + '/bootstrap/domain/' + clazz + '.java');
    for (var idx1 in moduleDef.cruds) {
        if (moduleDef.cruds.hasOwnProperty(idx1)) {
            polygene.current.clazz = moduleDef.cruds[idx1];
            copyTemplate(ctx,
                'DomainModule/Crud.tmpl',
                'model/src/main/java/' + polygene.javaPackageDir + '/model/' + moduleName + '/' + moduleDef.cruds[idx1].name + '.java');
        }
    }
    for (var idx2 in moduleDef.values) {
        if (moduleDef.values.hasOwnProperty(idx2)) {
            polygene.current.clazz = moduleDef.values[idx2];
            copyTemplate(ctx,
                'DomainModule/Value.tmpl',
                'model/src/main/java/' + polygene.javaPackageDir + '/model/' + moduleName + '/' + moduleDef.values[idx2].name + '.java');
        }
    }
    for (var idx3 in moduleDef.entities) {
        if (moduleDef.entities.hasOwnProperty(idx3)) {
            polygene.current.clazz = moduleDef.entities[idx2];
            copyTemplate(ctx,
                'DomainModule/Entity.tmpl',
                'model/src/main/java/' + polygene.javaPackageDir + '/model/' + moduleName + '/' + moduleDef.entities[idx3].name + '.java');
        }
    }
    for (var idx4 in moduleDef.transients) {
        if (moduleDef.transients.hasOwnProperty(idx4)) {
            polygene.current.clazz = moduleDef.transients[idx3];
            copyTemplate(ctx,
                'DomainModule/Transient.tmpl',
                'model/src/main/java/' + polygene.javaPackageDir + '/model/' + moduleName + '/' + moduleDef.transients[idx4].name + '.java');
        }
    }
    for (var idx5 in moduleDef.objects) {
        if (moduleDef.objects.hasOwnProperty(idx5)) {
            polygene.current.clazz = moduleDef.objects[idx5];
            copyTemplate(ctx,
                'DomainModule/Object.tmpl',
                'model/src/main/java/' + polygene.javaPackageDir + '/model/' + moduleName + '/' + moduleDef.objects[idx5].name + '.java');
        }
    }
    for (var idx6 in moduleDef.services) {
        if (moduleDef.services.hasOwnProperty(idx6)) {
            polygene.current.clazz = moduleDef.services[idx6];
            copyTemplate(ctx,
                'DomainModule/Service.tmpl',
                'model/src/main/java/' + polygene.javaPackageDir + '/model/' + moduleName + '/' + moduleDef.services[idx6].name + '.java');
        }
    }
}

function copyRestFeature(ctx, condition) {
    if (condition) {
        copyPolygeneBootstrap(ctx, "domain", "SecurityModule", true);

        copyTemplate(ctx,
            'RestApiModule/SimpleEnroler.tmpl',
            'rest/src/main/java/' + polygene.javaPackageDir + '/rest/security/SimpleEnroler.java');

        copyTemplate(ctx,
            'RestApiModule/SimpleVerifier.tmpl',
            'rest/src/main/java/' + polygene.javaPackageDir + '/rest/security/SimpleVerifier.java');

        copyTemplate(ctx,
            'RestApiModule/HardcodedSecurityRepositoryMixin.tmpl',
            'model/src/main/java/' + polygene.javaPackageDir + '/model/security/HardcodedSecurityRepositoryMixin.java');
    }
}

function copyHeroesSampleApp(ctx) {
    copyPolygeneDomain(ctx, "heroes", "Heroes", "Hero", hasFeature('sample (heroes) web application'));
    copyPolygeneApp(ctx, "Heroes", hasFeature('sample (heroes) web application'));
    copyTemplate(ctx,
        'Heroes/web.tmpl',
        'app/src/main/webapp/WEB-INF/web.xml');
}

function copyTemplate(ctx, from, to) {
    ctx.fs.copyTpl(
        ctx.templatePath(from),
        ctx.destinationPath(to),
        {
            packageName: polygene.packageName,
            hasFeature: hasFeature,
            hasEntityStore: hasEntityStore,
            hasIndexing: hasIndexing,
            hasCaching: hasCaching,
            firstUpper: firstUpper,
            polygene: polygene
        }
    );
}

function hasEntityStore(esType) {
    return polygene.entitystore === esType;
}

function hasIndexing(indexingType) {
    return polygene.indexing === indexingType;
}

function hasCaching(cachingType) {
    return polygene.caching === cachingType;
}

function hasSerialization(serializer) {
    return polygene.serialization === serializer;
}

function hasFeature(feature) {
    return polygene.features.indexOf(feature) >= 0;
}

function firstUpper(text) {
    return text.charAt(0).toUpperCase() + text.substring(1);
}

function importModel(ctx, filename) {
    return JSON.parse(fs.readFileSync(filename, 'utf8'));
}

function exportModel(ctx, filename) {
    delete polygene.current;
    return fs.writeFileSync(filename, JSON.stringify(polygene, null, 4) + "\n", 'utf8');
}
