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

var generators = require( 'yeoman-generator' );

var zest = {};

module.exports = generators.Base.extend(
    {
        // The name `constructor` is important here
        constructor: function ()
        {
            // Calling the super constructor is important so our generator is correctly set up
            generators.Base.apply( this, arguments );

            // this.option( 'coffee' ); // This method adds support for a `--coffee` flag
        },

        method1: function ()
        {
            console.log( 'method 1 just ran' );
        },
        method2: function ()
        {
            console.log( 'method 2 just ran' );
        },
        prompting: function ()
        {
            return this.prompt(
                [
                    {
                        type: 'input',
                        name: 'name',
                        message: 'Your project name',
                        default: firstUpper( this.appname )
                    },
                    {
                        type: 'input',
                        name: 'packagename',
                        message: 'Java package name',
                        default: this.appname // Default to current folder name
                    },
                    {
                        type: 'list',
                        name: 'entitystore',
                        choices: [
                            'file',
                            'geode',
                            'hazelcast',
                            'jclouds',
                            'jdbm',
                            'leveldb',
                            'in-memory',
                            'mongodb',
                            'preferences',
                            'redis',
                            'riak',
                            'sql'
                        ],
                        message: 'Which entity store do you want to use?'
                    },
                    {
                        type: 'list',
                        name: 'indexing',
                        choices: [
                            'rdf',
                            'elasticsearch',
                            'solr',
                            'sql'
                        ],
                        message: 'Which caching system do you want to use?'
                    },
                    {
                        type: 'list',
                        name: 'caching',
                        choices: [
                            'none',
                            'memcache',
                            'ehcache'
                        ],
                        message: 'Which serialization system do you want to use?'
                    },
                    {
                        type: 'list',
                        name: 'serialization',
                        choices: [
                            'jackson',
                            'stax',
                            'org.json'
                        ],
                        message: 'Which indexing system do you want to use?'
                    },
                    {
                        type: 'checkbox',
                        name: 'features',
                        choices: [
                            'rest api',
                            // 'reindexer',
                            // 'metrics',
                            // 'jmx',
                            // 'version migration',
                            'sample (heroes) web application'
                        ],
                        message: 'Other features?'
                    }
                ]
            ).then( function ( answers )
                    {
                        this.log( 'app name', answers.name );
                        this.log( 'Entity Stores:', answers.entitystore );
                        this.log( 'Indexing:', answers.indexing );
                        this.log( 'Caching:', answers.caching );
                        this.log( 'Serialization:', answers.serialization );
                        this.log( 'Features:', answers.features );
                        zest = answers;
                        zest.javaPackageDir = zest.packagename.replace( '.', '/' );
                        zest.singletonApp = false;
                        zest.entitystore = firstUpper( zest.entitystore );
                        zest.indexing = firstUpper( zest.indexing );
                        zest.caching = firstUpper( zest.caching );
                        zest.serialization = firstUpper( zest.serialization );
                        if( hasFeature( 'sample (heroes) web application' ) )
                        {
                            zest.features.push( 'rest api' );
                        }
                    }.bind( this )
            );
        },

        writing: function ()
        {
            copyZestBootstrap( this, "config", "ConfigurationLayer", !zest.singeltonApp );
            copyZestBootstrap( this, "infrastructure", "InfrastructureLayer", !zest.singeltonApp );
            copyZestBootstrap( this, "domain", "DomainLayer", !zest.singeltonApp );
            copyZestBootstrap( this, "connectivity", "ConnectivityLayer", !zest.singeltonApp );

            copyZestBootstrap( this, "config", "ConfigModule", true );

            copyZestBootstrap( this, "infrastructure", "FileConfigurationModule", true );

            copyZestBootstrap( this, "infrastructure", "FileStorageModule", hasEntityStore( 'File' ) );
            copyZestBootstrap( this, "infrastructure", "GeodeStorageModule", hasEntityStore( 'Geode' ) );
            copyZestBootstrap( this, "infrastructure", "HazelcastStorageModule", hasEntityStore( 'Hazelcast' ) );
            copyZestBootstrap( this, "infrastructure", "JCloudsStorageModule", hasEntityStore( 'Jclouds' ) );
            copyZestBootstrap( this, "infrastructure", "JdbmStorageModule", hasEntityStore( 'Jdbm' ) );
            copyZestBootstrap( this, "infrastructure", "LevelDbStorageModule", hasEntityStore( 'Leveldb' ) );
            copyZestBootstrap( this, "infrastructure", "InMemoryStorageModule", hasEntityStore( 'Memory' ) );
            copyZestBootstrap( this, "infrastructure", "MongoDbStorageModule", hasEntityStore( 'Mongodb' ) );
            copyZestBootstrap( this, "infrastructure", "PreferencesStorageModule", hasEntityStore( 'Preferences' ) );
            copyZestBootstrap( this, "infrastructure", "RedisStorageModule", hasEntityStore( 'Redis' ) );
            copyZestBootstrap( this, "infrastructure", "RiakStorageModule", hasEntityStore( 'Riak' ) );
            copyZestBootstrap( this, "infrastructure", "SqlStorageModule", hasEntityStore( 'Sql' ) );

            copyZestBootstrap( this, "infrastructure", "RdfIndexingModule", hasIndexing( 'Rdf' ) );
            copyZestBootstrap( this, "infrastructure", "ElasticSearchIndexingModule", hasIndexing( 'Elasticsearch' ) );
            copyZestBootstrap( this, "infrastructure", "SolrIndexingModule", hasIndexing( 'Solr' ) );
            copyZestBootstrap( this, "infrastructure", "SqlIndexingModule", hasIndexing( 'Sql' ) );

            copyZestBootstrap( this, "infrastructure", "NoCachingModule", hasCaching( 'none' ) );
            copyZestBootstrap( this, "infrastructure", "MemcacheCachingModule", hasCaching( 'Memcache' ) );
            copyZestBootstrap( this, "infrastructure", "EhCacheCachingModule", hasCaching( 'Ehcache' ) );

            copyZestBootstrap( this, "infrastructure", "JacksonSerializationModule", hasSerialization( 'Jackson' ) );
            copyZestBootstrap( this, "infrastructure", "StaxSerializationModule", hasSerialization( 'Stax' ) );
            copyZestBootstrap( this, "infrastructure", "OrgJsonSerializationModule", hasSerialization( 'Orgjson' ) );

            copyZestBootstrap( this, "connectivity", "RestApiModule", hasFeature( 'rest api' ) );
            copyZestBootstrap( this, "infrastructure", "ReindexerModule", hasFeature( 'reindexer' ) );
            copyZestBootstrap( this, "infrastructure", "MetricsModule", hasFeature( 'metrics' ) );
            copyZestBootstrap( this, "infrastructure", "JmxModule", hasFeature( 'jmx' ) );
            copyZestBootstrap( this, "infrastructure", "MigrationModule", hasFeature( 'version migration' ) );

            copyZestBootstrap( this, "domain", "CrudModule", true );
            copyHeroesSampleApp( this );
            copyZestDomain( this, "security", "RestApiModule", "SecurityRepository", hasFeature( 'rest api' ) );

            copyRestFeature( this, hasFeature( 'rest api' ) );

            this.fs.copyTpl( this.templatePath( 'buildtool/gradle-app.tmpl' ), this.destinationPath( 'app/build.gradle' ), {} );
            this.fs.copyTpl( this.templatePath( 'buildtool/gradle-bootstrap.tmpl' ), this.destinationPath( 'bootstrap/build.gradle' ), {} );
            this.fs.copyTpl( this.templatePath( 'buildtool/gradle-model.tmpl' ), this.destinationPath( 'model/build.gradle' ), {} );
            this.fs.copyTpl( this.templatePath( 'buildtool/gradle-rest.tmpl' ), this.destinationPath( 'rest/build.gradle' ), {} );
            this.fs.copyTpl( this.templatePath( 'buildtool/gradle-root.tmpl' ), this.destinationPath( 'build.gradle' ), {} );
            this.fs.copyTpl( this.templatePath( 'buildtool/settings.tmpl' ), this.destinationPath( 'settings.gradle' ), {projectName: zest.name} );
            this.fs.copyTpl( this.templatePath( 'buildtool/gradlew.tmpl' ), this.destinationPath( 'gradlew' ), {} );
            this.fs.copyTpl( this.templatePath( 'buildtool/gradlew-bat.tmpl' ), this.destinationPath( 'gradlew.bat' ), {} );
            this.fs.copy( this.templatePath( 'buildtool/gradle-wrapper.jar_' ), this.destinationPath( 'gradle/wrapper/gradle-wrapper.jar' ) );
            this.fs.copy( this.templatePath( 'buildtool/gradle-wrapper.properties_' ), this.destinationPath( 'gradle/wrapper/gradle-wrapper.properties' ) );
        }
    }
);

