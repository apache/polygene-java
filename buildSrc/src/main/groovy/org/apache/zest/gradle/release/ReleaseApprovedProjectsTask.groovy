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
package org.apache.zest.gradle.release

import groovy.json.JsonBuilder
import groovy.transform.CompileStatic
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

/**
 * Write paths of release approved projects to a JSON file.
 *
 * This task sole purpose is proper up-do-date behaviour when changing {@literal dev-status.xml} files.
 */
@CompileStatic
class ReleaseApprovedProjectsTask extends DefaultTask
{
  @InputFiles
  FileCollection getDevStatusFiles()
  {
    return project.files( project.allprojects
                                 .collect( { project -> project.file( 'dev-status.xml' ) } )
                                 .findAll( { it.exists() } ) )
  }

  @OutputFile
  File getJsonApprovedProjects()
  {
    return new File( new File( project.buildDir, 'release' ), 'approved-projects.json' )
  }

  @TaskAction
  void approveProjects()
  {
    def releaseSpec = project.extensions.getByType( ReleaseSpecExtension )
    jsonApprovedProjects.parentFile.mkdirs()
    jsonApprovedProjects.text = new JsonBuilder( releaseSpec.approvedProjects.collect( { it.path } ) ).toPrettyString()
  }
}
