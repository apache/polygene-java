/*
 * Copyright (c) 2011, Niclas Hedhman. All Rights Reserved.
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
package org.qi4j.gradle.plugin;

import org.gradle.api.Project

class ModuleReleaseSpecification
{
  def boolean satisfiedBy( Project project )
  {
    def devStatusFile = new File( project.projectDir, "dev-status.xml" )
    if( !devStatusFile.exists() )
    {
      return false
    }
    def module = new XmlSlurper().parse( devStatusFile )
    def codebase = module.status.codebase.text()
    def docs = module.status.documentation.text()
    def tests = module.status.unittests.text()
    def satisfied = ( codebase == 'none' && docs == 'complete' && tests != 'complete' )
    satisfied |= ( codebase != 'early' && docs == 'good' && tests == 'good' )
    satisfied |= ( codebase != 'beta' && docs == 'brief' && tests == 'some' )
    satisfied |= ( codebase == 'stable' )
    satisfied |= ( codebase == 'mature' )
    println "$project.name($satisfied) -> $codebase, $docs, $tests"
    return satisfied
  }
}