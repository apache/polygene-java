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
import org.apache.rat.gradle.RatTask
import org.apache.polygene.gradle.dependencies.DependenciesDeclarationExtension
import org.apache.polygene.gradle.dist.DistributionPlugin
import org.apache.polygene.gradle.release.ReleaseSpecExtension
import org.apache.polygene.gradle.release.ReleaseSpecPlugin
import org.apache.polygene.gradle.test.AggregatedJacocoReportTask
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.TestReport
import org.gradle.external.javadoc.StandardJavadocDocletOptions

@CompileStatic
class RootProjectPlugin implements Plugin<Project>
{
  static final String PROJECT_TITLE = 'Apache Polygene™ (Java Edition) SDK'
  static final String PROJECT_DESCRIPTION = 'Apache Polygene™ (Java Edition) is a framework for domain centric ' +
                                            'application development, including evolved concepts from AOP, DI and DDD.'

  static class TaskNames
  {
    static final String GO_OFFLINE = 'goOffline'
    static final String GLOBAL_TEST_REPORT = 'globalTestReport'
    static final String JAVADOCS = 'javadocs'
    static final String ARCHIVE_JAVADOCS = 'archiveJavadocs'
    static final String BUILD_ALL = 'buildAll'
  }

  @Override
  void apply( Project project )
  {
    project.plugins.apply ReleaseSpecPlugin

    applyProjectMetadata( project )
    applyHelperTasks( project )
    applyPlugins( project )

    configureJacoco( project )
    configureTestReport( project )
    configureJavadocs( project )
    configureRat( project )

    project.plugins.apply DistributionPlugin
    configureReleaseTask( project )
  }

  private static void applyProjectMetadata( Project project )
  {
    def extraProperties = project.extensions.extraProperties
    extraProperties.set 'title', PROJECT_TITLE
    extraProperties.set 'description', PROJECT_DESCRIPTION
  }

  private static void applyHelperTasks( Project project )
  {
    project.tasks.create( TaskNames.GO_OFFLINE ) { Task task ->
      task.group = TaskGroups.HELP
      task.description = 'Resolves all dependencies configuration'
      task.doLast {
        def allConfigurations = project.allprojects.collect { Project each ->
          each.configurations
        }.flatten() as Set<Configuration>
        allConfigurations*.resolvedConfiguration
      }
    }
    def buildAll = project.tasks.create( TaskNames.BUILD_ALL )
    buildAll.group = TaskGroups.BUILD
    buildAll.description = 'Builds all'
    buildAll.dependsOn 'javadocs', 'check', 'jar',
                       project.subprojects.collect { p -> p.tasks.getByName( 'dependencyReport' ) },
                       project.subprojects.collect { p -> p.tasks.getByName( 'assemble' ) },
                       ':org.apache.polygene.manual:website'
  }

  private static void applyPlugins( Project project )
  {
    project.plugins.apply 'org.nosphere.apache.rat'
  }

  private static void configureJacoco( Project project )
  {
    def dependencies = project.rootProject.extensions.getByType( DependenciesDeclarationExtension )
    project.configurations.create( 'jacoco' )
    project.dependencies.add( 'jacoco', "org.jacoco:org.jacoco.ant:${ dependencies.buildToolsVersions.jacoco }" )
    def task = project.tasks.create( 'coverageReport', AggregatedJacocoReportTask ) { AggregatedJacocoReportTask task ->
      task.group = TaskGroups.VERIFICATION
      task.description = 'Generates global coverage report'
      task.dependsOn project.subprojects.collect( { Project p -> p.tasks.getByName( 'test' ) } )
    }
    project.tasks.getByName( 'check' ).dependsOn task
  }

  private static void configureTestReport( Project project )
  {
    project.tasks.create( TaskNames.GLOBAL_TEST_REPORT, TestReport ) { TestReport task ->
      task.group = TaskGroups.VERIFICATION
      task.description = 'Generates global test report'
      task.destinationDir = project.file( "$project.buildDir/reports/tests" )
      task.reportOn project.subprojects.collect { it.tasks.getByName( 'test' ) }
    }
    def test = project.tasks.getByName( 'test' ) as Test
    test.dependsOn project.subprojects.collect { it.tasks.getByName( 'test' ) }
    test.dependsOn project.tasks.getByName( TaskNames.GLOBAL_TEST_REPORT )
    test.reports.html.enabled = false
  }

