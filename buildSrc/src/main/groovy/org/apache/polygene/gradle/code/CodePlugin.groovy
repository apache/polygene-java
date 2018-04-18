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
package org.apache.polygene.gradle.code

import groovy.transform.CompileStatic
import org.apache.polygene.gradle.BasePlugin
import org.apache.polygene.gradle.dependencies.DependenciesDeclarationExtension
import org.apache.polygene.gradle.dependencies.DependenciesPlugin
import org.gradle.api.*
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.plugins.osgi.OsgiManifest
import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.jvm.tasks.Jar
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification
import org.gradle.testing.jacoco.tasks.JacocoReport

@CompileStatic
class CodePlugin implements Plugin<Project>
{
  public static final String DOCKER_DISABLED_EXTRA_PROPERTY = 'dockerDisabled'

  @Override
  void apply( Project project )
  {
    project.plugins.apply BasePlugin
    applyJava project
    project.plugins.apply DependenciesPlugin
    configureDefaultDependencies project
    configureTest project
    configureJar project
    configureArchivesBaseName project
    configureJacoco project
    configureCheckstyle project
  }

  private static void applyJava( Project project )
  {
    project.plugins.apply 'java-library'
    def javaConvention = project.convention.getPlugin JavaPluginConvention
    javaConvention.targetCompatibility = JavaVersion.VERSION_1_8
    javaConvention.sourceCompatibility = JavaVersion.VERSION_1_8
    project.tasks.withType( JavaCompile ) { JavaCompile task ->
      task.options.encoding = 'UTF-8'
      task.options.compilerArgs << '-Xlint:deprecation'
      task.options.incremental = true
    }
  }

  private static void configureDefaultDependencies( Project project )
  {
    def declaration = project.rootProject.extensions.getByType DependenciesDeclarationExtension
    declaration.defaultDependencies.each { String configuration, List<Object> dependencies ->
      dependencies.each { dependency ->
        if( dependency instanceof Collection )
        {
          dependency.each { subdep ->
            project.dependencies.add configuration, subdep
          }
        }
        else
        {
          project.dependencies.add configuration, dependency
        }
      }
    }
    def junitEngine = declaration.libraries.get("junit_engine")
    project.dependencies.add "testRuntime", junitEngine
  }

  private static void configureTest( Project project )
  {
    // Match --max-workers and Test maxParallelForks, use 1 if parallel is disabled
    def parallel = project.gradle.startParameter.parallelProjectExecutionEnabled
    def maxTestWorkers = ( parallel ? project.gradle.startParameter.maxWorkerCount : 1 ) as int
    // The space in the directory name is intentional
    def allTestsDir = project.file "$project.buildDir/tmp/test files"
    def testTasks = project.tasks.withType( Test ) { Test testTask ->
      testTask.onlyIf { !project.hasProperty( 'skipTests' ) }
      testTask.testLogging.info.exceptionFormat = TestExceptionFormat.FULL
      testTask.maxHeapSize = '1g'
      testTask.maxParallelForks = maxTestWorkers
      testTask.systemProperties = [ 'proxySet' : System.properties[ 'proxySet' ],
                                    'proxyHost': System.properties[ 'proxyHost' ],
                                    'proxyPort': System.properties[ 'proxyPort' ] ]
      testTask.reports.html.enabled = true
      def testDir = new File( allTestsDir, testTask.name )
      def workDir = new File( testDir, 'work' )
      def tmpDir = new File( testDir, 'tmp' )
      def homeDir = new File( testDir, 'home' )
      testTask.useJUnitPlatform()
      testTask.workingDir = workDir
      testTask.systemProperties << ( [
        'user.dir'      : workDir.absolutePath,
        'java.io.tmpdir': tmpDir.absolutePath,
        'home.dir'      : homeDir.absolutePath
      ] as Map<String, Object> )
      testTask.environment << ( [
        'HOME'       : homeDir.absolutePath,
        'USERPROFILE': homeDir.absolutePath
      ] as Map<String, Object> )
      testTask.doFirst { Test task ->
        [ workDir, tmpDir, homeDir ]*.mkdirs()
      }
      testTask.doLast { Test task ->
        if( !task.state.failure )
        {
          project.delete testDir
        }
      }
    }
    // Configuration task to honor disabling Docker when unavailable
    project.tasks.create( 'configureDockerForTest', { Task task ->
      // TODO Untangle docker connectivity check & test task configuration
      task.dependsOn ':internals:testsupport-internal:checkDockerConnectivity'
      testTasks.each { it.dependsOn task }
      task.inputs.property 'polygeneTestSupportDockerDisabled',
                           { project.findProperty( DOCKER_DISABLED_EXTRA_PROPERTY ) }
      task.doLast {
        boolean dockerDisabled = project.findProperty( DOCKER_DISABLED_EXTRA_PROPERTY )
        testTasks.each { testTask ->
          testTask.inputs.property 'polygeneTestSupportDockerDisabled', dockerDisabled
          testTask.systemProperty 'DOCKER_DISABLED', dockerDisabled
        }
      }
    } as Action<Task> )
  }

