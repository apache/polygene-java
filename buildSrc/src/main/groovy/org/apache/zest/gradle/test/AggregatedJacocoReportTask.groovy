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
package org.apache.zest.gradle.test

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

class AggregatedJacocoReportTask extends DefaultTask
{
  @InputFiles
  FileCollection getJacocoExecDataDirectories()
  {
    return project.files( project.subprojects.collect( { Project p -> "${ p.buildDir.path }/jacoco" } ) )
  }

  @OutputDirectory
  File getOutputDirectory()
  {
    return project.file( "$project.buildDir/reports/coverage" )
  }

  @TaskAction
  void report()
  {
    def coveredProjects = project.subprojects.findAll { p -> new File( "${ p.buildDir.path }/jacoco" ).exists() }
    def coreProjects = coveredProjects.findAll { p -> p.name.startsWith( 'org.apache.zest.core' ) }
    def libProjects = coveredProjects.findAll { p -> p.name.startsWith( 'org.apache.zest.lib' ) }
    def extProjects = coveredProjects.findAll { p -> p.name.startsWith( 'org.apache.zest.ext' ) }
    def toolsProjects = coveredProjects.findAll { p -> p.name.startsWith( 'org.apache.zest.tool' ) }
    def tutoProjects = coveredProjects.findAll { p -> p.name.startsWith( 'org.apache.zest.tuto' ) }
    def samplesProjects = coveredProjects.findAll { p -> p.name.startsWith( 'org.apache.zest.sample' ) }
    def classpath = project.configurations.getByName( 'jacoco' ).asPath
    project.ant {
      taskdef name: 'jacocoreport', classname: 'org.jacoco.ant.ReportTask', classpath: classpath
      mkdir dir: outputDirectory
      jacocoreport {
        executiondata {
          coveredProjects.collect { p -> fileset( dir: "${ p.buildDir.path }/jacoco" ) { include( name: '*.exec' ) } }
        }
        structure( name: "Apache Zest™ (Java Edition) SDK" ) {
          group( name: "Core" ) {
            classfiles { coreProjects.collect { p -> fileset dir: "${ p.buildDir.path }/classes/main" } }
            sourcefiles { coreProjects.collect { p -> fileset dir: "${ p.projectDir.path }/src/main/java" } }
          }
          group( name: "Libraries" ) {
            classfiles { libProjects.collect { p -> fileset dir: "${ p.buildDir.path }/classes/main" } }
            sourcefiles { libProjects.collect { p -> fileset dir: "${ p.projectDir.path }/src/main/java" } }
          }
          group( name: "Extensions" ) {
            classfiles { extProjects.collect { p -> fileset dir: "${ p.buildDir.path }/classes/main" } }
            sourcefiles { extProjects.collect { p -> fileset dir: "${ p.projectDir.path }/src/main/java" } }
          }
          group( name: "Tools" ) {
            classfiles { toolsProjects.collect { p -> fileset dir: "${ p.buildDir.path }/classes/main" } }
            sourcefiles { toolsProjects.collect { p -> fileset dir: "${ p.projectDir.path }/src/main/java" } }
          }
          group( name: "Tutorials" ) {
            classfiles { tutoProjects.collect { p -> fileset dir: "${ p.buildDir.path }/classes/main" } }
            sourcefiles { tutoProjects.collect { p -> fileset dir: "${ p.projectDir.path }/src/main/java" } }
          }
          group( name: "Samples" ) {
            classfiles { samplesProjects.collect { p -> fileset dir: "${ p.buildDir.path }/classes/main" } }
            sourcefiles { samplesProjects.collect { p -> fileset dir: "${ p.projectDir.path }/src/main/java" } }
          }
        }
        csv destfile: "${ outputDirectory }/jacoco.csv", encoding: "UTF-8"
        xml destfile: "${ outputDirectory }/jacoco.xml", encoding: "UTF-8"
        html destdir: outputDirectory, encoding: "UTF-8", locale: "en", footer: "Apache Zest™ (Java Edition) SDK"
      }
    }
  }
}