function copyZestBootstrap( ctx, layer, moduleName, condition )
{
    if( condition )
    {
        copyTemplate( ctx,
                      moduleName + '/bootstrap.tmpl',
                      'bootstrap/src/main/java/' + zest.javaPackageDir + '/bootstrap/' + layer + '/' + moduleName + '.java' );
    }
}

function copyZestApp( ctx, name, condition )
{
    if( condition )
    {
        copyTemplate( ctx,
                      name + '/bootstrap.tmpl',
                      'bootstrap/src/main/java/' + zest.javaPackageDir + '/bootstrap/' + name + 'ApplicationAssembler.java' );

        copyTemplate( ctx,
                      name + '/app.tmpl',
                      'app/src/main/java/' + zest.javaPackageDir + '/app/' + name + '.java' );

        copyTemplate( ctx,
                      name + '/webapp/',
                      'app/src/main/webapp/' );
    }
}

function copyZestDomain( ctx, model, module, clazz, condition )
{
    if( condition )
    {
        copyTemplate( ctx,
                      module + '/' + clazz + '.tmpl',
                      'model/src/main/java/' + zest.javaPackageDir + '/model/' + model + '/' + clazz + '.java' );
    }
}

function copyRestFeature( ctx, condition )
{
    if( condition )
    {
        copyZestBootstrap( ctx, "domain", "SecurityModule", true );

        copyTemplate( ctx,
                      'RestApiModule/SimpleEnroler.tmpl',
                      'rest/src/main/java/' + zest.javaPackageDir + '/rest/security/SimpleEnroler.java' );

        copyTemplate( ctx,
                      'RestApiModule/SimpleVerifier.tmpl',
                      'rest/src/main/java/' + zest.javaPackageDir + '/rest/security/SimpleVerifier.java' );

        copyTemplate( ctx,
                      'RestApiModule/HardcodedSecurityRepositoryMixin.tmpl',
                      'model/src/main/java/' + zest.javaPackageDir + '/model/security/HardcodedSecurityRepositoryMixin.java' );
    }
}

