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
package org.apache.polygene.gradle.tasks

import groovy.transform.CompileStatic
import java.nio.file.Files
import java.util.function.BiConsumer
import org.gradle.api.Action
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.tasks.AbstractExecTask
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.logging.ConsoleRenderer
import org.gradle.process.ExecSpec

/**
 * Execute command and log standard output and error to files.
 *
 * See the {@literal stdoutFile} and {@literal stderrFile} properties.
 */
@CompileStatic
class ExecLogged extends AbstractExecTask<ExecLogged>
{
  @OutputFile
  File stdoutFile = project.file "$project.buildDir/log/${ getName() }/${ getName() }-stdout.log"

  @OutputFile
  File stderrFile = project.file "$project.buildDir/log/${ getName() }/${ getName() }-stderr.log"

  ExecLogged()
  {
    super( ExecLogged.class )
  }

  @TaskAction
  protected void exec()
  {
    doExecLogged stdoutFile, stderrFile, { OutputStream outStream, OutputStream errStream ->
      standardOutput = outStream
      errorOutput = errStream
      super.exec()
    }
  }

  /**
   * Execute command and log standard output and error to files.
   *
   * @param project Project reference
   * @param stdoutFile Standard output log file
   * @param stderrFile Standard error log file
   * @param specAction Execution specification
   */
  static void execLogged( Project project, File stdoutFile, File stderrFile, Action<? super ExecSpec> specAction )
  {
    doExecLogged stdoutFile, stderrFile, { OutputStream outStream, OutputStream errStream ->
      project.exec { ExecSpec spec ->
        spec.standardOutput = outStream
        spec.errorOutput = errStream
        specAction.execute spec
      }
    }
  }

  private static void doExecLogged( File stdoutFile, File stderrFile,
                                    BiConsumer<OutputStream, OutputStream> exec )
  {
    [ stdoutFile, stderrFile ].each { Files.createDirectories( it.parentFile.toPath() ) }
    def outStream, errStream
    if( stdoutFile.absolutePath == stderrFile.absolutePath )
    {
      outStream = stdoutFile.newOutputStream()
      errStream = outStream
    }
    else
    {
      outStream = stdoutFile.newOutputStream()
      errStream = stderrFile.newOutputStream()
    }
    try
    {
      exec.accept( outStream, errStream )
    }
    catch( Exception ex )
    {
      throw new GradleException( errorMessage( ex, stdoutFile, stderrFile ), ex )
    }
    finally
    {
      close outStream, errStream
    }
  }

  private static void close( Closeable... closeables )
    throws IOException
  {
    def errors = [ ] as List<IOException>
    closeables.each { Closeable closeable ->
      try
      {
        closeable.close()
      }
      catch( IOException ex )
      {
        errors.add( ex )
      }
    }
    if( !errors.empty )
    {
      def ex = new IOException( 'Failed to close some stream(s)' )
      errors.each { ex.addSuppressed it }
      throw ex
    }
  }

  private static String errorMessage( Exception ex, File stdoutFile, File stderrFile )
  {
    // TODO Remove use of Gradle internals
    def consoleRenderer = new ConsoleRenderer()
    return "${ ex.message }\n" +
           "\tSTDOUT ${ consoleRenderer.asClickableFileUrl( stdoutFile ) }\n" +
           "\tSTDERR ${ consoleRenderer.asClickableFileUrl( stderrFile ) }"
  }
}
