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
package org.apache.polygene.gradle.structure.tools

import com.moowork.gradle.node.NodeExtension
import com.moowork.gradle.node.NodePlugin
import com.moowork.gradle.node.npm.NpmTask
import org.apache.polygene.gradle.BasePlugin
import org.apache.polygene.gradle.dependencies.DependenciesDeclarationExtension
import org.apache.polygene.gradle.dependencies.DependenciesPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

class NpmToolPlugin implements Plugin<Project>
{
  @Override
  void apply( Project project )
  {
    project.plugins.apply BasePlugin
    project.plugins.apply DependenciesPlugin
    project.plugins.apply NodePlugin
    def dependencies = project.rootProject.extensions.getByType( DependenciesDeclarationExtension )
    def node = project.extensions.getByType NodeExtension
    node.download = true
    node.version = dependencies.nodeVersions.node
    node.npmVersion = dependencies.nodeVersions.npm
    project.tasks.withType( NpmTask ) { NpmTask task ->
      task.group = 'node'
      task.workingDir = project.projectDir
    }
  }
}
