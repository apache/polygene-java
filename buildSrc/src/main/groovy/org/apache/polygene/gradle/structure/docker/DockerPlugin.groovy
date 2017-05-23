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
package org.apache.polygene.gradle.structure.docker

import com.bmuschko.gradle.docker.DockerExtension
import com.bmuschko.gradle.docker.DockerRemoteApiPlugin
import com.bmuschko.gradle.docker.tasks.DockerVersion
import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage
import groovy.transform.CompileStatic
import org.apache.polygene.gradle.BasePlugin
import org.apache.polygene.gradle.code.CodePlugin
import org.apache.polygene.gradle.code.PublishNaming
import org.apache.polygene.gradle.dependencies.DependenciesDeclarationExtension
import org.apache.polygene.gradle.dependencies.DependenciesPlugin
import org.apache.tools.ant.filters.ReplaceTokens
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.CopySpec
import org.gradle.api.logging.LogLevel

@CompileStatic
class DockerPlugin implements Plugin<Project>
{
  static class TaskNames
  {
    static final String CHECK_DOCKER_CONNECTIVITY = 'checkDockerConnectivity'
  }

  private final String dockerMachineName = System.getenv( 'DOCKER_MACHINE_NAME' )
  private final String dockerHost = System.getenv( 'DOCKER_HOST' )
  private final String dockerCertPath = System.getenv( 'DOCKER_CERT_PATH' )

  @Override
  void apply( Project project )
  {
    project.plugins.apply BasePlugin
    project.plugins.apply DependenciesPlugin
    applyDockerPlugin( project )
    applyDockerSwitch( project )
    applyDockerBuildImage( project )
  }

  private void applyDockerPlugin( Project project )
  {
    project.plugins.apply DockerRemoteApiPlugin
    def dockerExtension = project.extensions.getByType DockerExtension
    // TLS support
    if( dockerCertPath )
    {
      dockerExtension.certPath = new File( dockerCertPath )
    }
  }

  private void applyDockerSwitch( Project project )
  {
    project.tasks.create( TaskNames.CHECK_DOCKER_CONNECTIVITY, DockerVersion, { DockerVersion task ->
      task.onError = { ex ->
        // Disable Docker for this build
        project.rootProject.extensions.extraProperties.set( CodePlugin.DOCKER_DISABLED_EXTRA_PROPERTY, true )
        if( project.hasProperty( 'skipDocker' ) )
        {
            project.logger.lifecycle 'skipDocker property is set, all Docker tasks will be SKIPPED'
        }
        else if( project.logger.isEnabled( LogLevel.INFO ) )
        {
          project.logger.info 'Unable to connect to Docker, all Docker tasks will be SKIPPED', ex
        }
        else
        {
          project.logger.lifecycle "Unable to connect to Docker, all Docker tasks will be SKIPPED\n  ${ ( ( Exception ) ex ).message }"
        }
      }
      task.onComplete = {
        if( project.hasProperty( 'skipDocker' ) )
        {
          // Disable Docker for this build
          project.rootProject.extensions.extraProperties.set( CodePlugin.DOCKER_DISABLED_EXTRA_PROPERTY, true )
          project.logger.lifecycle 'skipDocker property is set, all Docker tasks will be SKIPPED'
        }
      }
    } as Action<DockerVersion> )
  }

  private void applyDockerBuildImage( Project project )
  {
    def classesTask = project.tasks.getByName 'classes'
      def dockers = project.file('src/main/resources/docker')
    def dependencies = project.rootProject.extensions.getByType( DependenciesDeclarationExtension )
    dockers.eachDir { File dockerDir ->
      def dockerName = dockerDir.name
      def buildDockerfileTaskName = "build${ dockerName.capitalize() }Dockerfile"
      def buildImageTaskName = "build${ dockerName.capitalize() }DockerImage"
      def tmpDir = project.file "${ project.buildDir }/tmp/docker/${ dockerName }"
      tmpDir.mkdirs()
      def buildDockerfileTask = project.tasks.create( buildDockerfileTaskName ) { Task task ->
        task.description = "Build $dockerName Dockerfile"
        task.inputs.property 'dockerImagesVersions', dependencies.dockerImagesVersions
        task.inputs.dir dockerDir
        task.outputs.dir tmpDir
        // Filter Dockerfile for image versions from dependencies declaration
        task.doFirst {
          project.copy { CopySpec spec ->
            spec.from( dockerDir ) { CopySpec unfiltered ->
              unfiltered.exclude 'Dockerfile'
            }
            spec.from( dockerDir ) { CopySpec filtered ->
              filtered.include 'Dockerfile'
              filtered.filter ReplaceTokens, tokens: dependencies.dockerImagesVersions
            }
            spec.into tmpDir
          }
        }
      }
      def buildImageTask = project.tasks.create( buildImageTaskName, DockerBuildImage, { DockerBuildImage task ->
        task.description = "Build $dockerName Docker image"
        task.inputDir = tmpDir
        task.dockerFile = new File( tmpDir, 'Dockerfile' )
          task.tag = "org.apache.polygene:${PublishNaming.publishedNameFor ":testsupport:docker-$dockerName"}"
      } as Action<DockerBuildImage> )
      [ buildDockerfileTask, buildImageTask ].each { Task task ->
        task.group = 'docker'
        task.dependsOn TaskNames.CHECK_DOCKER_CONNECTIVITY
        task.onlyIf { !project.rootProject.findProperty( CodePlugin.DOCKER_DISABLED_EXTRA_PROPERTY ) }
        task.inputs.property 'dockerMachineName', dockerMachineName
        task.inputs.property 'dockerHostEnv', dockerHost
        task.inputs.property 'dockerCertPath', dockerCertPath
      }
      buildImageTask.dependsOn buildDockerfileTask
      // Ensure that all Docker images are built alongside this project
      // This is a bit of a stretch but works for now
      classesTask.dependsOn buildImageTask
    }
  }
}
