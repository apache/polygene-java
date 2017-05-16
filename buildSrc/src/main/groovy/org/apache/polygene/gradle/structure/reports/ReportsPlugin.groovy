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
package org.apache.polygene.gradle.structure.reports

import groovy.transform.CompileStatic
import org.apache.polygene.gradle.BasePlugin
import org.apache.polygene.gradle.TaskGroups
import org.apache.polygene.gradle.code.CodePlugin
import org.apache.polygene.gradle.code.PublishedCodePlugin
import org.apache.polygene.gradle.dependencies.DependenciesDeclarationExtension
import org.apache.polygene.gradle.dependencies.DependenciesPlugin
import org.apache.polygene.gradle.structure.RootPlugin
import org.apache.polygene.gradle.structure.release.ReleaseSpecExtension
import org.apache.polygene.gradle.structure.release.ReleaseSpecPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.plugins.ReportingBasePlugin
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.api.tasks.testing.TestReport
import org.gradle.external.javadoc.StandardJavadocDocletOptions
import org.gradle.language.base.plugins.LifecycleBasePlugin

// TODO Expose project output into configurations
@CompileStatic
class ReportsPlugin implements Plugin<Project>
{
  static class TaskNames
  {
    static final String COVERAGE = 'coverage'
    static final String TEST = 'test'
    static final String JAVADOCS = 'javadocs'
  }

  @Override
  void apply( Project project )
  {
    project.plugins.apply BasePlugin
    project.plugins.apply org.gradle.api.plugins.BasePlugin
    project.plugins.apply ReportingBasePlugin
    project.plugins.apply DependenciesPlugin
    applyTest project
    applyCoverage project
    applyJavadocs project
    PublishedCodePlugin.configureJavadoc project
  }

  private static void applyTest( Project project )
  {
    def task = project.tasks.create( TaskNames.TEST, TestReport ) { TestReport task ->
      task.group = TaskGroups.VERIFICATION
      task.description = 'Generates global test report'
      task.destinationDir = project.file( "$project.buildDir/reports/tests" )
      task.reportOn {
        project.rootProject.subprojects
               .findAll { p -> p.plugins.hasPlugin CodePlugin }
               .collect { it.tasks.getByName( 'test' ) }
      }
    }
    project.tasks.getByName( LifecycleBasePlugin.CHECK_TASK_NAME ).dependsOn task
  }

  private static void applyCoverage( Project project )
  {
    def dependencies = project.rootProject.extensions.getByType DependenciesDeclarationExtension
    project.configurations.create AggregatedJacocoReportTask.JACOCO_CONFIGURATION
    project.dependencies.add AggregatedJacocoReportTask.JACOCO_CONFIGURATION,
                             "org.jacoco:org.jacoco.ant:${ dependencies.buildToolsVersions.jacoco }"
    def task = project.tasks.create( TaskNames.COVERAGE, AggregatedJacocoReportTask )
      { AggregatedJacocoReportTask task ->
        task.group = TaskGroups.VERIFICATION
        task.description = 'Generates global coverage report'
        task.dependsOn {
          project.rootProject.subprojects
                 .findAll { p -> p.plugins.hasPlugin CodePlugin }
                 .collect( { p -> p.tasks.getByName JavaPlugin.TEST_TASK_NAME } )
        }
      }
    project.tasks.getByName( LifecycleBasePlugin.CHECK_TASK_NAME ).dependsOn task
  }

  private static void applyJavadocs( Project project )
  {
    def releaseSpec = project.extensions.getByType ReleaseSpecExtension
    def javadocsTask = project.tasks.create( TaskNames.JAVADOCS, Javadoc ) { Javadoc task ->
      task.onlyIf { !releaseSpec.developmentVersion }
      task.group = TaskGroups.DOCUMENTATION
      task.description = 'Builds the whole SDK public Javadoc'
      task.dependsOn { project.rootProject.tasks.getByName ReleaseSpecPlugin.TaskNames.RELEASE_APPROVED_PROJECTS }
      task.destinationDir = project.file "$project.buildDir/docs/javadocs"
      def options = task.options as StandardJavadocDocletOptions
      options.docFilesSubDirs = true
      options.encoding = 'UTF-8'
      options.docEncoding = 'UTF-8'
      options.charSet = 'UTF-8'
      options.noTimestamp = true
      options.overview = "${ project.projectDir }/src/javadoc/overview.html"
      task.title = "${ RootPlugin.PROJECT_TITLE } ${ project.version }"
      options.group( [
        "Core API"      : [ "org.apache.polygene.api",
                            "org.apache.polygene.api.*" ],
        "Core Bootstrap": [ "org.apache.polygene.bootstrap",
                            "org.apache.polygene.bootstrap.*" ],
        "Core SPI"      : [ "org.apache.polygene.spi",
                            "org.apache.polygene.spi.*" ],
        "Libraries"     : [ "org.apache.polygene.library.*" ],
        "Extensions"    : [ "org.apache.polygene.serialization.*",
                            "org.apache.polygene.entitystore.*",
                            "org.apache.polygene.index.*",
                            "org.apache.polygene.metrics.*",
                            "org.apache.polygene.cache.*",
                            "org.apache.polygene.migration",
                            "org.apache.polygene.migration.*" ],
        "Tools"         : [ "org.apache.polygene.tools.*",
                            "org.apache.polygene.envisage",
                            "org.apache.polygene.envisage.*" ],
        "Test Support"  : [ "org.apache.polygene.test",
                            "org.apache.polygene.test.*" ]
      ] )
      options.links = [
        'http://docs.oracle.com/javase/8/docs/api/',
        'https://stleary.github.io/JSON-java/',
        'http://junit.org/junit4/javadoc/latest/'
      ]
    }
    project.tasks.getByName( LifecycleBasePlugin.CHECK_TASK_NAME ).dependsOn javadocsTask
    project.tasks.withType( Javadoc ) { Javadoc task ->
      def apiSources = releaseSpec.publishedProjects.findAll { approved ->
        ( approved.path.startsWith( ':core' ) && !approved.path.startsWith( ':core:runtime' ) ) ||
        approved.path.startsWith( ':libraries' ) ||
        approved.path.startsWith( ':extensions' ) ||
        approved.path.startsWith( ':tools' )
      }
      apiSources.each { Project apiProject ->
        apiProject.afterEvaluate { Project evaluatedApiProject ->
          def javaConvention = evaluatedApiProject.convention.findPlugin( JavaPluginConvention )
          if(javaConvention) {
              def mainSourceSet = javaConvention.sourceSets.getByName( 'main' )
              task.source mainSourceSet.allJava
              task.classpath += mainSourceSet.compileClasspath
          }
        }
      }
    }
  }
}
