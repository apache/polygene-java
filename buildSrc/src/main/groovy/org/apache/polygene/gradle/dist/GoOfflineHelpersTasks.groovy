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
package org.apache.polygene.gradle.dist

import groovy.transform.CompileStatic
import org.apache.polygene.gradle.release.ReleaseSpecExtension
import org.apache.polygene.gradle.tasks.ExecLogged
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.artifacts.result.ResolvedComponentResult
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecSpec

/**
 * Tasks to generate and check go-offline maven and gradle helpers bundled with the binary distribution.
 */
@CompileStatic
interface GoOfflineHelpersTasks
{
  class GenerateMaven extends DefaultTask
  {
    static final String POM_FILENAME = 'go-offline.pom'

    @Input
    Map<String, String> repositories = [ : ]

    @Internal
    File outputDir = new File( project.buildDir, 'go-offline-helpers' )

    @OutputFile
    File getMavenGoOfflineHelper()
    {
      return new File( outputDir, POM_FILENAME )
    }

    GenerateMaven()
    {
      super();
      outputs.upToDateWhen { false }
    }

    @TaskAction
    void generate()
    {
      outputDir.mkdirs()
      def components = Utils.resolveAllRuntimeComponents( project )
      def maven = generateMaven( components )
      mavenGoOfflineHelper.text = maven
    }

    private String generateMaven( Set<ResolvedComponentResult> components )
    {
      def pom = Utils.licenseHeader( project.file( 'etc/header.txt' ).text, 'xml' )
      pom += '<project>\n  <modelVersion>4.0.0</modelVersion>\n'
      pom +=
        "  <groupId>org.apache.polygene</groupId>\n  <artifactId>go-offline-helper</artifactId>\n  <version>$project.version</version>\n"
      pom += '  <packaging>pom</packaging>\n'
      pom +=
        '  <!--\n  This pom has the sole purpose of downloading all dependencies in a directory relative to this file named \'dependencies\'.\n'
      pom += "  Use the following command:\n\n  mvn -f $POM_FILENAME validate\n  -->\n  <repositories>\n"
      repositories.entrySet().each { repo ->
        pom += "    <repository><id>go-offline-repo-$repo.key</id><url>${ repo.value }</url></repository>\n"
      }
      pom += '  </repositories>\n  <dependencies>\n'
      components.each { comp ->
        pom += '    <dependency>\n'
        pom += "      <groupId>$comp.moduleVersion.group</groupId>\n"
        pom += "      <artifactId>$comp.moduleVersion.name</artifactId>\n"
        pom += "      <version>$comp.moduleVersion.version</version>\n"
        pom += '    </dependency>\n'
      }
      pom += """  </dependencies>\n  <build><plugins><plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-dependency-plugin</artifactId>
    <version>2.10</version>
    <executions>
      <execution>
        <id>go-offline-jars</id><phase>validate</phase>
        <goals><goal>copy-dependencies</goal></goals>
        <configuration>
          <outputDirectory>\${project.basedir}/dependencies</outputDirectory>
          <excludeTransitive>true</excludeTransitive>
        </configuration>
      </execution>
      <execution>
        <id>go-offline-sources</id><phase>validate</phase>
        <goals><goal>copy-dependencies</goal></goals>
        <configuration>
          <classifier>sources</classifier><failOnMissingClassifierArtifact>false</failOnMissingClassifierArtifact>
          <outputDirectory>\${project.basedir}/dependencies</outputDirectory>
          <excludeTransitive>true</excludeTransitive>
        </configuration>
      </execution>
      <execution>
        <id>go-offline-javadocs</id><phase>validate</phase>
        <goals><goal>copy-dependencies</goal></goals>
        <configuration>
          <classifier>javadoc</classifier><failOnMissingClassifierArtifact>false</failOnMissingClassifierArtifact>
          <outputDirectory>\${project.basedir}/dependencies</outputDirectory>
          <excludeTransitive>true</excludeTransitive>
        </configuration>
      </execution>
    </executions>
  </plugin></plugins></build>
</project>
"""
      return pom
    }
  }

  class GenerateGradle extends DefaultTask
  {
    static final String BUILD_SCRIPT_FILENAME = 'go-offline.gradle'

