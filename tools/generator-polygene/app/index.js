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
                console.log(JSON.stringify(polygene, null, 4));
            }
            assignFunctions(polygene);
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
                            default: polygene.name ? polygene.name : firstUpper(this.appname)
                        },
                        {
                            type: 'input',
                            name: 'packageName',
                            message: 'Java package name',
                            default: polygene.packageName ? polygene.packageName : "com.acme"
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
                            default: polygene.entitystore ? polygene.entitystore : "Memory"
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
                            message: 'Which indexing system do you want to use?',
                            default: polygene.indexing ? polygene.indexing : "Rdf"
                        },
                        {
                            type: 'list',
                            name: 'caching',
                            choices: [
                                'None',
                                'Memcache',
                                'EhCache'
                            ],
                            message: 'Which caching system do you want to use?',
                            default: polygene.caching ? polygene.caching : "None"
                        },
                        {
                            type: 'list',
                            name: 'serialization',
                            choices: [
                                'Jackson',
                                // 'Johnzon',
                                'Stax'
                            ],
                            message: 'Which serialization system do you want to use?',
                            default: polygene.serialization ? polygene.serialization : "Jackson"
                        },
                        {
                            type: 'list',
                            name: 'metrics',
                            choices: [
                                'None',
                                'Codahale'
                            ],
                            message: 'Which metrics capturing system do you want to use?',
                            default: polygene.metrics ? polygene.metrics : "None"
                        },
                        {
                            type: 'checkbox',
                            name: 'features',
                            choices: [
                                'rest api'
                                , 'security'
                                // ,'version migration'
                                // ,'logging'
                                // ,'jmx'
                                // ,'circuit breakers'
                                // ,'file transactions'
                                // ,'spring integration'
                                // ,'servlet deployment'
                                // ,'osgi support'
                                // ,'alarms'
                                // ,'scheduling'
                                // ,'groovy mixins'
                                // ,'javascript mixins'
                            ],
                            message: 'Other features?',
                            default: polygene.features ? polygene.features : []
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
                        polygene.features = answers.features;
                        polygene.javaPackageDir = polygene.javaPackageDir ? polygene.javaPackageDir : polygene.packageName.replace(/[.]/g, '/');
                        polygene.singletonApp = false;
                    }.bind(this)
                );
            }
        },

        writing: function () {
            polygene.ctx = this;
            fs.readdir(__dirname + "/templates", function (err, files) {
                files.forEach(function (directory) {
                    if (directory.endsWith("Layer")) {
                        var layer = require(__dirname + '/templates/' + directory + '/layer.js');
                        layer.write(polygene);
                    }
                });
            });
            var buildToolChain = require(__dirname + '/templates/buildtool/build.js');
            buildToolChain.write(polygene);
            if (this.options.export != null) {
                exportModel(this, "exported-model.json");
            }
        }
    }
);


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

function assignFunctions(polygene) {

    polygene.hasFeature = function (feature) {
        console.log(polygene.features);
        return polygene.features.indexOf(feature) >= 0;
    };

    polygene.copyTemplate = function (ctx, from, to) {
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
    };

    polygene.copyPolygeneBootstrap = function (ctx, layer, moduleName, condition) {
        if (condition) {
            copyTemplate(ctx,
                moduleName + '/bootstrap.tmpl',
                'bootstrap/src/main/java/' + polygene.javaPackageDir + '/bootstrap/' + layer + '/' + moduleName + '.java');
        }
    };

    polygene.copyEntityStore = function (ctx, entityStoreName) {
        copyTemplate(ctx,
            'StorageModule/bootstrap.tmpl',
            'bootstrap/src/main/java/' + polygene.javaPackageDir + '/bootstrap/infrastructure/' + entityStoreName + 'StorageModule.java');
    };

    polygene.copyModules = function (dirname) {
        fs.readdir(dirname, function (err, files) {
            if (files !== undefined) {
                files.forEach(function (directory) {
                    if (directory.endsWith("Module")) {
                        var module = require(dirname + "/" + directory + '/module.js');
                        module.write(polygene);
                    }
                });
            }
        });
    };
    polygene.firstUpper = function (text) {
        return text.charAt(0).toUpperCase() + text.substring(1);
    };
    polygene.typeNameOnly = function(text) {
        var lastPos = text.lastIndexOf(".");
        if( lastPos < 0 ) {
            return text;
        }
        return text.substring(lastPos + 1);
    };

    polygene.configurationClassName = function( clazzName ) {
        if( clazzName.endsWith( "Service" )) {
            clazzName = clazzName.substring(0, clazzName.length - 7 );
        }
        return clazzName + "Configuration";
    };

    polygene.prepareClazz = function (current) {
        var state = [];
        var imported = {};
        var props = current.clazz.properties;
        if( props ) {
            for( var idx in props ) {
                var prop = props[idx];
                state.push( 'Property' + '<' + polygene.typeNameOnly(prop.type) + "> " + prop.name + "();")
                imported[prop.type] = imported[prop.type];
            }
        } else {
            state.push( 'Property<String> name();    // TODO: remove sample property')
        }
        var assocs = current.clazz.associations;
        if( assocs ) {
            for( var idx in assocs ) {
                var assoc = assocs[idx];
                state.push("Association" + '<' +  polygene.typeNameOnly(assoc.type) + '>' + assoc.name + "();")
                imported[assoc.type] = imported[assoc.type] ;
            }
        }
        assocs = current.clazz.manyassociations;
        if( assocs ) {
            for( var idx in assocs ) {
                var assoc = assocs[idx];
                state.push("ManyAssociation<" +  polygene.typeNameOnly(assoc.type) + ">" + assoc.name + "();")
                imported[assoc.type] = imported[assoc.type] ;
            }
        }
        assocs = current.clazz.namedassociations;
        if( assocs ) {
            for( var idx in assocs ) {
                var assoc = assocs[idx];
                state.push("NamedAssociation<" +  polygene.typeNameOnly(assoc.type) + ">" + assoc.name + "();")
                imported[assoc.type] = imported[assoc.type];
            }
        }
        current.state = state;
        current.imported = imported;
    };

    polygene.prepareConfigClazz = function (current) {
        var state = [];
        var imported = {};
        var props = current.clazz.configuration;
        if( props ) {
            for( var idx in props ) {
                var prop = props[idx];
                state.push( 'Property' + '<' + polygene.typeNameOnly(prop.type) + "> " + prop.name + "();")
                imported[prop.type] = imported[prop.type];
            }
        } else {
            state.push( 'Property<String> name();    // TODO: remove sample property')
        }
        current.state = state;
        current.imported = imported;
    };

}