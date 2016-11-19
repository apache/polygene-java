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
package org.apache.zest.gradle

import groovy.transform.CompileStatic
import org.apache.zest.gradle.dependencies.DependenciesPlugin
import org.apache.zest.gradle.publish.PublishingPlugin
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.external.javadoc.StandardJavadocDocletOptions
import org.nosphere.honker.gradle.HonkerExtension
import org.nosphere.honker.gradle.HonkerGenDependenciesTask
import org.nosphere.honker.gradle.HonkerGenLicenseTask
import org.nosphere.honker.gradle.HonkerGenNoticeTask
import org.nosphere.honker.gradle.HonkerLicenseOverrideCandidate

@CompileStatic
class AllProjectsPlugin implements Plugin<Project>
{
  @Override
  void apply( final Project project )
  {
    project.defaultTasks = [ 'classes', 'test' ]
    project.group = project.name == 'org.apache.zest' ?
                    'org.apache.zest' :
                    project.name.substring( 0, project.name.lastIndexOf( '.' ) )

    applyDefaultVersion( project )
    applyZestExtension( project )

    configureJava( project )
    project.plugins.apply DependenciesPlugin
    configureJavadoc( project )
    configureTest( project )
    if( CodeProjectsPlugin.isCodeProject( project ) )
    {
      project.plugins.apply CodeProjectsPlugin
    }
    configureDependencyReport( project )
    configureHonker( project )
    project.plugins.apply PublishingPlugin
  }

  private static void applyDefaultVersion( Project project )
  {
    if( project.version == 'unspecified' )
    {
      project.version = System.properties.version ?: '0'
    }
  }

  private static void applyZestExtension( Project project )
  {
    project.extensions.create( "zest", ZestExtension, project )
  }

  private static void configureJava( Project project )
  {
    project.plugins.apply 'java'
    def javaConvention = project.convention.getPlugin( JavaPluginConvention )
    javaConvention.targetCompatibility = JavaVersion.VERSION_1_8
    javaConvention.sourceCompatibility = JavaVersion.VERSION_1_8
    project.tasks.withType( JavaCompile ) { JavaCompile task ->
      task.options.encoding = 'UTF-8'
      // Deprecation warnings for all compilations
      task.options.compilerArgs << "-Xlint:deprecation"
      // Unchecked warnings for non-test core compilations
      if( 'org.apache.zest.core' == project.group && !task.name.toLowerCase( Locale.US ).contains( 'test' ) )
      {
        task.options.compilerArgs << "-Xlint:unchecked"
      }
    }
  }

  private static void configureJavadoc( Project project )
  {
    project.tasks.withType( Javadoc ) { Javadoc task ->
      def options = task.options as StandardJavadocDocletOptions
      options.encoding = 'UTF-8'
      options.docEncoding = 'UTF-8'
      options.charSet = 'UTF-8'
      options.noTimestamp = true
      options.links = [
        'http://docs.oracle.com/javase/8/docs/api/',
        'https://stleary.github.io/JSON-java/',
        'http://junit.org/junit4/javadoc/latest/'
      ]
      // exclude '**/internal/**'
    }
  }

  private static void configureTest( Project project )
  {
    def test = project.tasks.getByName( 'test' ) as Test
    test.onlyIf { !project.hasProperty( 'skipTests' ) }
    test.testLogging.info.exceptionFormat = TestExceptionFormat.FULL
    test.maxHeapSize = '1g'
    test.systemProperties = [ 'proxySet' : System.properties[ 'proxySet' ],
                              'proxyHost': System.properties[ 'proxyHost' ],
                              'proxyPort': System.properties[ 'proxyPort' ] ]
    test.systemProperties[ 'user.dir' ] = test.workingDir
    test.reports.html.enabled = true
  }

  // Dependency Report generate only the runtime configuration
  // The report is packaged in the SDK distributions
  private static void configureDependencyReport( Project project )
  {
    project.plugins.apply 'project-report'
    // TODO Fails with no task found
    // def dependencyReport = project.tasks.getByName( 'dependencyReport' ) as DependencyReportTask
    // dependencyReport.configurations = [ project.configurations.getByName( 'runtime' ) ] as Set
  }

  private static void configureHonker( Project project )
  {
    project.plugins.apply 'org.nosphere.honker'
    def honkerGenDependencies = project.tasks.getByName( 'honkerGenDependencies' ) as HonkerGenDependenciesTask
    def honkerGenLicense = project.tasks.getByName( 'honkerGenLicense' ) as HonkerGenLicenseTask
    def honkerGenNotice = project.tasks.getByName( 'honkerGenNotice' ) as HonkerGenNoticeTask
    def javaConvention = project.convention.getPlugin( JavaPluginConvention )
    def mainSourceSet = javaConvention.sourceSets.getByName( 'main' )
    mainSourceSet.output.dir( [ builtBy: honkerGenDependencies ] as Map<String, Object>,
                              honkerGenDependencies.outputDir )
    mainSourceSet.output.dir( [ builtBy: honkerGenLicense ] as Map<String, Object>,
                              honkerGenLicense.outputDir )
    mainSourceSet.output.dir( [ builtBy: honkerGenNotice ] as Map<String, Object>,
                              honkerGenNotice.outputDir )
    def honker = project.extensions.getByType( HonkerExtension )
    // Project License, applied to all submodules
    honker.license 'Apache 2'
    // Dependencies (transitive or not) with no license information, overriding them
    honker.licenseOverride { HonkerLicenseOverrideCandidate candidate ->
      if( candidate.group == 'asm' || candidate.module == 'prefuse-core' )
      {
        candidate.license = 'BSD 3-Clause'
      }
      if( candidate.group == 'javax.websocket' )
      {
        candidate.license = 'CDDL'
      }
      if( candidate.group == 'org.apache.httpcomponents'
        || candidate.group == 'net.java.dev.jna'
        || candidate.group == 'lucene'
        || candidate.group == 'jdbm'
        || candidate.group == 'org.osgi'
        || candidate.group.startsWith( 'org.restlet' ) )
      {
        candidate.license = 'Apache 2'
      }
    }
    honkerGenNotice.header = 'Apache Zest'
    honkerGenNotice.footer = 'This product includes software developed at\n' +
                             'The Apache Software Foundation (http://www.apache.org/).\n'
    project.tasks.getByName( 'check' ).dependsOn project.tasks.getByName( 'honkerCheck' )
  }
}
