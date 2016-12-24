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
package org.apache.polygene.gradle.code

import groovy.transform.CompileStatic
import java.nio.file.Files
import org.apache.polygene.gradle.util.Licensing
import org.gradle.api.Project
import org.gradle.api.Plugin
import org.gradle.api.Task
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.bundling.Jar

// TODO Add build date, placeholder for dev versions
// TODO Add git data, placeholders for dev versions
@CompileStatic
class VersionClassPlugin implements Plugin<Project>
{
  @Override
  void apply( Project project )
  {
    project.getPlugins().apply JavaPlugin.class
    def genSrc = 'generated-src/version'
    project.task( 'generateVersionClass' ) { Task task ->
      def headerFile = project.rootProject.file 'etc/header.txt'
      def generatedSrcDir = new File( project.buildDir, genSrc )
      task.inputs.file headerFile
      task.inputs.property 'projectVersion', project.version
      task.inputs.property 'projectName', project.name
      task.inputs.property 'projectGroup', project.group
      task.outputs.file generatedSrcDir
      task.doLast {
        def publishedName = PublishNaming.publishedNameFor project.path
        def packageName = "${ publishedName }".replace '-', '_'
        def outFilename = "java/" + packageName.replace( '.', '/' ) + "/BuildVersion.java"
        def outFile = new File( generatedSrcDir, outFilename )
        Files.createDirectories outFile.parentFile.toPath()
        outFile.withWriter( 'UTF-8' ) { BufferedWriter out ->
          out.write Licensing.withLicenseHeader( headerFile.text, 'java' )
          out.writeLine """
        package ${ packageName };

        /**
         * Build version.
         */
        public interface BuildVersion
        {
            /** {@literal ${ project.group }}. */
            String GROUP = \"${ project.group }\";
        
            /** {@literal ${ publishedName }}. */
            String NAME = \"${ publishedName }\";
        
            /** {@literal ${ project.version }}. */
            String VERSION = \"${ project.version }\";
        
            /** {@literal ${ project.group }:${ publishedName }:${ project.version }}. */
            String GAV = GROUP + ':' + NAME + ':' + VERSION;
        }
        """.stripIndent().trim()
        }
      }
    }
    def sourceSets = project.convention.getPlugin( JavaPluginConvention ).sourceSets
    sourceSets.create( "version" ) { SourceSet sourceSet ->
      sourceSet.java { SourceDirectorySet dirSet ->
        dirSet.srcDir project.buildDir.name + '/' + genSrc + '/java'
      }
    }
    project.tasks.getByName( 'compileJava' ).dependsOn 'compileVersionJava'
    project.tasks.getByName( 'compileVersionJava' ).dependsOn 'generateVersionClass'
    project.tasks.getByName( 'jar' ) { Jar task ->
      task.from sourceSets.getByName( 'version' ).output
    }
  }
}
