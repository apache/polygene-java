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
package org.apache.polygene.gradle.structure.tests

import groovy.transform.CompileStatic
import org.apache.polygene.gradle.TaskGroups
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.testing.Test
import org.gradle.language.base.plugins.LifecycleBasePlugin

// TODO Add profiling tasks (jfr or honest? flamegraphs?)
// TODO Add simple regression assertions, how? testing against a previous version?
@CompileStatic
class PerformanceTestsPlugin implements Plugin<Project>
{
  static class TaskNames
  {
    static final String PERFORMANCE_TEST = 'performanceTest'
    static final String PERFORMANCE_PROFILE = 'performanceProfile'
    static final String PERFORMANCE_CHECK = 'performanceCheck'
  }

  @Override
  void apply( final Project project )
  {
    def sourceSets = project.convention.getPlugin( JavaPluginConvention ).sourceSets
    sourceSets.create 'perf'
    project.dependencies.add 'perfImplementation', sourceSets.getByName( 'main' ).output
    project.dependencies.add 'perfImplementation', sourceSets.getByName( 'test' ).output
    project.dependencies.add 'perfImplementation', project.configurations.getByName( 'testImplementation' )
    project.dependencies.add 'perfRuntimeOnly', project.configurations.getByName( 'testRuntimeOnly' )
    project.tasks.getByName( LifecycleBasePlugin.CHECK_TASK_NAME ).dependsOn 'compilePerfJava'
    project.tasks.create( TaskNames.PERFORMANCE_TEST, Test, { Test task ->
      task.group = TaskGroups.PERFORMANCE
      task.description = 'Runs performance tests.'
      task.maxParallelForks = 1
      task.forkEvery = 1L
      task.testClassesDirs = sourceSets.getByName( 'perf' ).output.classesDirs
      task.classpath = sourceSets.getByName( 'perf' ).runtimeClasspath
      task.systemProperty 'jar.path', ( project.tasks.getByName( 'jar' ) as Jar ).archivePath
    } as Action<Test> )
  }
}
