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
package org.apache.polygene.gradle.structure.core

import groovy.transform.CompileStatic
import org.apache.polygene.gradle.code.PublishedCodePlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile

import static org.apache.polygene.gradle.structure.ProjectGroupTasks.configureProjectGroupTasks

@CompileStatic
class CorePlugin implements Plugin<Project>
{
  @Override
  void apply( Project project )
  {
    project.plugins.apply PublishedCodePlugin
    configureJava( project )
    configureProjectGroupTasks( "core", project )
  }

  private static void configureJava( Project project )
  {
    project.tasks.withType( JavaCompile ) { JavaCompile task ->
      // Unchecked warnings for non-test compilations
      if( !task.name.toLowerCase( Locale.US ).contains( 'test' ) )
      {
        task.options.compilerArgs << '-Xlint:unchecked'
      }
    }
  }
}
