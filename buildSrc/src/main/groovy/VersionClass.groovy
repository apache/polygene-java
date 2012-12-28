/*
 * Copyright (c) 2010, Niclas Hedhman. All Rights Reserved.
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

import org.gradle.api.Project
import org.gradle.api.Plugin
import org.gradle.api.plugins.JavaPlugin

class VersionClass implements Plugin<Project>
{

  VersionClass()
  {
  }

  def void apply(Project project)
  {
    project.getPlugins().apply(JavaPlugin.class)
    def genSrc = 'generated-src/version'
    def generatedSrcDir = new File(project.buildDir, genSrc)

    def makeVersionClassTask = project.task('makeVersionClass') << {
      def now = new Date()
      def tmpGroup = project.name
      if( tmpGroup.startsWith("org.qi4j.core"))
      {
        tmpGroup = tmpGroup - ".core"
      }
      def outFilename = "java/" + tmpGroup.replace('.', '/') + "/BuildVersion.java"
      def outFile = new File(generatedSrcDir, outFilename)
      outFile.getParentFile().mkdirs()
      def f = new FileWriter(outFile)
      f.write('package ' + tmpGroup.replace('-', '_') + ';\n')
      f.write("""
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
""")
      f.close()
    }
    project.sourceSets {
      version {
        java {
          srcDir project.buildDir.name + '/' + genSrc + '/java'
        }
      }
    }
    makeVersionClassTask.getInputs().files(project.sourceSets.main.getAllSource())
    makeVersionClassTask.getOutputs().files(generatedSrcDir)
    if( project.getBuildFile() != null && project.getBuildFile().exists() )
    {
      makeVersionClassTask.getInputs().files(project.getBuildFile())
    }
    project.getTasks().getByName('compileJava').dependsOn('compileVersionJava')
    project.getTasks().getByName('compileVersionJava').dependsOn('makeVersionClass')
    project.getTasks().getByName('jar') {
      from project.sourceSets.version.output
    }
  }
}


