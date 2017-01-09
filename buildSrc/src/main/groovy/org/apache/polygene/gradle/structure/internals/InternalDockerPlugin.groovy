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

import com.palantir.gradle.docker.DockerExtension
import com.palantir.gradle.docker.PalantirDockerPlugin
import groovy.transform.CompileStatic
import org.apache.polygene.gradle.BasePlugin
import org.apache.polygene.gradle.code.PublishNaming
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.language.base.plugins.LifecycleBasePlugin

@CompileStatic
class InternalDockerPlugin implements Plugin<Project>
{
  @Override
  void apply( Project project )
  {
    project.plugins.apply BasePlugin
    project.plugins.apply PalantirDockerPlugin
    def dockerExtension = project.extensions.getByType( DockerExtension )
    dockerExtension.name = "org.apache.polygene:${ PublishNaming.publishedNameFor( project.path ) }"
    dockerExtension.dockerfile = 'src/main/docker/Dockerfile'
    dockerExtension.files 'src/main/docker'
    def dockerHost = System.getenv( 'DOCKER_HOST' ) as Object
    [ project.tasks.getByName( 'dockerClean' ),
      project.tasks.getByName( 'dockerPrepare' ),
      project.tasks.getByName( 'docker' )
    ].each { Task dockerTask ->
      dockerTask.inputs.property 'dockerHostEnv', dockerHost
      dockerTask.onlyIf { dockerHost }
    }
    project.tasks.getByName( LifecycleBasePlugin.BUILD_TASK_NAME ).dependsOn 'docker'
  }
}