    @Input
    Map<String, String> repositories = [ : ]

    @Internal
    File outputDir = new File( project.buildDir, 'go-offline-helpers' )

    @OutputFile
    File getGradleGoOfflineHelper()
    {
      return new File( outputDir, BUILD_SCRIPT_FILENAME )
    }

    GenerateGradle()
    {
      super();
      outputs.upToDateWhen { false }
    }

    @TaskAction
    void generate()
    {
      outputDir.mkdirs()
      def components = Utils.resolveAllRuntimeComponents( project )
      def gradle = generateGradle( components )
      gradleGoOfflineHelper.text = gradle
    }

    private String generateGradle( Set<ResolvedComponentResult> components )
    {
      def build = Utils.licenseHeader( project.file( 'etc/header.txt' ).text, 'java' )
      build += '// This gradle build file has the sole purpose of downloading all dependencies in a directory\n'
      build += '// relative to this file named \'dependencies\'.\n'
      build += "// Use the following command: gradle -b $BUILD_SCRIPT_FILENAME download\n"
      build += 'apply plugin: \'java\'\nconfigurations { download }\nrepositories {\n'
      repositories.entrySet().each { repo ->
        build += "  maven { url '${ repo.value }' }\n"
      }
      build += '}\ndependencies {\n'
      components.each { comp ->
        def depCoords = "${ comp.moduleVersion.group }:${ comp.moduleVersion.name }:${ comp.moduleVersion.version }"
        build += "  download( '$depCoords' ) { transitive = false }\n"
      }
      build += """}
task download( type: Copy ) {
  outputs.upToDateWhen { false }
  def sources = configurations.download.resolvedConfiguration.resolvedArtifacts.collect { artifact ->
    project.dependencies.create( [ group: artifact.moduleVersion.id.group, name: artifact.moduleVersion.id.name, version: artifact.moduleVersion.id.version, classifier: 'sources' ] )
  }
  def javadocs = configurations.download.resolvedConfiguration.resolvedArtifacts.collect { artifact ->
    project.dependencies.create( [ group: artifact.moduleVersion.id.group, name: artifact.moduleVersion.id.name, version: artifact.moduleVersion.id.version, classifier: 'javadoc' ] )
  }
  from configurations.download
  from configurations.detachedConfiguration( sources as Dependency[] ).resolvedConfiguration.lenientConfiguration.getFiles( Specs.SATISFIES_ALL )
  from configurations.detachedConfiguration( javadocs as Dependency[] ).resolvedConfiguration.lenientConfiguration.getFiles( Specs.SATISFIES_ALL )
  into file( 'dependencies/' )
}
"""
      return build
    }
  }

  class CheckMaven extends DefaultTask
  {
    @Internal
    File directory

    @InputFile
    File getMavenGoOfflineHelper()
    {
      return new File( directory, GenerateMaven.POM_FILENAME )
    }

    CheckMaven()
    {
      super();
      description = 'Check the binary distribution Maven go-offline helper'
      outputs.upToDateWhen { false }
      onlyIf { Utils.isMvnInstalled() }
    }

    @TaskAction
    void check()
    {
      def dependenciesDir = new File( directory, 'dependencies' )
      project.delete dependenciesDir
      def outLog = project.file( "$project.buildDir/tmp/$name/stdout.log" )
      def errLog = project.file( "$project.buildDir/tmp/$name/stderr.log" )
      def command = [ 'mvn', '-e', '-f', GenerateMaven.POM_FILENAME, 'validate' ] as Object[]
      ExecLogged.execLogged( project, outLog, errLog ) { ExecSpec spec ->
        spec.workingDir directory
        spec.commandLine command
      }
      Utils.checkAllJarsArePresent( project, dependenciesDir, GenerateMaven.POM_FILENAME )
    }
  }

  class CheckGradle extends DefaultTask
  {
    @Internal
    File directory

    @InputFile
    File getGradleGoOfflineHelper()
    {
      return new File( directory, GenerateGradle.BUILD_SCRIPT_FILENAME )
    }

    CheckGradle()
    {
      super();
      description = 'Check the binary distribution Gradle go-offline helper'
      outputs.upToDateWhen { false }
    }

