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

var polygene = {};

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
                            'File',
                            'Geode',
                            'Hazelcast',
                            'JClouds',
                            'Jdbm',
                            'LevelDB',
                            'Memory',
                            'MongoDB',
                            'Preferences',
                            'Redis',
                            'Riak',
                            'MySQL',
                            'PostgresSQL',
                            'SQLite',
                            'H2SQL',
                            'DerbySQL'
                        ],
                        message: 'Which entity store do you want to use?'
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
                            'Stax',
                            'OrgJson'
                        ],
                        message: 'Which serialization system do you want to use?'
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
                        polygene = answers;
                        polygene.javaPackageDir = polygene.packagename.replace( '.', '/' );
                        polygene.singletonApp = false;
                        if( hasFeature( 'sample (heroes) web application' ) )
                        {
                            polygene.features.push( 'rest api' );
                        }
                    }.bind( this )
            );
        },

        writing: function ()
        {
            copyPolygeneBootstrap( this, "config", "ConfigurationLayer", !polygene.singeltonApp );
            copyPolygeneBootstrap( this, "infrastructure", "InfrastructureLayer", !polygene.singeltonApp );
            copyPolygeneBootstrap( this, "domain", "DomainLayer", !polygene.singeltonApp );
            copyPolygeneBootstrap( this, "connectivity", "ConnectivityLayer", !polygene.singeltonApp );

            copyPolygeneBootstrap( this, "config", "ConfigModule", true );

            copyPolygeneBootstrap( this, "infrastructure", "FileConfigurationModule", true );

            copyEntityStore( this, polygene.entitystore );

            copyPolygeneBootstrap( this, "infrastructure", "RDFIndexingModule", hasIndexing( 'Rdf' ) );
            copyPolygeneBootstrap( this, "infrastructure", "ElasticSearchIndexingModule", hasIndexing( 'Elasticsearch' ) );
            copyPolygeneBootstrap( this, "infrastructure", "SolrIndexingModule", hasIndexing( 'Solr' ) );
            copyPolygeneBootstrap( this, "infrastructure", "SqlIndexingModule", hasIndexing( 'Sql' ) );

            copyPolygeneBootstrap( this, "infrastructure", "NoCachingModule", hasCaching( 'none' ) );
            copyPolygeneBootstrap( this, "infrastructure", "MemcacheCachingModule", hasCaching( 'Memcache' ) );
            copyPolygeneBootstrap( this, "infrastructure", "EhCacheCachingModule", hasCaching( 'Ehcache' ) );

            copyPolygeneBootstrap( this, "infrastructure", "JacksonSerializationModule", hasSerialization( 'Jackson' ) );
            copyPolygeneBootstrap( this, "infrastructure", "StaxSerializationModule", hasSerialization( 'Stax' ) );
            copyPolygeneBootstrap( this, "infrastructure", "OrgJsonSerializationModule", hasSerialization( 'Orgjson' ) );

            copyPolygeneBootstrap( this, "connectivity", "RestApiModule", hasFeature( 'rest api' ) );
            copyPolygeneBootstrap( this, "infrastructure", "ReindexerModule", hasFeature( 'reindexer' ) );
            copyPolygeneBootstrap( this, "infrastructure", "MetricsModule", hasFeature( 'metrics' ) );
            copyPolygeneBootstrap( this, "infrastructure", "JmxModule", hasFeature( 'jmx' ) );
            copyPolygeneBootstrap( this, "infrastructure", "MigrationModule", hasFeature( 'version migration' ) );

            copyPolygeneBootstrap( this, "domain", "CrudModule", true );
            copyHeroesSampleApp( this );
            copyPolygeneDomain( this, "security", "RestApiModule", "SecurityRepository", hasFeature( 'rest api' ) );

            copyRestFeature( this, hasFeature( 'rest api' ) );

            copyTemplate( this, 'buildtool/gradle-app.tmpl', 'app/build.gradle' );
            copyTemplate( this, 'buildtool/gradle-bootstrap.tmpl', 'bootstrap/build.gradle' );
            copyTemplate( this, 'buildtool/gradle-model.tmpl', 'model/build.gradle' );
            copyTemplate( this, 'buildtool/gradle-rest.tmpl', 'rest/build.gradle' );
            copyTemplate( this, 'buildtool/gradle-root.tmpl', 'build.gradle' );
            copyTemplate( this, 'buildtool/settings.tmpl', 'settings.gradle' );
            copyTemplate( this, 'buildtool/gradlew.tmpl', 'gradlew' );
            copyTemplate( this, 'buildtool/gradlew-bat.tmpl', 'gradlew.bat' );
            this.fs.copy( this.templatePath( 'buildtool/gradle-wrapper.jar_' ), this.destinationPath( 'gradle/wrapper/gradle-wrapper.jar' ) );
            this.fs.copy( this.templatePath( 'buildtool/gradle-wrapper.properties_' ), this.destinationPath( 'gradle/wrapper/gradle-wrapper.properties' ) );
        }
    }
);

