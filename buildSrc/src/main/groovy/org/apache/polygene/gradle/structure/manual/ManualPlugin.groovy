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
package org.apache.polygene.gradle.structure.manual

import groovy.transform.CompileStatic
import org.apache.polygene.gradle.TaskGroups
import org.apache.polygene.gradle.code.CodePlugin
import org.apache.polygene.gradle.structure.release.ReleaseSpecExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.language.base.plugins.LifecycleBasePlugin

// TODO Expose project output into configurations
@CompileStatic
class ManualPlugin implements Plugin<Project>
{
  static class TaskNames
  {
    static final String WEBSITE = "website"
    static final String MANUALS = "manuals"
  }

  @Override
  void apply( final Project project )
  {
    project.plugins.apply CodePlugin
    def releaseSpec = project.extensions.getByType ReleaseSpecExtension
    def websiteTask = project.tasks.create( TaskNames.WEBSITE, DocumentationTask ) { DocumentationTask task ->
      task.group = TaskGroups.DOCUMENTATION
      task.description = 'Generates documentation website'
      project.rootProject.allprojects.findResults { Project p ->
        // TODO Remove project.afterEvaluate
        p.afterEvaluate {
          if( p.tasks.findByName( AsciidocBuildInfoPlugin.TASK_NAME ) )
          {
            task.dependsOn p.tasks.findByName( AsciidocBuildInfoPlugin.TASK_NAME )
          }
        }
      }
      task.onlyIf { isAsciidocInstalled( project, releaseSpec ) }
      task.docName = 'website'
      task.docType = 'article'
    }
    def manualsTask = project.tasks.create( TaskNames.MANUALS ) { Task task ->
      task.group = TaskGroups.DOCUMENTATION
      task.description = 'Generates all documentation'
      task.dependsOn websiteTask
    }
    project.tasks.getByName( LifecycleBasePlugin.BUILD_TASK_NAME ).dependsOn manualsTask
  }

  private static Boolean asciidocInstalled = null

  // Force when building a release version
  // Skip if skipAsciidocIfAbsent property is set
  // Skip if asciidoc is not found in PATH when building a development version
  private static boolean isAsciidocInstalled( Project project, ReleaseSpecExtension releaseSpec )
  {
    if( asciidocInstalled == null )
    {
      def skipAsciidocIfAbsent = project.findProperty 'skipAsciidocIfAbsent'
      if( !skipAsciidocIfAbsent && releaseSpec.releaseVersion )
      {
        project.logger.info 'Asciidoc tasks forced for building a release version, hope you have Asciidoc installed'
        asciidocInstalled = true
      }
      else
      {
        def pathDirs = System.getenv( 'PATH' ).split( File.pathSeparator )
        def asciidocCandidates = pathDirs.collect( { String path ->
          new File( path, 'asciidoc' )
        } ).flatten() as List<File>
        asciidocInstalled = asciidocCandidates.findAll( { it.isFile() } )
        if( !asciidocInstalled )
        {
          project.logger.lifecycle 'WARNING Asciidoc not found in PATH, manual tasks will skip\n' +
                                   '        Please install http://www.methods.co.nz/asciidoc/'
        }
      }
    }
    return asciidocInstalled
  }
}
