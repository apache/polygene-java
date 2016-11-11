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
package org.apache.zest.gradle.dist

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.apache.rat.gradle.RatTask
import org.apache.tools.ant.filters.ReplaceTokens
import org.apache.zest.gradle.RootProjectPlugin
import org.apache.zest.gradle.dependencies.DependenciesPlugin
import org.apache.zest.gradle.release.ReleaseSpecExtension
import org.apache.zest.gradle.release.ReleaseSpecPlugin
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.CopySpec
import org.gradle.api.file.FileCopyDetails
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.GradleBuild
import org.gradle.api.tasks.bundling.Compression
import org.gradle.api.tasks.bundling.Tar
import org.gradle.api.tasks.bundling.Zip

@CompileStatic
class DistributionPlugin implements Plugin<Project>
{
  static class TaskGroups
  {
    static final String DISTRIBUTION = 'Distribution'
    static final String DISTRIBUTION_VERIFICATION = 'Distribution verification'
  }

  static class TaskNames
  {
    static final String UNPACK_SOURCE_DIST = 'unpackSrcDist'
    static final String UNPACK_BINARY_DIST = 'unpackBinDist'
    static final String CHECK_SOURCE_DIST = 'checkSrcDist'
    static final String CHECK_BINARY_DIST = 'checkBinDist'
    static final String CHECK_BINARY_DIST_RAT = 'checkBinDist_rat'
    static final String GENERATE_MAVEN_OFFLINE_HELPERS = 'generateMavenGoOfflineHelpers'
    static final String GENERATE_GRADLE_OFFLINE_HELPERS = 'generateGradleGoOfflineHelpers'
    static final String CHECK_MAVEN_OFFLINE_HELPERS = 'checkMavenGoOfflineHelpers'
    static final String CHECK_GRADLE_OFFLINE_HELPERS = 'checkGradleGoOfflineHelpers'
  }

  @Override
  void apply( final Project project )
  {
    configureSourceDistribution( project )
    configureBinaryDistribution( project )
    configureDistributionChecksums( project )
    configureHelperTasks( project )
  }

