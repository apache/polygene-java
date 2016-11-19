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
 */
package org.apache.zest.gradle.dependencies

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.DependencySubstitution
import org.gradle.api.artifacts.component.ModuleComponentSelector
import org.gradle.api.artifacts.repositories.MavenArtifactRepository

@CompileStatic
class DependenciesPlugin implements Plugin<Project>
{
  static final Map<String, String> REPOSITORIES_URLS = [
    mavenCentral: "https://repo1.maven.org/maven2/",
    ops4j       : "http://repository.ops4j.org/maven2/",
    restlet     : 'https://maven.restlet.com/',
    clojars     : "https://clojars.org/repo/",
  ]

  static final def asmVersion = '5.1'
  static final def bonecpVersion = '0.8.0.RELEASE'
  static final def bouncyVersion = '1.55'
  static final def codahaleMetricsVersion = '3.1.2'
  static final def commonsDbcpVersion = '2.1.1'
  static final def commonsLangVersion = '3.5'
  static final def derbyVersion = '10.13.1.1'
  static final def dnsJavaVersion = '2.1.7'
  static final def ehcacheVersion = '3.1.3'
  static final def elasticsearchVersion = '5.0.0'
  static final def freemarkerVersion = '2.3.25-incubating'
  static final def geodeVersion = '1.0.0-incubating'
  static final def groovyVersion = '2.4.7'
  static final def h2Version = '1.4.193'
  static final def hazelcastVersion = '3.7.2'
  static final def hamcrestVersion = '1.3'
  static final def httpClientVersion = '4.5.2'
  static final def jacksonVersion = '2.8.4'
  static final def javascriptVersion = '1.7.7.1'
  static final def javasqlgeneratorVersion = '0.3.2'
  static final def jcloudsVersion = '1.9.2'
  static final def jdbmVersion = '2.4'
  static final def jedisVersion = '2.9.0'
  static final def jettyVersion = '9.2.17.v20160517' // 9.3.x Tests fail!
  static final def jgoodiesLooksVersion = '2.7.0'
  static final def jtaVersion = '1.1'
  static final def leveldbVersion = '0.9'
  static final def leveldbJniVersion = '1.8'
  static final def liquibaseVersion = '3.5.3'
  static final def logbackVersion = '1.1.7'
  static final def mongodbVersion = '3.3.0'
  static final def mysqlVersion = '6.0.4'
  static final def orgJsonVersion = '20130213'
  static final def osgiVersion = '4.2.0' // 4.3.0 Fails to compile! - 5.0.0 exists
  static final def pdfboxVersion = '2.0.3'
  static final def postgresqlVersion = '9.4.1211'
  static final def prefuseVersion = '1.0.1'
  static final def restletVersion = '2.3.7'
  static final def rdfVersion = '2.7.16' // 2.8.x change query results!! 4.x exists
  static final def riakVersion = '2.0.8'
  static final def servletVersion = '3.1.0'
  static final def shiroVersion = '1.3.2'
  static final def skedVersion = '2.1'
  static final def slf4jVersion = '1.7.21'
  static final def solrVersion = "1.4.1" // 4.x Fails to compile!
  static final def springVersion = '4.3.3.RELEASE'
  static final def spymemcachedVersion = '2.12.1'
  static final def sqliteVersion = '3.14.2.1'
  static final def velocityVersion = '1.7'
  static final def woodstoxVersion = '4.4.1'

  static final def antVersion = '1.9.7'
  static final def awaitilityVersion = '2.0.0'
  static final def easyMockVersion = '3.4'
  static final def junitVersion = '4.12'
  static final def mockitoVersion = '2.2.9'

