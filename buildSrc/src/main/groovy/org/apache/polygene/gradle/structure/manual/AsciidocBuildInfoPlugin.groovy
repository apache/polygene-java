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
import org.gradle.api.Project
import org.gradle.api.Plugin

@CompileStatic
class AsciidocBuildInfoPlugin implements Plugin<Project>
{
  final static String TASK_NAME = 'makeAsciidocBuildInfo'

  void apply( Project project )
  {
    def buildInfoDir = new File( project.buildDir, "docs/buildinfo" );

    def makeAsciidocBuildInfoTask = project.tasks.create( TASK_NAME )
    makeAsciidocBuildInfoTask.group = TaskGroups.DOCUMENTATION
    makeAsciidocBuildInfoTask.description = 'Generates asciidoc artifact snippet'
    makeAsciidocBuildInfoTask.doLast {
      buildInfoDir.mkdirs()

      // GroupID, ArtifactID, Version table in artifact.txt
      def artifactTableFile = new File( buildInfoDir, "artifact.txt" )
      def artifactTable = """
        |.Artifact
        |[role="artifact", options="header,autowidth"]
        ||===================================================
        ||Group ID|Artifact ID|Version
        ||${ project.group }|${ project.name }|${ project.version }
        ||===================================================
        """.stripMargin()
      artifactTableFile.withWriter { out -> out.println( artifactTable ) }
    }

    // Declare inputs/outputs
    if( project.getBuildFile() != null && project.getBuildFile().exists() )
    {
      makeAsciidocBuildInfoTask.getInputs().file( project.getBuildFile() )
    }
    makeAsciidocBuildInfoTask.getOutputs().file( buildInfoDir )
  }
}
