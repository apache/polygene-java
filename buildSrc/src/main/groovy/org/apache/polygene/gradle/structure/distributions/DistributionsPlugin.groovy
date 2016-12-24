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
package org.apache.polygene.gradle.structure.distributions

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import java.nio.file.Files
import java.nio.file.Path
import org.apache.commons.io.FileUtils
import org.apache.polygene.gradle.BasePlugin
import org.apache.polygene.gradle.TaskGroups
import org.apache.polygene.gradle.code.PublishedCodePlugin
import org.apache.polygene.gradle.dependencies.DependenciesDeclarationExtension
import org.apache.polygene.gradle.dependencies.DependenciesPlugin
import org.apache.polygene.gradle.structure.release.ReleaseSpecExtension
import org.apache.polygene.gradle.tasks.ExecLogged
import org.apache.rat.gradle.RatTask
import org.apache.tools.ant.filters.ReplaceTokens
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.artifacts.result.ComponentArtifactsResult
import org.gradle.api.artifacts.result.ResolvedArtifactResult
import org.gradle.api.artifacts.result.ResolvedDependencyResult
import org.gradle.api.file.CopySpec
import org.gradle.api.file.FileCopyDetails
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Sync
import org.gradle.api.tasks.bundling.Compression
import org.gradle.api.tasks.bundling.Tar
import org.gradle.api.tasks.bundling.Zip
import org.gradle.language.base.plugins.LifecycleBasePlugin
import org.gradle.maven.MavenModule
import org.gradle.maven.MavenPomArtifact
import org.gradle.plugins.ide.internal.IdeDependenciesExtractor
import org.gradle.plugins.ide.internal.resolver.model.IdeExtendedRepoFileDependency

// TODO Expose all project outputs into configurations
@CompileStatic
class DistributionsPlugin implements Plugin<Project>
{
  static class TaskNames
  {
    static final String STAGE_SOURCE_DIST = 'stageSourceDistribution'
    static final String ZIP_SOURCE_DIST = 'zipSourceDistribution'
    static final String TAR_SOURCE_DIST = 'tarSourceDistribution'

    static final String STAGE_MAVEN_BINARIES = 'stageBinariesMavenRepository'
    static final String STAGE_BINARY_DIST = 'stageBinaryDistribution'
    static final String ZIP_BINARY_DIST = 'zipBinaryDistribution'
    static final String TAR_BINARY_DIST = 'tarBinaryDistribution'

    private static final String STAGE_MAVEN_DEPENDENCIES = 'stageDependenciesMavenRepository'
    static final String ZIP_DEPENDENCIES_DIST = 'zipDependenciesDistribution'

    static final String CHECK_DISTRIBUTIONS = 'checkDistributions'
    static final String CHECK_SOURCE_DIST = 'checkSourceDistribution'
    static final String CHECK_BINARY_DIST = 'checkBinaryDistribution'
    private static final String RAT_SOURCE_DIST = 'ratSourceDistribution'
    private static final String BUILD_SOURCE_DIST = 'buildSourceDistribution'
    private static final String RAT_BINARY_DIST = 'ratBinaryDistribution'
  }

  @Override
  void apply( final Project project )
  {
    project.plugins.apply BasePlugin
    project.plugins.apply org.gradle.api.plugins.BasePlugin
    project.plugins.apply DependenciesPlugin
    applyStageBinariesMavenRepository project
    applyStageLibrariesMavenRepository project
    applyAssembleDistributions project
    applyCheckDistributions project
    configureDistributionChecksums project
  }

  private static void applyAssembleDistributions( Project project )
  {
    applySourceDistribution project
    applyBinaryDistribution project
  }

  private static void applyCheckDistributions( Project project )
  {
    def distChecks = [ applySourceDistributionCheck( project ),
                       applyBinaryDistributionCheck( project ) ]
    project.tasks.create( TaskNames.CHECK_DISTRIBUTIONS ) { Task task ->
      task.group = TaskGroups.DISTRIBUTION_VERIFICATION
      task.description = 'Run all distribution checks.'
      task.dependsOn distChecks
    }
  }

