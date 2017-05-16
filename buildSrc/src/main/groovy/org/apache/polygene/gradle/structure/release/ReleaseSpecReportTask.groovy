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
import org.gradle.api.tasks.TaskAction

@CompileStatic
class ReleaseSpecReportTask extends DefaultTask
{
  ReleaseSpecReportTask()
  {
    description = 'Report module(s) that do or don\'t fit the release criteria.'
    dependsOn {
      project.extensions.getByType( ReleaseSpecExtension ).approvedProjects
             .collect { each -> each.configurations.getByName( 'runtime' ) }
    }
  }

  @TaskAction
  void report()
  {
    def releaseSpec = project.extensions.getByType( ReleaseSpecExtension )
    println 'Approved Projects'
    releaseSpec.approvedProjects.each { println "\t${ it.path }" }
    println 'NOT Approved Projects'
    releaseSpec.unapprovedProjects.each { println "\t${ it.path }" }
  }
}