  private static void configureJar( Project project )
  {
    project.plugins.apply 'osgi'
    def jar = project.tasks.getByName( 'jar' ) as Jar
    def manifest = jar.manifest as OsgiManifest
    manifest.attributes( [
      license    : 'http://www.apache.org/licenses/LICENSE-2.0.txt',
      docURL     : 'https://polygene.apache.org/',
      description: project.description ?:
                   'Apache Polygene™ (Java Edition) is a platform for Composite Oriented Programming',
      vendor     : 'The Apache Software Foundation, https://www.apache.org',
    ] )
    manifest.instruction '-debug', 'true'
  }


  private static void configureArchivesBaseName( Project project )
  {
    def publishedName = PublishNaming.publishedNameFor( project.path )
    project.tasks.withType( AbstractArchiveTask ) { AbstractArchiveTask task ->
      task.baseName = publishedName
    }
  }

  private static void configureJacoco( Project project )
  {
    def dependencies = project.rootProject.extensions.getByType DependenciesDeclarationExtension
    project.plugins.apply 'jacoco'
    def jacoco = project.extensions.getByType JacocoPluginExtension
    jacoco.toolVersion = dependencies.buildToolsVersions.jacoco
    project.tasks.withType( JacocoReport ) { Task task ->
      task.group = null
    }
    project.tasks.withType( JacocoCoverageVerification ) { Task task ->
      task.group = null
    }
  }

  private static void configureCheckstyle( Project project )
  {
    // project.plugins.apply 'checkstyle'
    //    if( name == "org.apache.polygene.core.runtime" )
    //    {
    //      checkstyleMain {
    //        configFile = new File( "$rootProject.projectDir.absolutePath/etc/polygene-runtime-checkstyle.xml" )
    //        ignoreFailures = true
    //      }
    //    }
    //    else
    //    {
    //      checkstyleMain {
    //        configFile = new File( rootProject.projectDir.absolutePath.toString() + '/etc/polygene-api-checkstyle.xml' )
    //        ignoreFailures = true
    //        reporting.baseDir = "$rootProject.reporting.baseDir/checkstyle"
    //      }
    //    }
    //    checkstyleTest {
    //      configFile = new File( "$rootProject.projectDir.absolutePath/etc/polygene-tests-checkstyle.xml" )
    //      ignoreFailures = true
    //    }
    //
    //    checkstyleVersion {
    //      configFile = new File( "$rootProject.projectDir.absolutePath/etc/polygene-tests-checkstyle.xml" )
    //      ignoreFailures = true
    //    }
    //    // Create checkstyle report
    //    task checkstyleReport( type: XsltTask, dependsOn: check ) {
    //      source project.checkstyle.reportsDir
    //      include '*.xml'
    //      destDir = file( "build/reports/checkstyle/" )
    //      extension = 'html'
    //      stylesheetFile = file( "$rootProject.projectDir/etc/checkstyle-noframes.xsl" )
    //    }
    //
  }
}
