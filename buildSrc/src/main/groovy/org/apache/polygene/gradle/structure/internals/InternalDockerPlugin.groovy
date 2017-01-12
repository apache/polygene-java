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
package org.apache.polygene.gradle.structure.internals

import com.bmuschko.gradle.docker.DockerExtension
import com.bmuschko.gradle.docker.DockerRemoteApiPlugin
import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage
import groovy.transform.CompileStatic
import org.apache.polygene.gradle.BasePlugin
import org.apache.polygene.gradle.code.PublishNaming
import org.apache.polygene.gradle.dependencies.DependenciesPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

@CompileStatic
class InternalDockerPlugin implements Plugin<Project>
{
  private final String dockerMachineName = System.getenv( 'DOCKER_MACHINE_NAME' )
  private final String dockerHost = System.getenv( 'DOCKER_HOST' )
  private final String dockerCertPath = System.getenv( 'DOCKER_CERT_PATH' )

  @Override
  void apply( Project project )
  {
    project.plugins.apply BasePlugin
    project.plugins.apply DependenciesPlugin
    applyDockerPlugin( project )
    applyDockerBuildImage( project )
  }

  private void applyDockerPlugin( Project project )
  {
    project.plugins.apply DockerRemoteApiPlugin
    def dockerExtension = project.extensions.getByType DockerExtension
    dockerExtension.certPath = dockerCertPath ?
                               new File( dockerCertPath ) :
                               new File( System.getProperty( 'user.home' ), '.boot2docker/certs/boot2docker-vm' )
  }

  private void applyDockerBuildImage( Project project )
  {
    def classesTask = project.tasks.getByName 'classes'
    def dockers = project.file( 'src/main/docker' )
    dockers.eachDir { File dockerDir ->
      def dockerName = dockerDir.name
      def taskName = "build${ dockerName.capitalize() }DockerImage"
      project.tasks.create( taskName, DockerBuildImage ) { DockerBuildImage task ->
        task.group = 'docker'
        task.description = "Build $dockerName Docker image"
        task.onlyIf { dockerHost }
        task.inputs.property 'dockerMachineName', dockerMachineName
        task.inputs.property 'dockerHostEnv', dockerHost
        task.inputs.property 'dockerCertPath', dockerCertPath
        task.inputDir = dockerDir
        task.dockerFile = new File( dockerDir, 'Dockerfile' )
        task.tag = "org.apache.polygene:${ PublishNaming.publishedNameFor ":internals:docker-$dockerName" }"
        // Ensure that all Docker images are built alongside this project
        // This is a bit of a stretch but works for now
        classesTask.dependsOn task
      }
    }
  }
}
