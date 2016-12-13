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
package org.apache.polygene.gradle.doc

import groovy.transform.CompileStatic
import org.apache.polygene.gradle.TaskGroups
import org.apache.polygene.gradle.PolygeneExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.Copy

@CompileStatic
class ManualPlugin implements Plugin<Project>
{
  static class TaskNames
  {
    static final String WEBSITE = "website"
    static final String ARCHIVE_WEBSITE = "archiveWebsite"
    static final String COPY_WEBSITE = "copyWebsite"
    static final String MANUALS = "manuals"
  }

  @Override
  void apply( final Project project )
  {
    def polygene = project.extensions.getByType( PolygeneExtension )
    project.tasks.create( TaskNames.WEBSITE, DocumentationTask ) { DocumentationTask task ->
      task.group = TaskGroups.DOCUMENTATION
      task.description = 'Generates documentation website'
      task.dependsOn project.rootProject.allprojects.findResults { Project p ->
        p.tasks.findByName AsciidocBuildInfoPlugin.TASK_NAME
      }
      task.onlyIf { isAsciidocInstalled( project, polygene ) }
      task.docName = 'website'
      task.docType = 'article'
    }
    project.tasks.create( TaskNames.ARCHIVE_WEBSITE, Copy ) { Copy task ->
      task.group = TaskGroups.DOCUMENTATION
      task.description = 'Copy website to ../polygene-web'
      task.dependsOn TaskNames.WEBSITE
      task.onlyIf { isAsciidocInstalled( project, polygene ) }
      if( polygene.developmentVersion )
      {
        task.into "$project.rootProject.projectDir/../polygene-web/site/content/java/develop"
      }
      else
      {
        task.into "$project.rootProject.projectDir/../polygene-web/site/content/java/$project.version"
      }
      task.from "$project.buildDir/docs/website/"
    }
    project.tasks.create( TaskNames.COPY_WEBSITE, Copy ) { Copy task ->
      task.group = TaskGroups.RELEASE
      task.description = 'Copy website to ../polygene-web LATEST'
      task.dependsOn TaskNames.ARCHIVE_WEBSITE
      task.onlyIf { polygene.releaseVersion }
      task.from "$project.rootProject.projectDir/../polygene-web/site/content/java/$project.version/"
      task.into "$project.rootProject.projectDir/../polygene-web/site/content/java/latest/"
    }
    project.tasks.create( TaskNames.MANUALS ) { Task task ->
      task.group = TaskGroups.DOCUMENTATION
      task.description = 'Generates all documentation'
      task.dependsOn TaskNames.COPY_WEBSITE
      task.onlyIf { isAsciidocInstalled( project, polygene ) }
    }
  }

  private static Boolean asciidocInstalled = null

  // Force when building a release version
  // Skip if skipAsciidocIfAbsent property is set
  // Skip if asciidoc is not found in PATH when building a development version
  private static boolean isAsciidocInstalled( Project project, PolygeneExtension polygene )
  {
    if( asciidocInstalled == null )
    {
      def skipAsciidocIfAbsent = project.findProperty 'skipAsciidocIfAbsent'
      if( !skipAsciidocIfAbsent && polygene.releaseVersion )
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
