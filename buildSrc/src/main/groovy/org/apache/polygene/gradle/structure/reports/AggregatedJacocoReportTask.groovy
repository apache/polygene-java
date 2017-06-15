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
import groovy.transform.TypeCheckingMode
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.TaskAction

@CompileStatic
class AggregatedJacocoReportTask extends DefaultTask
{
  public static final String JACOCO_CONFIGURATION = 'jacoco'

  @InputFiles
  FileCollection getJacocoExecDataDirectories()
  {
    return project.files( project.rootProject.subprojects
                                 .collect( { Project p -> "${ p.buildDir.absolutePath }/jacoco" } ) )
  }

  @OutputDirectory
  File getOutputDirectory()
  {
    return project.file( "$project.buildDir/reports/coverage" )
  }

  @CompileStatic( TypeCheckingMode.SKIP )
  @TaskAction
  void report()
  {
    def classpath = project.configurations.getByName( JACOCO_CONFIGURATION ).asPath

    def coveredProjects = project.rootProject.subprojects.findAll { p -> new File( p.buildDir, 'jacoco' ).exists() }

    def allExecutionData = coveredProjects.collect { "${ it.buildDir.absolutePath }/jacoco" }

    def sourceSetsOf = { String projectPathPrefix, Set<Project> projects ->
      projects.findAll { p -> p.path.startsWith( projectPathPrefix ) }
              .collect { it.convention.getPlugin( JavaPluginConvention ).sourceSets }
              .flatten() as List<SourceSet>
    }
    def sourceDirsOf = { List<SourceSet> sourceSets ->
      def sourceDirs = sourceSets.collect { it.allSource.srcDirs }.flatten() as List<File>
      sourceDirs.findAll { it.directory }.collect { it.absolutePath }
    }
    def classesDirsOf = { List<SourceSet> sourceSets ->
      def classesDirs = sourceSets.collect { it.output.classesDirs.files }.flatten() as List<File>
      classesDirs.findAll { it.directory }.collect { it.absolutePath }
    }

    def coreSourceSets = sourceSetsOf( ':core', coveredProjects )
    def libSourceSets = sourceSetsOf( ':libraries', coveredProjects )
    def extSourceSets = sourceSetsOf( ':extensions', coveredProjects )
    def toolsSourceSets = sourceSetsOf( ':tools', coveredProjects )
    def tutoSourceSets = sourceSetsOf( ':tutorials', coveredProjects )
    def samplesSourceSets = sourceSetsOf( ':samples', coveredProjects )

    project.ant {
      taskdef name: 'jacocoreport', classname: 'org.jacoco.ant.ReportTask', classpath: classpath
      mkdir dir: outputDirectory
      jacocoreport {
        executiondata { allExecutionData.collect { fileset( dir: it ) { include name: '*.exec' } } }
        structure( name: 'Apache Polygene™ (Java Edition) SDK' ) {
          group( name: 'Core' ) {
            classfiles { classesDirsOf(coreSourceSets).collect { fileset( dir: it ) } }
            sourcefiles { sourceDirsOf(coreSourceSets).collect { fileset( dir: it ) } }
          }
          group( name: 'Libraries' ) {
            classfiles { classesDirsOf(libSourceSets).collect { fileset( dir: it ) } }
            sourcefiles { sourceDirsOf(libSourceSets).collect { fileset( dir: it ) } }
          }
          group( name: 'Extensions' ) {
            classfiles { classesDirsOf(extSourceSets).collect { fileset( dir: it ) } }
            sourcefiles { sourceDirsOf(extSourceSets).collect { fileset( dir: it ) } }
          }
          group( name: 'Tools' ) {
            classfiles { classesDirsOf(toolsSourceSets).collect { fileset( dir: it ) } }
            sourcefiles { sourceDirsOf(toolsSourceSets).collect { fileset( dir: it ) } }
          }
          group( name: 'Tutorials' ) {
            classfiles { classesDirsOf(tutoSourceSets).collect { fileset( dir: it ) } }
            sourcefiles { sourceDirsOf(tutoSourceSets).collect { fileset( dir: it ) } }
          }
          group( name: 'Samples' ) {
            classfiles { classesDirsOf(samplesSourceSets).collect { fileset( dir: it ) } }
            sourcefiles { sourceDirsOf(samplesSourceSets).collect { fileset( dir: it ) } }
          }
        }
        csv destfile: "${ outputDirectory }/jacoco.csv", encoding: 'UTF-8'
        xml destfile: "${ outputDirectory }/jacoco.xml", encoding: 'UTF-8'
        html destdir: outputDirectory, encoding: 'UTF-8', locale: 'en', footer: 'Apache Polygene™ (Java Edition) SDK'
      }
    }
  }
}
