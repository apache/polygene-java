/*
 * Copyright (c) 2012, Paul Merlin. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 */

import org.gradle.api.Project
import org.gradle.api.Plugin

class AsciidocBuildInfo implements Plugin<Project>
{

    final static TASK_NAME = 'makeAsciidocBuildInfo'

    AsciidocBuildInfo()
    {
    }

    def void apply( Project project )
    {
        def buildInfoDir = new File( project.buildDir, "docs/buildinfo" );

        def makeAsciidocBuildInfoTask = project.task( TASK_NAME ) << {
            buildInfoDir.mkdirs()

            // GroupID, ArtifactID, Version table in artifact.txt
            def artifactTableFile = new File( buildInfoDir, "artifact.txt" )
            def artifactTable = """
                |.Artifact
                |[role="artifact", options="header,autowidth"]
                ||===================================================
                ||Group ID|Artifact ID|Version
                ||${project.group}|${project.name}|${project.version}
                ||===================================================
                """.stripMargin()
            artifactTableFile.withWriter { out -> out.println( artifactTable ) }
        }

        // Declare inputs/outputs
        if( project.getBuildFile() != null && project.getBuildFile().exists() )
        {
            makeAsciidocBuildInfoTask.getInputs().files(project.getBuildFile())
        }
        makeAsciidocBuildInfoTask.getOutputs().files( buildInfoDir )
    }

}