  private static void configureSourceDistribution( Project project )
  {
    def releaseSpec = project.extensions.getByType( ReleaseSpecExtension )
    def srcDistFilesCopySpec = project.copySpec { CopySpec spec ->
      spec.from '.'
      spec.include '*.txt'
      spec.include 'doap.rdf'
      spec.include '*.gradle'
      spec.include 'gradlew*'
      spec.include 'gradle/**'
      spec.include 'etc/**'
      spec.include 'buildSrc/**'
      spec.include 'src/**'
      releaseSpec.approvedProjects.each { p ->
        def relPath = new File( project.projectDir.toURI().relativize( p.projectDir.toURI() ).toString() )
        spec.include "$relPath/**"
      }
      spec.include 'manual/**'
      spec.include 'samples/**'
      spec.include 'tests/**'
      spec.include 'tutorials/**'
      spec.include 'tools/shell/**'
      // Filtered, see below
      spec.exclude 'settings.gradle'
      spec.exclude 'gradle.properties'
      // Excludes
      spec.exclude '**/build/**'             // Build output
      spec.exclude 'derby.log'               // Derby test garbage
      spec.exclude '**/*.iml'                // IDEA files
      spec.exclude '**/*.ipr'                // IDEA files
      spec.exclude '**/*.iws'                // IDEA files
      spec.exclude '**/.idea'                // IDEA files
      spec.exclude '**/out/**'               // IDEA build output
      spec.exclude '**/.classpath'           // Eclipse files
      spec.exclude '**/.project'             // Eclipse files
      spec.exclude '**/.settings'            // Eclipse files
      spec.exclude '**/.nb-gradle/**'        // Netbeans files
      spec.exclude '**/.nb-gradle*'          // Netbeans files
      spec.exclude '**/.git/**'              // Git directories
      spec.exclude '**/.git*'                // Git files
      spec.exclude '**/.gradle/**'           // Gradle management files
      spec.exclude '**/.gradletasknamecache' // Gradle cache
      spec.into '.'
    }
    def srcDistFilteredFilesTask = project.tasks.create( 'srcDistFilteredFiles' )
    // Generates various files for the source distribution
    // - settings.gradle
    // - gradle.properties to set version !
    def filteredDir = new File( "$project.buildDir/tmp/srcDistFilteredFiles" )
    srcDistFilteredFilesTask.outputs.file filteredDir
    srcDistFilteredFilesTask.doLast {
      // Settings
      def settingsFile = new File( filteredDir, 'settings.gradle' )
      settingsFile.parentFile.mkdirs()
      def filteredSettings = ''
      project.file( 'settings.gradle' ).readLines().each { line ->
        if( line.contains( '\'libraries:' ) || line.contains( '\'extensions:' ) || line.contains( '\'tools:' ) )
        {
          def accepted = false
          releaseSpec.approvedProjects.collect { it.projectDir }.each { acceptedProjectDir ->
            if( line.contains( "'${ acceptedProjectDir.parentFile.name }:${ acceptedProjectDir.name }'" ) )
            {
              accepted = true
            }
          }
          if( accepted )
          {
            filteredSettings += "$line\n"
          }
        }
        else
        {
          filteredSettings += "$line\n"
        }
      }
      settingsFile.text = filteredSettings
      // gradle.properties
      def gradlePropsFile = new File( filteredDir, 'gradle.properties' )
      gradlePropsFile.parentFile.mkdirs()
      gradlePropsFile.text = project.file( 'gradle.properties' ).text +
                             "\nskipSigning=true\nskipAsciidocIfAbsent=true\n\nversion=$project.version\n"
    }
    def srcDistFilteredFilesCopySpec = project.copySpec { CopySpec spec ->
      spec.from srcDistFilteredFilesTask
      spec.into '.'
    }
    def srcDistCopySpec = project.copySpec { CopySpec spec ->
      spec.into "apache-zest-java-$project.version-src"
      spec.with srcDistFilesCopySpec
      spec.with srcDistFilteredFilesCopySpec
    }

    def zipSources = project.tasks.create( 'zipSources', Zip ) { Zip task ->
      task.baseName = 'apache-zest-java'
      task.with srcDistCopySpec
      task.classifier = 'src'
    }
    def tarSources = project.tasks.create( 'tarSources', Tar ) { Tar task ->
      task.baseName = 'apache-zest-java'
      task.with srcDistCopySpec
      task.compression = Compression.GZIP
      task.classifier = 'src'
    }
    project.artifacts.add( 'archives', zipSources )
    project.artifacts.add( 'archives', tarSources )

    project.tasks.create( TaskNames.UNPACK_SOURCE_DIST, Copy ) { Copy task ->
      task.description = "Unpack the source distribution"
      task.group = TaskGroups.DISTRIBUTION
      task.with srcDistCopySpec
      task.into 'build/unpacked-distributions/src'
    }

    def unpackedSrcDistDir = project.file( "build/unpacked-distributions/src/apache-zest-java-$project.version-src" )
    project.tasks.create( TaskNames.CHECK_SOURCE_DIST, GradleBuild.class, { GradleBuild task ->
      task.description = "Check the source distribution by running the 'check' and 'assemble' tasks inside"
      task.group = TaskGroups.DISTRIBUTION_VERIFICATION
      task.dependsOn TaskNames.UNPACK_SOURCE_DIST
      task.buildFile = "$unpackedSrcDistDir/build.gradle"
      task.tasks = [ 'check', 'assemble' ]
    } as Action<GradleBuild> )
  }