function copyPolygeneBootstrap( ctx, layer, moduleName, condition )
{
    if( condition )
    {
        copyTemplate( ctx,
                      moduleName + '/bootstrap.tmpl',
                      'bootstrap/src/main/java/' + polygene.javaPackageDir + '/bootstrap/' + layer + '/' + moduleName + '.java' );
    }
}

function copyEntityStore( ctx, entityStoreName )
{
    copyTemplate( ctx,
                  'StorageModule/bootstrap.tmpl',
                  'bootstrap/src/main/java/' + polygene.javaPackageDir + '/bootstrap/infrastructure/' + entityStoreName + 'StorageModule.java' );
}

function copyPolygeneApp( ctx, name, condition )
{
    if( condition )
    {
        copyTemplate( ctx,
                      name + '/bootstrap.tmpl',
                      'bootstrap/src/main/java/' + polygene.javaPackageDir + '/bootstrap/' + name + 'ApplicationAssembler.java' );

        copyTemplate( ctx,
                      name + '/app.tmpl',
                      'app/src/main/java/' + polygene.javaPackageDir + '/app/' + name + '.java' );

        copyTemplate( ctx,
                      name + '/webapp/',
                      'app/src/main/webapp/' );
    }
}

function copyPolygeneDomain( ctx, model, module, clazz, condition )
{
    if( condition )
    {
        copyTemplate( ctx,
                      module + '/' + clazz + '.tmpl',
                      'model/src/main/java/' + polygene.javaPackageDir + '/model/' + model + '/' + clazz + '.java' );
    }
}

function copyRestFeature( ctx, condition )
{
    if( condition )
    {
        copyPolygeneBootstrap( ctx, "domain", "SecurityModule", true );

        copyTemplate( ctx,
                      'RestApiModule/SimpleEnroler.tmpl',
                      'rest/src/main/java/' + polygene.javaPackageDir + '/rest/security/SimpleEnroler.java' );

        copyTemplate( ctx,
                      'RestApiModule/SimpleVerifier.tmpl',
                      'rest/src/main/java/' + polygene.javaPackageDir + '/rest/security/SimpleVerifier.java' );

        copyTemplate( ctx,
                      'RestApiModule/HardcodedSecurityRepositoryMixin.tmpl',
                      'model/src/main/java/' + polygene.javaPackageDir + '/model/security/HardcodedSecurityRepositoryMixin.java' );
    }
}

function copyHeroesSampleApp( ctx )
{
    copyPolygeneDomain( ctx, "heroes", "Heroes", "Hero", hasFeature( 'sample (heroes) web application' ) );
    copyPolygeneApp( ctx, "Heroes", hasFeature( 'sample (heroes) web application' ) );
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
            packageName: polygene.packagename,
            hasFeature: hasFeature,
            hasEntityStore: hasEntityStore,
            hasIndexing: hasIndexing,
            hasCaching: hasCaching,
            polygene: polygene
        }
    );
}

function hasEntityStore( esType )
{
    return polygene.entitystore === esType;
}

function hasIndexing( indexingType )
{
    return polygene.indexing === indexingType;
}

function hasCaching( cachingType )
{
    return polygene.caching === cachingType;
}

function hasSerialization( serializer )
{
    return polygene.serialization === serializer;
}

function hasFeature( feature )
{
    return polygene.features.indexOf( feature ) >= 0;
}

function firstUpper( text )
{
    return text.charAt( 0 ).toUpperCase() + text.substring( 1 );
}