  static final def libraries = [
    // Ant
    ant                 : "org.apache.ant:ant:$antVersion",
    ant_junit           : "org.apache.ant:ant-junit:$antVersion",

    // ASM
    asm                 : "org.ow2.asm:asm:$asmVersion",
    asm_util            : "org.ow2.asm:asm-util:$asmVersion",
    asm_commons         : "org.ow2.asm:asm-commons:$asmVersion",

    // OSGi
    osgi_core           : "org.osgi:org.osgi.core:$osgiVersion",
    osgi_compendium     : "org.osgi:org.osgi.compendium:$osgiVersion",
    osgi_enterprise     : "org.osgi:org.osgi.enterprise:$osgiVersion",

    // logging
    slf4j_api           : "org.slf4j:slf4j-api:$slf4jVersion",
    slf4j_simple        : "org.slf4j:slf4j-simple:$slf4jVersion",
    logback             : 'ch.qos.logback:logback-classic:' + logbackVersion,
    jcl_slf4j           : "org.slf4j:jcl-over-slf4j:$slf4jVersion",
    jcl_api             : 'commons-logging:commons-logging-api:99.0-does-not-exist',  //ensure it is never used.
    jcl                 : 'commons-logging:commons-logging:99.0-does-not-exist',  // ensure it is never used.

    // org.json
    org_json            : "org.codeartisans:org.json:$orgJsonVersion",

    // Restlet
    restlet             : [ "org.restlet.jee:org.restlet:$restletVersion",
                            "org.restlet.jee:org.restlet.ext.atom:$restletVersion",
                            "org.restlet.jee:org.restlet.ext.servlet:$restletVersion",
                            "org.restlet.jee:org.restlet.ext.slf4j:$restletVersion" ],

    // Spring
    spring_core         : [ "org.springframework:spring-beans:$springVersion",
                            "org.springframework:spring-context:$springVersion" ],

    spring_testsupport  : "org.springframework:spring-test:$springVersion",

    // RDF
    sesame              : [ "org.openrdf.sesame:sesame-model:$rdfVersion",
                            "org.openrdf.sesame:sesame-queryparser-sparql:$rdfVersion",
                            "org.openrdf.sesame:sesame-repository-dataset:$rdfVersion",
                            "org.openrdf.sesame:sesame-repository-http:$rdfVersion",
                            "org.openrdf.sesame:sesame-rio-api:$rdfVersion",
                            "org.openrdf.sesame:sesame-rio-n3:$rdfVersion",
                            "org.openrdf.sesame:sesame-rio-ntriples:$rdfVersion",
                            "org.openrdf.sesame:sesame-rio-rdfxml:$rdfVersion",
                            "org.openrdf.sesame:sesame-rio-trig:$rdfVersion",
                            "org.openrdf.sesame:sesame-rio-trix:$rdfVersion",
                            "org.openrdf.sesame:sesame-rio-turtle:$rdfVersion",
                            "org.openrdf.sesame:sesame-sail-api:$rdfVersion",
                            "org.openrdf.sesame:sesame-sail-memory:$rdfVersion",
                            "org.openrdf.sesame:sesame-sail-nativerdf:$rdfVersion",
                            "org.openrdf.sesame:sesame-sail-rdbms:$rdfVersion" ],
    sparql              : [ "org.openrdf.sesame:sesame-queryresultio-sparqlxml:$rdfVersion",
                            "org.openrdf.sesame:sesame-queryresultio-sparqljson:$rdfVersion" ],

    // SOLR
    solr                : [ "org.apache.solr:solr-core:$solrVersion",
                            "org.apache.solr:solr-solrj:$solrVersion" ],

    // Jetty
    jetty_server        : "org.eclipse.jetty:jetty-server:$jettyVersion",
    jetty_webapp        : "org.eclipse.jetty:jetty-webapp:$jettyVersion",
    jetty_servlet       : "org.eclipse.jetty:jetty-servlet:$jettyVersion",
    jetty_http          : "org.eclipse.jetty:jetty-http:$jettyVersion",
    jetty_io            : "org.eclipse.jetty:jetty-io:$jettyVersion",
    jetty_jmx           : "org.eclipse.jetty:jetty-jmx:$jettyVersion",
    jetty_security      : "org.eclipse.jetty:jetty-security:$jettyVersion",
    jetty_jsp           : "org.eclipse.jetty:jetty-jsp:$jettyVersion",
    jetty_util          : "org.eclipse.jetty:jetty-util:$jettyVersion",
    jetty_continuation  : "org.eclipse.jetty:jetty-continuation:$jettyVersion",
    jetty_client        : "org.eclipse.jetty:jetty-client:$jettyVersion",
    jetty_xml           : "org.eclipse.jetty:jetty-xml:$jettyVersion",

    // Scripting
    groovy              : "org.codehaus.groovy:groovy-all:$groovyVersion",

    javascript          : "org.mozilla:rhino:$javascriptVersion",

    // Library & Extension dependencies
    jackson_mapper      : "com.fasterxml.jackson.core:jackson-databind:$jacksonVersion",
    ehcache             : "org.ehcache:ehcache:$ehcacheVersion",
    elasticsearch       : [ "org.elasticsearch:elasticsearch:$elasticsearchVersion",
                            "org.elasticsearch.client:transport:$elasticsearchVersion",
                            // Elasticsearch 5.0 do not work with log4j 2.7
                            "org.apache.logging.log4j:log4j-api:2.6.2",
                            "org.apache.logging.log4j:log4j-core:2.6.2" ],
    geode               : "org.apache.geode:geode-core:$geodeVersion",
    h2                  : "com.h2database:h2:$h2Version",
    hazelcast           : "com.hazelcast:hazelcast:$hazelcastVersion",
    jclouds_core        : "org.apache.jclouds:jclouds-core:$jcloudsVersion",
    jclouds_blobstore   : "org.apache.jclouds:jclouds-allblobstore:$jcloudsVersion",
    jclouds_filesystem  : "org.apache.jclouds.api:filesystem:$jcloudsVersion",
    jdbm                : "jdbm:jdbm:$jdbmVersion",
    jedis               : "redis.clients:jedis:$jedisVersion",
    jgoodies_looks      : "com.jgoodies:jgoodies-looks:$jgoodiesLooksVersion",
    leveldb_api         : "org.iq80.leveldb:leveldb-api:$leveldbVersion",
    leveldb_java        : "org.iq80.leveldb:leveldb:$leveldbVersion",
    leveldb_jni_all     : "org.fusesource.leveldbjni:leveldbjni-all:$leveldbJniVersion",
    mongodb             : "org.mongodb:mongo-java-driver:$mongodbVersion",
    riak                : "com.basho.riak:riak-client:$riakVersion",
    jta                 : "javax.transaction:jta:$jtaVersion",
    javaSqlGenerator    : "org.java-sql-generator:org.java-sql-generator.api:$javasqlgeneratorVersion",
    javaSqlGeneratorImpl: "org.java-sql-generator:org.java-sql-generator.implementation:$javasqlgeneratorVersion",
    velocity            : "org.apache.velocity:velocity:$velocityVersion",
    commons_dbcp        : "org.apache.commons:commons-dbcp2:$commonsDbcpVersion",
    commons_lang        : "org.apache.commons:commons-lang3:$commonsLangVersion",
    servlet_api         : "javax.servlet:javax.servlet-api:$servletVersion",
    http_client         : "org.apache.httpcomponents:httpclient:$httpClientVersion",
    woodstox            : "org.codehaus.woodstox:woodstox-core-asl:$woodstoxVersion",
    restlet_xml         : "org.restlet.jee:org.restlet.ext.xml:$restletVersion",
    bouncy_castle       : "org.bouncycastle:bcprov-jdk15on:$bouncyVersion",
    dnsjava             : "dnsjava:dnsjava:$dnsJavaVersion",
    freemarker          : "org.freemarker:freemarker:$freemarkerVersion",
    shiro               : "org.apache.shiro:shiro-core:$shiroVersion",
    shiro_web           : "org.apache.shiro:shiro-web:$shiroVersion",
    bonecp              : "com.jolbox:bonecp:$bonecpVersion",
    liquibase           : "org.liquibase:liquibase-core:$liquibaseVersion",
    sked                : "org.codeartisans:sked:$skedVersion",
    pdfbox              : "org.apache.pdfbox:pdfbox:$pdfboxVersion",
    prefuse             : "de.sciss:prefuse-core:$prefuseVersion",
    spymemcached        : "net.spy:spymemcached:$spymemcachedVersion",
    codahale_metrics    : [ "io.dropwizard.metrics:metrics-core:$codahaleMetricsVersion",
                            "io.dropwizard.metrics:metrics-healthchecks:$codahaleMetricsVersion" ],

    // Testing
    junit               : "junit:junit:$junitVersion",
    hamcrest            : [ "org.hamcrest:hamcrest-core:$hamcrestVersion",
                            "org.hamcrest:hamcrest-library:$hamcrestVersion" ],
    awaitility          : "org.awaitility:awaitility:$awaitilityVersion",
    easymock            : "org.easymock:easymock:$easyMockVersion",
    mockito             : "org.mockito:mockito-core:$mockitoVersion",

    // Tests dependencies
    derby               : "org.apache.derby:derby:$derbyVersion",
    derbyclient         : "org.apache.derby:derbyclient:$derbyVersion",
    derbynet            : "org.apache.derby:derbynet:$derbyVersion",
    postgres            : "org.postgresql:postgresql:$postgresqlVersion",
    mysql_connector     : "mysql:mysql-connector-java:$mysqlVersion",
    sqlite              : "org.xerial:sqlite-jdbc:$sqliteVersion",
  ]

