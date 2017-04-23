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
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

@CompileStatic
class AggregatedJacocoReportTask extends DefaultTask
{
  public static final String JACOCO_CONFIGURATION = 'jacoco'

  @InputFiles
  FileCollection getJacocoExecDataDirectories()
  {
    return project.files( project.subprojects.collect( { Project p -> "${ p.buildDir.absolutePath }/jacoco" } ) )
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
    def coveredProjects = project.rootProject.subprojects.findAll { p -> new File( p.buildDir, 'jacoco' ).exists() }
    def coreProjects = coveredProjects.findAll { p -> p.path.startsWith ':core' }
    def libProjects = coveredProjects.findAll { p -> p.path.startsWith ':libraries' }
    def extProjects = coveredProjects.findAll { p -> p.path.startsWith ':extensions' }
    def toolsProjects = coveredProjects.findAll { p -> p.path.startsWith ':tools' }
    def tutoProjects = coveredProjects.findAll { p -> p.path.startsWith ':tutorials' }
    def samplesProjects = coveredProjects.findAll { p -> p.path.startsWith ':samples' }
    def classpath = project.configurations.getByName( JACOCO_CONFIGURATION ).asPath
    project.ant {
      taskdef name: 'jacocoreport', classname: 'org.jacoco.ant.ReportTask', classpath: classpath
      mkdir dir: outputDirectory
      jacocoreport {
        executiondata {
          coveredProjects.collect { p ->
            fileset( dir: "${ p.buildDir.path }/jacoco" ) { include name: '*.exec' }
          }
        }
        structure( name: 'Apache Polygene™ (Java Edition) SDK' ) {
          group( name: 'Core' ) {
            classfiles { coreProjects.collect { p -> fileset dir: "${ p.buildDir.path }/classes/main" } }
            sourcefiles { samplesProjects.collect { p -> sourceRootsOf( p ).each { sourceRoot -> fileset dir: sourceRoot.absolutePath } } }
          }
          group( name: 'Libraries' ) {
            classfiles { libProjects.collect { p -> fileset dir: "${ p.buildDir.path }/classes/main" } }
            sourcefiles { samplesProjects.collect { p -> sourceRootsOf( p ).each { sourceRoot -> fileset dir: sourceRoot.absolutePath } } }
          }
          group( name: 'Extensions' ) {
            classfiles { extProjects.collect { p -> fileset dir: "${ p.buildDir.path }/classes/main" } }
            sourcefiles { samplesProjects.collect { p -> sourceRootsOf( p ).each { sourceRoot -> fileset dir: sourceRoot.absolutePath } } }
          }
          group( name: 'Tools' ) {
            classfiles { toolsProjects.collect { p -> fileset dir: "${ p.buildDir.path }/classes/main" } }
            sourcefiles { samplesProjects.collect { p -> sourceRootsOf( p ).each { sourceRoot -> fileset dir: sourceRoot.absolutePath } } }
          }
          group( name: 'Tutorials' ) {
            classfiles { tutoProjects.collect { p -> fileset dir: "${ p.buildDir.path }/classes/main" } }
            sourcefiles { samplesProjects.collect { p -> sourceRootsOf( p ).each { sourceRoot -> fileset dir: sourceRoot.absolutePath } } }
          }
          group( name: 'Samples' ) {
            classfiles { samplesProjects.collect { p -> fileset dir: "${ p.buildDir.path }/classes/main" } }
            sourcefiles { samplesProjects.collect { p -> sourceRootsOf( p ).each { sourceRoot -> fileset dir: sourceRoot.absolutePath } } }
          }
        }
        csv destfile: "${ outputDirectory }/jacoco.csv", encoding: 'UTF-8'
        xml destfile: "${ outputDirectory }/jacoco.xml", encoding: 'UTF-8'
        html destdir: outputDirectory, encoding: 'UTF-8', locale: 'en', footer: 'Apache Polygene™ (Java Edition) SDK'
      }
    }
  }

  private static List<File> sourceRootsOf( Project project )
  {
    [ 'src/main/java', 'src/main/groovy', 'src/main/kotlin' ]
      .collect { project.file( it ) }
      .findAll { it.exists() }
  }
}