    @TaskAction
    void check()
    {
      def buildScript = new File( directory, GenerateGradle.BUILD_SCRIPT_FILENAME )
      def dependenciesDir = new File( directory, 'dependencies' )
      project.delete dependenciesDir
      def outLog = project.file( "$project.buildDir/tmp/$name/stdout.log" )
      def errLog = project.file( "$project.buildDir/tmp/$name/stderr.log" )
      ExecLogged.execLogged( project, outLog, errLog ) { ExecSpec spec ->
        spec.workingDir project.projectDir
        spec.commandLine './gradlew', '-u', '-s', '-b', buildScript.absolutePath, 'download'
      }
      Utils.checkAllJarsArePresent( project, dependenciesDir, GenerateGradle.BUILD_SCRIPT_FILENAME )
    }
  }

  static class Utils
  {
    // Do the global dependency resolution here so there won't be any surprise when using the helpers
    // This also allow to apply the resolution strategy defined in libraries.gradle
    // WARN some of our modules depends on != versions of some artifacts, this resolution flatten this using the most up to date
    private static Set<ResolvedComponentResult> resolveAllRuntimeComponents( Project rootProject )
    {
      def allRuntimeDeps = getAllRuntimeDependencies( rootProject )
      def configuration = rootProject.configurations.findByName( 'goOfflineHelpers' )
      if( !configuration )
      {
        configuration = rootProject.configurations.create( 'goOfflineHelpers' )
        allRuntimeDeps.each { set -> rootProject.dependencies.add( configuration.name, set ) }
      }
      return configuration.incoming.resolutionResult.allComponents.findAll { ResolvedComponentResult comp ->
        !comp.moduleVersion.group.startsWith( 'org.apache.polygene' )
      } as Set<ResolvedComponentResult>
    }

    private static List<Dependency> getAllRuntimeDependencies( Project rootProject )
    {
      def releaseSpec = rootProject.extensions.getByType( ReleaseSpecExtension )
      def allDependencies = releaseSpec.approvedProjects.collect { project ->
        project.configurations.getByName( 'runtime' ).allDependencies
      }.flatten() as List<Dependency>
      return allDependencies.findAll { Dependency dep ->
        !( dep instanceof ProjectDependency ) && dep.name != null && !dep.group.startsWith( 'org.apache.polygene' )
      }
    }

    private static void checkAllJarsArePresent( Project rootProject, File dependenciesDir, String helper )
    {
      def allDependencies = getAllRuntimeDependencies( rootProject )
      allDependencies.each { Dependency dep ->
        def jarName = "${ dep.name }-${ dep.version }.jar"
        def jarFile = new File( dependenciesDir, jarName )
        if( !jarFile.exists() )
        {
          throw new GradleException( "Binary distribution $helper failed!\n" +
                                     "\tMissing: $dep\n" +
                                     "\tin $jarFile" );
        }
      }
    }

    private static boolean isMvnInstalled()
    {
      def pathDirs = System.getenv( 'PATH' ).split( File.pathSeparator )
      def flattened = pathDirs.collect( { String pathDir -> new File( pathDir, 'mvn' ) } ).flatten() as List<File>
      return flattened.find( { File pathDir -> pathDir.isFile() } ) != null
    }

    // Generate license headers with comment styles
    private static String licenseHeader( String base, String flavour )
    {
      def header
      switch( flavour )
      {
        case 'java': case 'groovy': case 'js':
          header = licenseHeader_wrap( base, '/*', ' * ', ' */' ); break
        case 'xml': case 'html':
          header = licenseHeader_wrap( base, '<!--', '  ', '-->' ); break
        case 'txt': case 'shell': case 'python': case 'ruby':
          header = licenseHeader_wrap( base, null, '# ', null ); break
        case 'adoc': case 'asciidoc':
          header = licenseHeader_wrap( base, null, '// ', null ); break
        default:
          header = base
      }
      header
    }

    private static String licenseHeader_wrap( String base, String top, String left, String bottom )
    {
      ( top ? "$top\n" : '' ) + base.readLines().collect { "${ left }${ it }" }.join( '\n' ) + '\n' +
      ( bottom ? "$bottom\n" : '' )
    }
  }
}