  static final Map<String, Object> defaultTestDependencies = [
    testCompile: [ libraries[ 'junit' ], libraries[ 'hamcrest' ], libraries[ 'ant' ], libraries[ 'ant_junit' ] ],
    testRuntime: [ libraries[ 'asm' ], libraries[ 'asm_commons' ], libraries[ 'asm_util' ] ]
  ] as Map

  @Override
  void apply( final Project project )
  {
    applyRepositories( project )
    applyLibraries( project )
    applyDependencyResolutionRules( project )
    applyDefaultTestDependencies( project )
  }

  private static void applyRepositories( Project project )
  {
    REPOSITORIES_URLS.each { name, url ->
      project.repositories.maven { MavenArtifactRepository repo ->
        repo.name = name
        repo.url = url
      }
    }
  }

  private static void applyLibraries( Project project )
  {
    if( project.rootProject == project )
    {
      project.extensions.extraProperties.set( 'libraries', libraries )
    }
  }

  private static void applyDependencyResolutionRules( Project project )
  {
    project.configurations.all(
      { Configuration configuration ->
        configuration.resolutionStrategy.dependencySubstitution.all( { DependencySubstitution dep ->
          if( dep.requested instanceof ModuleComponentSelector )
          {
            def requested = dep.requested as ModuleComponentSelector
            // Always resolve SLF4J to the same version
            if( requested.group == 'org.slf4j' )
            {
              dep.useTarget group: requested.group, name: requested.module, version: slf4jVersion
            }
            // Always resolve ASM to the same version
            if( requested.group == 'org.ow2.asm' )
            {
              dep.useTarget group: requested.group, name: requested.module, version: asmVersion
            }
            // Always resolve OSGi to the same version
            if( requested.group == 'org.osgi' )
            {
              dep.useTarget group: requested.group, name: requested.module, version: osgiVersion
            }
            // Always resolve Jackson to the same version
            if( requested.group.startsWith( 'com.fasterxml.jackson' ) && requested.module != 'jackson-parent' )
            {
              dep.useTarget group: requested.group, name: requested.module, version: jacksonVersion
            }
            // woodstox:wstx-asl is broken (no pom), use org.codehaus.woodstox:wstx-asl instead
            if( requested.group == 'woodstox' && requested.module == 'wstx-asl' )
            {
              dep.useTarget group: 'org.codehaus.woodstox', name: 'wstx-asl', version: requested.version
            }
            // some bad citizens have SNAPSHOT parents ...
            if( requested.module == 'commons-sandbox-parent' && requested.version == '3-SNAPSHOT' )
            {
              dep.useTarget group: requested.group, name: requested.module, version: '3'
            }
            // GSON 2.3 POM is invalid, use 2.3.1 instead .. see https://github.com/google/gson/issues/588
            if( requested.group == 'com.google.code.gson' && requested.module == 'gson' && requested.version == '2.3' )
            {
              dep.useTarget group: requested.group, name: requested.module, version: '2.3.1'
            }
            // Findbugs Annotation is LGPL, use https://github.com/stephenc/findbugs-annotations which is
            // Apache 2 licensed instead
            if( requested.group == 'net.sourceforge.findbugs' && requested.module == 'annotations' )
            {
              dep.useTarget group: 'com.github.stephenc.findbugs', name: 'findbugs-annotations', version: '1.3.9-1'
            }
          }
        } as Action<DependencySubstitution> )
      } as Action<Configuration> )
  }

  private static void applyDefaultTestDependencies( Project project )
  {
    defaultTestDependencies.each { String configuration, Object dependencies ->
      dependencies.each { dependency ->
        if( dependency instanceof Collection )
        {
          dependency.each { subdep ->
            project.dependencies.add( configuration, subdep )
          }
        }
        else
        {
          project.dependencies.add( configuration, dependency )
        }
      }
    }
  }
}
