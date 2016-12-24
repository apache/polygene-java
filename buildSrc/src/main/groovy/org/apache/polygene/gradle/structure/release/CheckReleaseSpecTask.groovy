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
package org.apache.polygene.gradle.structure.release

import groovy.transform.CompileStatic
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.tasks.TaskAction

@CompileStatic
class CheckReleaseSpecTask extends DefaultTask
{
  CheckReleaseSpecTask()
  {
    description = 'Ensure that no releasable module depend on module(s) that don\'t fit the release criteria.'
    dependsOn {
      project.extensions.getByType( ReleaseSpecExtension ).approvedProjects
             .collect { each -> each.configurations.getByName( 'runtime' ) }
    }
  }

  @TaskAction
  void check()
  {
    Map<Project, Set<ProjectDependency>> notReleasable = [ : ]
    def approvedProjects = project.extensions.getByType( ReleaseSpecExtension ).approvedProjects
    approvedProjects.each { approvedProject ->
      def projectDependencies = approvedProject.configurations.getByName( 'runtime' ).allDependencies.findAll {
        it instanceof ProjectDependency
      } as Set<ProjectDependency>
      projectDependencies.each { dep ->
        def depNotReleaseApproved = approvedProjects.findAll { rp ->
          rp.group == dep.dependencyProject.group && rp.name == dep.dependencyProject.name
        }.isEmpty()
        if( depNotReleaseApproved )
        {
          if( !notReleasable[ approvedProject ] )
          {
            notReleasable[ approvedProject ] = [ ] as Set
          }
          notReleasable[ approvedProject ] << dep
        }
      }
    }
    if( !notReleasable.isEmpty() )
    {
      def errorMessage = new StringBuilder()
      errorMessage << "At least one releasable module depends on module(s) that don't fit the release criteria!\n"
      errorMessage << "\tOffending module -> Non releasable dependencies\n"
      notReleasable.each { k, v ->
        def noRlsDeps = v.collect { d -> ':' + d.dependencyProject.group + ':' + d.dependencyProject.name }
        errorMessage << "\t$k -> ${ noRlsDeps })\n"
      }
      errorMessage << "Check the dev-status.xml file content in each modules directory."
      throw new GradleException( errorMessage.toString() )
    }
  }
}