  private static void configureJavadocs( Project project )
  {
    def polygene = project.extensions.getByType( PolygeneExtension )
    def releaseSpec = project.extensions.getByType( ReleaseSpecExtension )
    project.tasks.create( TaskNames.JAVADOCS, Javadoc ) { Javadoc task ->
      task.group = TaskGroups.DOCUMENTATION
      task.description = 'Builds the whole SDK public Javadoc'
      task.dependsOn ReleaseSpecPlugin.TaskNames.RELEASE_APPROVED_PROJECTS
      def options = task.options as StandardJavadocDocletOptions
      options.docFilesSubDirs = true
      options.encoding = "UTF-8"
      options.overview = "${ project.projectDir }/src/javadoc/overview.html"
      task.title = "${ PROJECT_TITLE } ${ project.version }"
      def apiSources = releaseSpec.approvedProjects.findAll { approved ->
        ( approved.name.startsWith( 'org.apache.polygene.core' ) &&
          !approved.name.startsWith( 'org.apache.polygene.core.runtime' ) ) ||
        approved.name.startsWith( 'org.apache.polygene.library' ) ||
        approved.name.startsWith( 'org.apache.polygene.extension' ) ||
        approved.name.startsWith( 'org.apache.polygene.tool' )
      }
      task.source apiSources.collect { each ->
        each.convention.getPlugin( JavaPluginConvention ).sourceSets.getByName( 'main' ).allJava
      }
      task.destinationDir = project.file( "${ project.convention.getPlugin( JavaPluginConvention ).docsDir }/javadocs" )
      task.classpath = project.files( apiSources.collect { apiProject ->
        apiProject.convention.getPlugin( JavaPluginConvention ).sourceSets.getByName( 'main' ).compileClasspath
      } )
      options.group( [
        "Core API"      : [ "org.apache.polygene.api",
                            "org.apache.polygene.api.*" ],
        "Core Bootstrap": [ "org.apache.polygene.bootstrap",
                            "org.apache.polygene.bootstrap.*" ],
        "Core SPI"      : [ "org.apache.polygene.spi",
                            "org.apache.polygene.spi.*" ],
        "Libraries"     : [ "org.apache.polygene.library.*" ],
        "Extensions"    : [ "org.apache.polygene.valueserialization.*",
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
    }
    project.tasks.create( TaskNames.ARCHIVE_JAVADOCS, Copy ) { Copy task ->
      task.group = TaskGroups.DOCUMENTATION
      task.description = 'Copy SDK public Javadoc to ../polygene-web'
      task.dependsOn TaskNames.JAVADOCS
      task.from 'build/docs/javadoc/'
      if( polygene.developmentVersion )
      {
        task.into( "$project.projectDir/../polygene-web/site/content/java/develop/javadocs/" )
      }
      else
      {
        task.into( "$project.projectDir/../polygene-web/site/content/java/$project.version/javadocs/" )
      }
    }
  }

  private static void configureRat( Project project )
  {
    def rat = project.tasks.getByName( 'rat' ) as RatTask
    rat.group = TaskGroups.VERIFICATION
    rat.onlyIf { project.version != '0' }
    rat.excludes = [
      '**/.DS_Store/**', '**/._*',
      // Git Files
      '**/.git/**', '**/.gitignore',
      // Gradle Files
      'gradle/wrapper/**', '**/gradlew', '**/gradlew.bat', '**/.gradle/**',
      // Build Output
      '**/build/**', '**/derby.log', 'out/**',
      // IDE Files
      '**/.idea/**', '**/*.iml', '**/*.ipr', '**/*.iws',
      '**/.settings/**', '**/.classpath', '**/.project',
      '**/.gradletasknamecache', '**/private/cache/**',
      '**/.nb-gradle-properties', '**/.nb-gradle/**',
      // JSON files are not allowed to have comments, according to http://www.json.org/ and http://www.ietf.org/rfc/rfc4627.txt
      '**/*.json',
      // Various Text Resources
      '**/README.*', '**/README*.*', '**/TODO',
      '**/src/main/resources/**/*.txt',
      '**/src/test/resources/**/*.txt',
      'libraries/rest-server/src/main/resources/**/*.htm',
      'libraries/rest-server/src/main/resources/**/*.atom',
      'tools/qidea/src/main/resources/**/*.ft',
      'tools/qidea/src/main/resources/**/*.template',
      // Graphic Resources
      '**/*.svg', '**/*.gif', '**/*.png', '**/*.jpg', '**/*.psd',
      // Keystores
      '**/*.jceks',
      // Syntax Highlighter - MIT
      'manual/**/sh*.css', 'manual/**/sh*.js',
      // jQuery & plugins - MIT
      'manual/**/jquery*.js',
      // W3C XML Schemas - W3C Software License
      'samples/rental/src/main/resources/*.xsd',
      // Polygene Generator Heroes Templates - MIT
      'tools/generator-polygene/app/templates/Heroes/**',
      // templates that will become the user's source files, should not have license headers
      'tools/shell/src/dist/etc/templates/**',
    ]
  }

  private static void configureReleaseTask( Project project )
  {
    def polygene = project.extensions.getByType( PolygeneExtension )
    def release = project.tasks.create( 'release' )
    release.description = 'Builds, tests and uploads the release artifacts'
    release.group = TaskGroups.RELEASE
    release.doFirst {
      if( polygene.developmentVersion )
      {
        throw new GradleException( "Cannot release development version $project.version, use '-Dversion=X.Y.Z'" )
      }
    }
    release.dependsOn 'checkReleaseSpec',
                      'rat',
                      'archiveJavadocs',
                      ':org.apache.polygene.manual:copyWebsite',
                      project.allprojects.collect { it.tasks.getByName( 'uploadArchives' ) },
                      'dist'
  }
}