  private static void configureBinaryDistribution( Project project )
  {
    configureGoOfflineHelpers( project )

    def releaseSpec = project.extensions.getByType( ReleaseSpecExtension )
    def reportsDistCopySpec = project.copySpec { CopySpec spec ->
      spec.from "$project.buildDir/reports"
      spec.into 'docs/reports'
    }
    def docsCopySpec = project.copySpec { CopySpec spec ->
      spec.from 'build/docs'
      spec.from 'manual/build/docs/website'
      spec.into 'docs'
    }
    def runtimeDependenciesListCopySpec = project.copySpec { CopySpec spec ->
      releaseSpec.approvedProjects.collect { p ->
        spec.into( 'libs/' ) { CopySpec sub ->
          sub.from "$p.buildDir/reports/project/dependencies.txt"
          sub.rename 'dependencies.txt', "${ p.name }-${ p.version }-runtime-deps.txt"
        }
      }
      spec.into( '.' ) { CopySpec sub ->
        sub.from project.tasks.getByName( TaskNames.GENERATE_MAVEN_OFFLINE_HELPERS ).outputs
        sub.from project.tasks.getByName( TaskNames.GENERATE_GRADLE_OFFLINE_HELPERS ).outputs
      }
    }
    def libsCopySpec = project.copySpec { CopySpec spec ->
      releaseSpec.approvedProjects.collect { proj ->
        spec.into( 'libs/' ) { CopySpec sub ->
          sub.from proj.configurations.getByName( 'archives' ).artifacts.files
          sub.exclude '**-testsources.jar'
          sub.exclude '**/*.asc'
        }
      }
    }
    def extraDistTextCopySpec = project.copySpec { CopySpec spec ->
      releaseSpec.approvedProjects.collect { p ->
        spec.from project.fileTree( dir: "$p.projectDir/src/dist/", include: '**', exclude: "**/*.jar*" )
        spec.eachFile { FileCopyDetails fcd ->
          fcd.filter( ReplaceTokens, tokens: [ version: project.version ] )
        }
      }
      spec.into '.'
    }
    def extraDistBinCopySpec = project.copySpec { CopySpec spec ->
      releaseSpec.approvedProjects.collect { p ->
        spec.from "$p.projectDir/src/dist/"
        spec.include '**/*.jar'
        spec.include '**/*.jar_'
      }
      spec.into '.'
    }
    def binDistNoticesCopySpec = project.copySpec { CopySpec spec ->
      spec.from "$project.projectDir/LICENSE.txt"
      spec.from "$project.projectDir/src/bin-dist"
      spec.into '.'
    }
    def binDistImage = project.copySpec { CopySpec spec ->
      spec.into "apache-zest-java-$project.version-bin"
      spec.with binDistNoticesCopySpec
      spec.with docsCopySpec
      spec.with reportsDistCopySpec
      spec.with runtimeDependenciesListCopySpec
      spec.with extraDistTextCopySpec
      spec.with extraDistBinCopySpec
      spec.with libsCopySpec
    }

    def zipBinaries = project.tasks.create( 'zipBinaries', Zip ) { Zip task ->
      task.dependsOn project.tasks.getByName( RootProjectPlugin.TaskNames.BUILD_ALL )
      task.baseName = 'apache-zest-java'
      task.classifier = 'bin'
      task.with binDistImage
    }
    def tarBinaries = project.tasks.create( 'tarBinaries', Tar ) { Tar task ->
      task.dependsOn project.tasks.getByName( RootProjectPlugin.TaskNames.BUILD_ALL )
      task.baseName = 'apache-zest-java'
      task.classifier = 'bin'
      task.compression = Compression.GZIP
      task.with binDistImage
    }
    project.artifacts.add( 'archives', zipBinaries )
    project.artifacts.add( 'archives', tarBinaries )

    project.tasks.create( TaskNames.UNPACK_BINARY_DIST, Copy ) { Copy task ->
      task.description = "Unpack the binary distribution"
      task.group = TaskGroups.DISTRIBUTION
      task.with binDistImage
      task.into 'build/unpacked-distributions/bin'
    }

    configureBinaryDistributionCheck( project )
  }