function copyHeroesSampleApp( ctx )
{
    copyZestDomain( ctx, "heroes", "Heroes", "Hero", hasFeature( 'sample (heroes) web application' ) );
    copyZestApp( ctx, "Heroes", hasFeature( 'sample (heroes) web application' ) );
    copyTemplate( ctx,
                  'Heroes/web.tmpl',
                  'app/src/main/webapp/WEB-INF/web.xml' );
}

function copyTemplate( ctx, from, to )
{
    ctx.fs.copyTpl(
        ctx.templatePath( from ),
        ctx.destinationPath( to ),
        {
            packageName: zest.packagename,
            hasFeature: hasFeature,
            hasEntityStore: hasEntityStore,
            hasIndexing: hasIndexing,
            hasCaching: hasCaching,
            zest: zest
        }
    );
}

function hasEntityStore( esType )
{
    return zest.entitystore === esType;
}

function hasIndexing( indexingType )
{
    return zest.indexing === indexingType;
}

function hasCaching( cachingType )
{
    return zest.caching === cachingType;
}

function hasSerialization( serializer )
{
    return zest.serialization === serializer;
}

function hasFeature( feature )
{
    return zest.features.indexOf( feature ) >= 0;
}

function firstUpper( text )
{
    return text.charAt( 0 ).toUpperCase() + text.substring( 1 );
}