  private static void applySourceDistribution( Project project )
  {
    def releaseSpec = project.extensions.getByType( ReleaseSpecExtension )
    def srcDistFilesCopySpec = project.copySpec { CopySpec spec ->
      spec.from project.rootProject.projectDir
      spec.include '*.txt'
      spec.include 'doap.rdf'
      spec.include '*.gradle'
      spec.include 'gradlew*'
      spec.include 'gradle/**'
      spec.include 'etc/**'
      spec.include 'buildSrc/**'
      spec.include 'src/**'
      releaseSpec.publishedProjects.each { p ->
        def relPath = new File( project.rootProject.projectDir.toURI().relativize( p.projectDir.toURI() ).toString() )
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
      spec.exclude '**/.gradle/**'           // Gradle caches
      spec.exclude '**/.gradletasknamecache' // Gradle shell completion cache
      spec.into '.'
    }
    def srcDistSupplementaryFilesCopySpec = project.copySpec { CopySpec spec ->
      spec.from project.file( 'src/src-dist' )
      spec.into '.'
    }
    def srcDistFilteredFilesTask = project.tasks.create 'srcDistFilteredFiles'
    srcDistFilteredFilesTask.description = 'Apply release specification to source distribution build scripts'
    // Generates various files for the source distribution
    // - settings.gradle
    // - gradle.properties to set version !
    def filteredDir = new File( "$project.buildDir/tmp/srcDistFilteredFiles" )
    srcDistFilteredFilesTask.outputs.file filteredDir
    srcDistFilteredFilesTask.doLast {
      // Settings
      def settingsFile = new File( filteredDir, 'settings.gradle' )
      settingsFile.parentFile.mkdirs()
      if( releaseSpec.releaseVersion )
      {
        def filteredSettings = ''
        def unapprovedProjectPaths = releaseSpec.unapprovedProjects.collect { p -> p.path.substring( 1 ) }
        project.rootProject.file( 'settings.gradle' ).readLines().each { line ->
          if( !unapprovedProjectPaths.find { p -> line.contains p } )
          {
            filteredSettings += "$line\n"
          }
        }
        settingsFile.text = filteredSettings
      }
      else
      {
        settingsFile.text = project.rootProject.file( 'settings.gradle' ).text
      }
      // gradle.properties
      def gradlePropsFile = new File( filteredDir, 'gradle.properties' )
      gradlePropsFile.parentFile.mkdirs()
      gradlePropsFile.text = project.rootProject.file( 'gradle.properties' ).text +
                             "\nskipSigning=true\nskipAsciidocIfAbsent=true\n\nversion=$project.version\n"
    }
    def srcDistFilteredFilesCopySpec = project.copySpec { CopySpec spec ->
      spec.from srcDistFilteredFilesTask
      spec.into '.'
    }
    def srcDistCopySpec = project.copySpec { CopySpec spec ->
      spec.into "apache-polygene-java-$project.version-src"
      spec.with srcDistFilesCopySpec
      spec.with srcDistSupplementaryFilesCopySpec
      spec.with srcDistFilteredFilesCopySpec
    }

    def zipSources = project.tasks.create( TaskNames.ZIP_SOURCE_DIST, Zip ) { Zip task ->
      task.group = TaskGroups.DISTRIBUTION
      task.description = 'Assembles .zip source distribution.'
      task.baseName = 'apache-polygene-java'
      task.with srcDistCopySpec
      task.classifier = 'src'
    }
    def tarSources = project.tasks.create( TaskNames.TAR_SOURCE_DIST, Tar ) { Tar task ->
      task.group = TaskGroups.DISTRIBUTION
      task.description = 'Assembles .tar.gz source distribution.'
      task.baseName = 'apache-polygene-java'
      task.with srcDistCopySpec
      task.compression = Compression.GZIP
      task.classifier = 'src'
    }
    project.artifacts.add( 'archives', zipSources )
    project.artifacts.add( 'archives', tarSources )

    project.tasks.create( TaskNames.STAGE_SOURCE_DIST, Copy ) { Copy task ->
      task.group = TaskGroups.DISTRIBUTION
      task.description = "Stages the source distribution in the build directory."
      task.with srcDistCopySpec
      task.into 'build/stage/source-distribution'
    }
  }

  private static Task applySourceDistributionCheck( Project project )
  {
    def releaseSpec = project.extensions.getByType ReleaseSpecExtension
    project.plugins.apply 'org.nosphere.apache.rat-base'
    def unpackedSrcDistDir = project.file( "build/stage/source-distribution/apache-polygene-java-$project.version-src" )
    project.tasks.create( TaskNames.RAT_SOURCE_DIST, RatTask, { RatTask task ->
      task.group = TaskGroups.DISTRIBUTION_VERIFICATION
      task.description = 'Checks the source distribution using Apache RAT.'
      task.dependsOn TaskNames.STAGE_SOURCE_DIST
      task.setInputDir unpackedSrcDistDir.absolutePath
      task.onlyIf { !releaseSpec.developmentVersion }
      task.excludes = [
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
    } as Action<RatTask> )
    project.tasks.create( TaskNames.BUILD_SOURCE_DIST, ExecLogged, { ExecLogged task ->
      task.group = TaskGroups.DISTRIBUTION_VERIFICATION
      task.description = 'Checks the source distribution by running `gradle build` inside.'
      task.dependsOn TaskNames.STAGE_SOURCE_DIST
      task.mustRunAfter TaskNames.RAT_SOURCE_DIST
      def workDir = project.file( "$project.buildDir/tmp/${ TaskNames.BUILD_SOURCE_DIST }" )
      task.inputs.dir unpackedSrcDistDir
      task.workingDir = workDir
      task.commandLine = [ './gradlew', 'build', '-u', '-s', /* '-g', workDir */ ]
      task.doFirst {
        project.copy { CopySpec spec ->
          spec.from unpackedSrcDistDir
          spec.into workDir
        }
      }
      task.doLast {
        if( workDir.exists() )
        {
          FileUtils.deleteDirectory( workDir )
        }
      }
    } as Action<ExecLogged> )
    project.tasks.create( TaskNames.CHECK_SOURCE_DIST ) { Task task ->
      task.description = "Checks the source distribution."
      task.dependsOn TaskNames.RAT_SOURCE_DIST, TaskNames.BUILD_SOURCE_DIST
    }
  }

  private static void applyBinaryDistribution( Project project )
  {
    def releaseSpec = project.extensions.getByType( ReleaseSpecExtension )
    def reportsDistCopySpec = project.copySpec { CopySpec spec ->
      spec.from "$project.rootProject.projectDir/reports/build/reports"
      spec.into 'docs/reports'
    }
    def docsCopySpec = project.copySpec { CopySpec spec ->
      spec.from "$project.rootProject.projectDir/reports/build/docs"
      spec.from "$project.rootProject.projectDir/manual/build/docs/website"
      spec.into 'docs'
    }
    def libsCopySpec = project.copySpec { CopySpec spec ->
      spec.from project.tasks.getByName( TaskNames.STAGE_MAVEN_BINARIES )
      spec.into 'libs/'
      spec.exclude '**-testsources.jar'
      spec.exclude '**/*.asc'
    }
    def extraDistTextCopySpec = project.copySpec { CopySpec spec ->
      releaseSpec.publishedProjects.collect { p ->
        spec.from project.fileTree( dir: "$p.projectDir/src/dist/", include: '**', exclude: "**/*.jar*" )
        spec.eachFile { FileCopyDetails fcd ->
          fcd.filter( ReplaceTokens, tokens: [ version: project.version ] )
        }
      }
      spec.into '.'
    }
    def extraDistBinCopySpec = project.copySpec { CopySpec spec ->
      releaseSpec.publishedProjects.collect { p ->
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
      spec.into "apache-polygene-java-$project.version-bin"
      spec.with binDistNoticesCopySpec
      spec.with docsCopySpec
      spec.with reportsDistCopySpec
      spec.with extraDistTextCopySpec
      spec.with extraDistBinCopySpec
      spec.with libsCopySpec
    }

    def binariesBuildDependencies = {
      project.rootProject.allprojects
             .findAll { p -> p.plugins.hasPlugin( PublishedCodePlugin ) || p.path == ':manual' || p.path == ':reports' }
             .collect { p -> "${ p.path }:${ LifecycleBasePlugin.BUILD_TASK_NAME }" }
    }

    def zipBinaries = project.tasks.create( TaskNames.ZIP_BINARY_DIST, Zip ) { Zip task ->
      task.group = TaskGroups.DISTRIBUTION
      task.description = 'Assembles .zip binary distribution.'
      task.dependsOn binariesBuildDependencies
      task.baseName = 'apache-polygene-java'
      task.classifier = 'bin'
      task.with binDistImage
    }
    def tarBinaries = project.tasks.create( TaskNames.TAR_BINARY_DIST, Tar ) { Tar task ->
      task.group = TaskGroups.DISTRIBUTION
      task.description = 'Assembles .tar.gz binary distribution.'
      task.dependsOn binariesBuildDependencies
      task.baseName = 'apache-polygene-java'
      task.classifier = 'bin'
      task.compression = Compression.GZIP
      task.with binDistImage
    }
    project.artifacts.add( 'archives', zipBinaries )
    project.artifacts.add( 'archives', tarBinaries )

    project.tasks.create( TaskNames.STAGE_BINARY_DIST, Copy ) { Copy task ->
      task.group = TaskGroups.DISTRIBUTION
      task.description = "Stages the binary distribution in the build directory."
      task.with binDistImage
      task.into 'build/stage/binary-distribution'
    }
  }

  private static Task applyBinaryDistributionCheck( Project project )
  {
    def releaseSpec = project.extensions.getByType ReleaseSpecExtension
    project.plugins.apply 'org.nosphere.apache.rat-base'
    def unpackedBinDistDir = project.file( "build/stage/binary-distribution/apache-polygene-java-$project.version-bin" )
    project.tasks.create( TaskNames.RAT_BINARY_DIST, RatTask, { RatTask task ->
      task.group = TaskGroups.DISTRIBUTION_VERIFICATION
      task.description = "Checks the binary distribution using Apache RAT."
      task.onlyIf { !releaseSpec.developmentVersion }
      task.dependsOn TaskNames.STAGE_BINARY_DIST
      task.inputDir = unpackedBinDistDir.absolutePath
      task.reportDir = project.file( 'build/reports/rat-bin-dist' )
      task.excludes = [
        '.gradle/**',
        'docs/**',
        'etc/templates/**',
        'libs/**'
      ]
    } as Action<RatTask> )
    project.tasks.create( TaskNames.CHECK_BINARY_DIST ) { Task task ->
      task.description = 'Checks the binary distribution.'
      task.dependsOn TaskNames.RAT_BINARY_DIST
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

  private static void applyStageBinariesMavenRepository( Project project )
  {
    def releaseSpec = project.extensions.getByType ReleaseSpecExtension
    project.tasks.create( TaskNames.STAGE_MAVEN_BINARIES, Sync, { Sync task ->
      task.group = TaskGroups.DISTRIBUTION
      task.description = 'Stages published binaries as a maven repository in the build directory.'
      releaseSpec.publishedProjects.each { p ->
        task.dependsOn "${ p.path }:uploadStageArchives"
        task.from "${ p.buildDir }/stage/archives"
      }
      task.into project.file( "$project.buildDir/stage/maven-binaries" )
    } as Action<Sync> )
  }

  private static void applyStageLibrariesMavenRepository( Project project )
  {
    def releaseSpec = project.extensions.getByType ReleaseSpecExtension
    def dependenciesDeclaration = project.rootProject.extensions.getByType DependenciesDeclarationExtension
    def stageTask = project.tasks.create( TaskNames.STAGE_MAVEN_DEPENDENCIES ) { Task task ->
      task.group = TaskGroups.DISTRIBUTION
      task.description = 'Stages dependencies of published binaries as a maven repository in the build directory. (BIG)'
      def output = project.file "$project.buildDir/stage/dependencies"
      task.inputs.property 'libraries', dependenciesDeclaration.libraries
      task.outputs.dir output
      task.doLast {
        // Resolve runtime dependencies across all staged projects
        def libraries = [ ] as Set<Dependency>
        releaseSpec.publishedProjects.each { p ->
          libraries += p.configurations.getByName( 'runtime' ).allDependencies
        }
        def configuration = project.configurations.detachedConfiguration( libraries as Dependency[] )
        DependenciesPlugin.applyDependencyResolutionRules configuration, dependenciesDeclaration
        // Copy Maven POMs
        def allDependencies = configuration.incoming.resolutionResult.allDependencies as Set<ResolvedDependencyResult>
        def componentIds = allDependencies.collect { it.selected.id }
        def result = project.dependencies.createArtifactResolutionQuery()
                            .forComponents( componentIds )
                            .withArtifacts( MavenModule, MavenPomArtifact )
                            .execute()
        result.resolvedComponents.each { ComponentArtifactsResult component ->
          def id = component.id
          if( id instanceof ModuleComponentIdentifier )
          {
            def artifact = component.getArtifacts( MavenPomArtifact )[ 0 ]
            if( artifact instanceof ResolvedArtifactResult )
            {
              def groupDir = new File( output, id.getGroup().split( "\\." ).join( '/' ) )
              File moduleDir = new File( groupDir, "${ id.getModule() }/${ id.getVersion() }" )
              Files.createDirectories moduleDir.toPath()
              def destination = new File( moduleDir, artifact.getFile().getName() ).toPath()
              Files.exists( destination ) ?: Files.copy( artifact.getFile().toPath(), destination )
            }
          }
        }
        // Copy Maven artifacts using the Gradle IDE Model
        // Include sources if available, otherwise include javadoc if available
        IdeDependenciesExtractor dependenciesExtractor = new IdeDependenciesExtractor();
        def ideDependencies = dependenciesExtractor.extractRepoFileDependencies project.dependencies,
                                                                                [ configuration ], [ ],
                                                                                true, true
        ideDependencies.each { IdeExtendedRepoFileDependency ideDependency ->
          def id = ideDependency.id
          def groupDir = new File( output, id.group.split( "\\." ).join( '/' ) )
          File moduleDir = new File( groupDir, "${ id.name }/${ id.version }" )
          Files.createDirectories moduleDir.toPath()
          Path destination = new File( moduleDir, ideDependency.file.name ).toPath()
          Files.exists( destination ) ?: Files.copy( ideDependency.file.toPath(), destination )
          if( ideDependency.sourceFile )
          {
            def sourceDestination = new File( moduleDir, ideDependency.sourceFile.name ).toPath()
            Files.exists( sourceDestination ) ?: Files.copy( ideDependency.sourceFile.toPath(), sourceDestination )
          }
          else if( ideDependency.javadocFile )
          {
            def javadocDestination = new File( moduleDir, ideDependency.javadocFile.name ).toPath()
            Files.exists( javadocDestination ) ?: Files.copy( ideDependency.javadocFile.toPath(), javadocDestination )
          }
        }
      }
    }
    project.tasks.create( TaskNames.ZIP_DEPENDENCIES_DIST, Zip ) { Zip task ->
      task.group = TaskGroups.DISTRIBUTION
      task.description = 'Assemble .zip dependencies distribution (BIG)'
      task.baseName = 'apache-polygene-java'
      task.classifier = 'dependencies'
      task.from stageTask
      task.into "apache-polygene-java-$project.version-dependencies"
    }
  }
}