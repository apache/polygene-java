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
package org.apache.polygene.gradle.version

import groovy.transform.CompileStatic
import org.gradle.api.Project
import org.gradle.api.Plugin
import org.gradle.api.Task
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.bundling.Jar

// TODO:release:perf Placeholder for date for dev versions
// TODO:release:perf Add git data, placeholders for dev versions
@CompileStatic
class VersionClassPlugin implements Plugin<Project>
{
  def void apply( Project project )
  {
    project.getPlugins().apply( JavaPlugin.class )
    def genSrc = 'generated-src/version'
    def generatedSrcDir = new File( project.buildDir, genSrc )

    Task makeVersionClassTask = project.task( 'makeVersionClass' )
    makeVersionClassTask.doLast {
      def now = new Date()
      def tmpGroup = project.name
      if( tmpGroup.startsWith( "org.apache.polygene.core" ) )
      {
        tmpGroup = tmpGroup - ".core"
      }
      tmpGroup = tmpGroup.replace( '-', '_' )
      def outFilename = "java/" + tmpGroup.replace( '.', '/' ) + "/BuildVersion.java"
      def outFile = new File( generatedSrcDir, outFilename )
      outFile.getParentFile().mkdirs()
      def f = new FileWriter( outFile )
      f.write( 'package ' + tmpGroup + ';\n' )
      f.write( """
/**
 * Simple class for storing the version derived from the build system.
 *
 */
public interface BuildVersion
{
    /** The version of the project from the gradle build.gradle file. */
    String VERSION = \"""" + project.version + """\";

    /** The name of the project from the gradle build.gradle file. */
    String NAME = \"""" + project.name + """\";

    /** The group of the project from the gradle build.gradle file. */
    String GROUP = \"""" + project.group + """\";

    /** The date this file was generated, usually the last date that the project was modified. */
    String DATE = \"""" + now + """\";

    /** The full details of the version, including the build date. */
    String DETAILED_VERSION = GROUP + ":" + NAME + ":" + VERSION + " " + DATE;
}\n
""" )
      f.close()
    }
    def sourceSets = project.convention.getPlugin( JavaPluginConvention ).sourceSets
    sourceSets.create( "version" ) { SourceSet sourceSet ->
      sourceSet.java { SourceDirectorySet dirSet ->
        dirSet.srcDir project.buildDir.name + '/' + genSrc + '/java'
      }
    }
    makeVersionClassTask.getInputs().files( sourceSets.getByName( 'main' ).allSource )
    makeVersionClassTask.getOutputs().file( generatedSrcDir )
    if( project.getBuildFile() != null && project.getBuildFile().exists() )
    {
      makeVersionClassTask.getInputs().files( project.getBuildFile() )
    }
    project.getTasks().getByName( 'compileJava' ).dependsOn( 'compileVersionJava' )
    project.getTasks().getByName( 'compileVersionJava' ).dependsOn( 'makeVersionClass' )
    project.getTasks().getByName( 'jar' ) { Jar task ->
      task.from sourceSets.getByName( 'version' ).output
    }
  }
}