  private static void configureGoOfflineHelpers( Project project )
  {
    def approvedProjectsTask = project.tasks.getByName( ReleaseSpecPlugin.TaskNames.RELEASE_APPROVED_PROJECTS )
    def genOfflineMaven = project.tasks.create( TaskNames.GENERATE_MAVEN_OFFLINE_HELPERS,
                                                GoOfflineHelpersTasks.GenerateMaven )
    def genOfflineGradle = project.tasks.create( TaskNames.GENERATE_GRADLE_OFFLINE_HELPERS,
                                                 GoOfflineHelpersTasks.GenerateGradle )
    genOfflineMaven.repositories = DependenciesPlugin.REPOSITORIES_URLS
    genOfflineGradle.repositories = DependenciesPlugin.REPOSITORIES_URLS
    [ genOfflineMaven, genOfflineGradle ].each { task ->
      task.group = TaskGroups.DISTRIBUTION
      task.dependsOn approvedProjectsTask
    }
    def checkOfflineMaven = project.tasks.create( TaskNames.CHECK_MAVEN_OFFLINE_HELPERS,
                                                  GoOfflineHelpersTasks.CheckMaven )
    def checkOfflineGradle = project.tasks.create( TaskNames.CHECK_GRADLE_OFFLINE_HELPERS,
                                                   GoOfflineHelpersTasks.CheckGradle )
    checkOfflineMaven.dependsOn genOfflineMaven
    checkOfflineGradle.dependsOn genOfflineGradle
    [ checkOfflineMaven, checkOfflineGradle ].each { task ->
      task.group = TaskGroups.DISTRIBUTION_VERIFICATION
      task.dependsOn TaskNames.UNPACK_BINARY_DIST
    }
  }

  private static void configureBinaryDistributionCheck( Project project )
  {
    def unpackedBinDistDir = project.file( "build/unpacked-distributions/bin/apache-zest-java-$project.version-bin" )
    project.tasks.create( TaskNames.CHECK_BINARY_DIST_RAT, RatTask, { RatTask task ->
      task.dependsOn TaskNames.UNPACK_BINARY_DIST
      task.description = "Check the binary distribution using Apache RAT"
      task.group = TaskGroups.DISTRIBUTION_VERIFICATION
      task.inputDir = unpackedBinDistDir.absolutePath
      task.reportDir = project.file( 'build/reports/rat-bin-dist' )
      task.excludes = [
        '.gradle/**',
        'docs/reports/**',
        'etc/templates/**',
        'libs/**'
      ]
    } as Action<RatTask> )
    project.tasks.getByName( TaskNames.CHECK_MAVEN_OFFLINE_HELPERS ) { GoOfflineHelpersTasks.CheckMaven task ->
      task.directory = unpackedBinDistDir
    }
    project.tasks.getByName( TaskNames.CHECK_GRADLE_OFFLINE_HELPERS ) { GoOfflineHelpersTasks.CheckGradle task ->
      task.directory = unpackedBinDistDir
      task.mustRunAfter TaskNames.CHECK_MAVEN_OFFLINE_HELPERS
    }
    project.tasks.create( TaskNames.CHECK_BINARY_DIST ) { Task task ->
      task.dependsOn TaskNames.CHECK_BINARY_DIST_RAT
      task.dependsOn TaskNames.CHECK_MAVEN_OFFLINE_HELPERS
      task.dependsOn TaskNames.CHECK_GRADLE_OFFLINE_HELPERS
    }
  }

  @CompileStatic( TypeCheckingMode.SKIP )
  private static void configureDistributionChecksums( Project project )
  {
    project.tasks.withType( Zip ) { Zip task ->
      task.doLast {
        project.ant.checksum file: task.archivePath, algorithm: 'MD5'
        project.ant.checksum file: task.archivePath, algorithm: 'SHA-512'
      }
    }
    project.tasks.withType( Tar ) { Tar task ->
      task.doLast {
        project.ant.checksum file: task.archivePath, algorithm: 'MD5'
        project.ant.checksum file: task.archivePath, algorithm: 'SHA-512'
      }
    }
  }

  private static void configureHelperTasks( Project project )
  {
    project.tasks.create( 'dist', Copy ) { Copy task ->
      task.dependsOn 'install'
      task.description = "Unpack the binary distribution"
      task.group = TaskGroups.DISTRIBUTION
      task.from project.tasks.getByName( 'unpackBinDist' )
      task.into "$project.buildDir/dist"
    }
    project.tasks.create( 'checkDists' ) { Task task ->
      task.description = "Check the source and binary distributions"
      task.group = TaskGroups.DISTRIBUTION_VERIFICATION
      task.dependsOn TaskNames.CHECK_SOURCE_DIST, TaskNames.CHECK_BINARY_DIST
    }
  }
}
