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
package org.apache.polygene.gradle

import groovy.transform.CompileStatic
import org.apache.polygene.gradle.dependencies.DependenciesDeclarationExtension
import org.apache.polygene.gradle.doc.AsciidocBuildInfoPlugin
import org.apache.polygene.gradle.version.VersionClassPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.plugins.osgi.OsgiManifest
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.jvm.tasks.Jar
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
import org.gradle.testing.jacoco.tasks.JacocoReport

@CompileStatic
class CodeProjectsPlugin implements Plugin<Project>
{
  static boolean isCodeProject( Project project )
  {
    [ 'src/main/java', 'src/test/java',
      'src/main/groovy', 'src/test/groovy' ].collect { path ->
      new File( "$project.projectDir/$path" )
    }.any { dir -> dir.isDirectory() }
  }

  @Override
  void apply( final Project project )
  {
    project.plugins.apply VersionClassPlugin
    project.plugins.apply AsciidocBuildInfoPlugin

    configureJar( project )
    configureSupplementaryArchives( project )

    configureJacoco( project )
    configureCheckstyle( project )
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

  private static void configureSupplementaryArchives( Project project )
  {
    def javaConvention = project.convention.getPlugin( JavaPluginConvention )
    def sourceJar = project.tasks.create( 'sourceJar', Jar ) { Jar task ->
      task.description = 'Builds -sources.jar'
      task.classifier = 'sources'
      task.from javaConvention.sourceSets.getByName( 'main' ).allSource
    }
    def testSourceJar = project.tasks.create( 'testSourceJar', Jar ) { Jar task ->
      task.description = 'Builds -testsources.jar'
      task.classifier = 'testsources'
      task.from javaConvention.sourceSets.getByName( 'test' ).allSource
    }
    def javadoc = project.tasks.getByName( 'javadoc' ) as Javadoc
    def javadocJar = project.tasks.create( 'javadocJar', Jar ) { Jar task ->
      task.description = 'Builds -javadoc.jar'
      task.classifier = 'javadoc'
      task.from javadoc.destinationDir
      task.dependsOn javadoc
    }
    project.artifacts.add( 'archives', sourceJar )
    project.artifacts.add( 'archives', testSourceJar )
    project.artifacts.add( 'archives', javadocJar )
  }

  private static void configureJacoco( Project project )
  {
    def dependencies = project.rootProject.extensions.getByType( DependenciesDeclarationExtension )
    project.plugins.apply 'jacoco'
    def jacoco = project.extensions.getByType( JacocoPluginExtension )
    jacoco.toolVersion = dependencies.buildToolsVersions.jacoco
    project.tasks.withType( JacocoReport ) { JacocoReport task ->
      task.group = TaskGroups.VERIFICATION
      task.description = 'Generates test coverage report.'
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
