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
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.ContentType
import org.apache.http.entity.mime.MultipartEntityBuilder
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.polygene.gradle.BasePlugin
import org.apache.polygene.gradle.TaskGroups
import org.apache.polygene.gradle.structure.distributions.DistributionsPlugin
import org.apache.polygene.gradle.structure.manual.ManualPlugin
import org.apache.polygene.gradle.structure.reports.ReportsPlugin
import org.gradle.api.GradleException
import org.gradle.api.InvalidUserDataException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.execution.TaskExecutionGraph
import org.gradle.api.file.CopySpec
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.bundling.Zip
import org.gradle.language.base.plugins.LifecycleBasePlugin

@CompileStatic
class ReleasePlugin implements Plugin<Project>
{
  static class TaskNames
  {
    static final String RELEASE_ASF = 'releaseAsf'
    static final String PUBLISH_ASF_MAVEN = 'publishAsfMavenArtifacts'
    private static final String PREPARE_ASF_MAVEN = 'prepareAsfMavenBundle'
    private static final String UPLOAD_ASF_MAVEN = 'uploadAsfMavenBundle'
    private static final String CLOSE_ASF_MAVEN = 'closeAsfMavenRepository'
    private static final String CHECK_ASF_MAVEN = 'checkAsfMavenArtifacts'
    private static final String PROMOTE_ASF_MAVEN = 'promoteAsfMavenRepository'
    static final String PUBLISH_ASF_DIST = 'publishAsfDistributions'
    private static final String CHECKOUT_ASF_DIST = 'checkoutAsfDistributions'
    private static final String COPY_ASF_DIST = 'copyAsfDistributions'
    private static final String COMMIT_ASF_DIST = 'commitAsfDistributions'
    static final String PUBLISH_ASF_DOC = 'publishAsfDocumentation'
    private static final String CHECKOUT_ASF_DOC = 'checkoutAsfDocumentation'
    private static final String COPY_ASF_DOC = 'copyAsfDocumentation'
    private static final String COPY_ASF_DOC_LATEST = 'copyAsfDocumentationAsLatest'
    private static final String COMMIT_ASF_DOC = 'commitAsfDocumentation'
  }

  @Override
  void apply( final Project project )
  {
    project.plugins.apply BasePlugin
    project.gradle.taskGraph.whenReady { TaskExecutionGraph taskGraph ->
      def check = taskGraph.allTasks.any { task -> task.name.contains( 'Asf' ) }
      if( check )
      {
        checkAsfPreconditions( project )
      }
    }
    applyAsfRelease project
  }

  static void checkAsfPreconditions( Project project )
  {
    def releaseSpec = project.extensions.getByType ReleaseSpecExtension
    if( releaseSpec.developmentVersion )
    {
      throw new InvalidUserDataException(
        'Development version is unreleasable, please clean and retry with a -Dversion=' )
    }
    def polygeneWeb = new File( project.rootProject.projectDir.parentFile, 'polygene-website' )
    if( !polygeneWeb.exists() )
    {
      throw new InvalidUserDataException(
        'To perform ASF releases you need to clone the `polygene-website` repository under ../polygene-website' )
    }
    def polygeneDist = new File( project.rootProject.projectDir.parentFile, 'polygene-dist' )
    if( !polygeneDist.exists() )
    {
      throw new InvalidUserDataException(
        'To perform ASF releases you need to checkout the SVN dist directory under ../polygene-dist' )
    }
    // TODO Check Nexus credentials availability
    // TODO Check svn command line availability
  }

  static void applyAsfRelease( Project project )
  {
    Task releaseTask = project.tasks.create( TaskNames.RELEASE_ASF ) { Task task ->
      task.group = TaskGroups.RELEASE
      task.description = 'Rolls out an Apache Software Foundation release.'
    }
    def subTasks = [
      applyPublishAsfMavenArtifacts( project ),
      applyPublishAsfDistributions( project ),
      applyPublishAsfDocumentation( project )
    ]
    // Two upload strategies for now
    if( project.findProperty( 'useMavenBundle' ) )
    {
      // Use maven artifact bundle
      releaseTask.dependsOn subTasks
    }
    else
    {
      // Use :**:uploadArchives for now
      // TODO Remove this once the bundle strategy is done
      def releaseSpec = project.extensions.getByType ReleaseSpecExtension
      releaseSpec.publishedProjects.each { p ->
        releaseTask.dependsOn "${ p.path }:uploadArchives"
      }
      releaseTask.dependsOn subTasks.drop( 1 )
    }
  }

  static Task applyPublishAsfMavenArtifacts( Project project )
  {
    def releaseSpec = project.extensions.getByType ReleaseSpecExtension
    def distributions = project.rootProject.project ':distributions'
    def prepare = project.tasks.create( TaskNames.PREPARE_ASF_MAVEN, Zip ) { Zip task ->
      // TODO Consume distributions through configurations
      task.dependsOn "${ distributions.path }:${ DistributionsPlugin.TaskNames.STAGE_MAVEN_BINARIES }"
      task.from "${ distributions.buildDir }/stage/maven-binaries"
      task.into '.'
      task.destinationDir = project.file( "$project.buildDir/asf/maven" )
      task.baseName = "apache-polygene-${ project.version }-maven-artifacts"
      task.exclude '**/maven-metadata*.*'
    }
    def upload = project.tasks.create( TaskNames.UPLOAD_ASF_MAVEN ) { Task task ->
      task.dependsOn prepare
      // ASF Nexus instance is a Nexus Pro so has the Unpack Plugin that allow uploading
      // artifacts "en masse" as a ZIP file
      // TODO Ensure that we have the 'Unpack' privilege in ASF Nexus
      // Looks like we can upload 'Artifact Bundles' using Nexus UI
      task.doLast {
        def uploadUrl = releaseSpec.releaseVersion ?
                        'https://repository.apache.org/service/local/staging/deploy/maven2' :
                        'https://repository.apache.org/content/repositories/snapshots'
        CloseableHttpClient httpClient = HttpClients.createDefault()
        try
        {
          // TODO Add Nexus Authentication
          HttpPost post = new HttpPost( "$uploadUrl/content-compressed" )
          MultipartEntityBuilder builder = MultipartEntityBuilder.create();
          builder.addBinaryBody( 'fieldname',
                                 prepare.archivePath,
                                 ContentType.APPLICATION_OCTET_STREAM,
                                 prepare.archivePath.getName() )
          post.setEntity( builder.build() )
          CloseableHttpResponse response = httpClient.execute( post )
          if( response.statusLine.statusCode != 200 )
          {
            throw new GradleException( "Unable to upload maven artifacts to ASF Nexus, got ${ response.statusLine }" )
          }
        }
        finally
        {
          httpClient.close()
        }
      }
    }
    def close = project.tasks.create( TaskNames.CLOSE_ASF_MAVEN ) { Task task ->
      task.mustRunAfter upload
      // TODO Close Nexus repository
      task.enabled = false
    }
    def check = project.tasks.create( TaskNames.CHECK_ASF_MAVEN ) { Task task ->
      task.mustRunAfter close
      // TODO Run tests against binaries from Nexus staged repository
      task.enabled = false
    }
    def promote = project.tasks.create( TaskNames.PROMOTE_ASF_MAVEN ) { Task task ->
      task.mustRunAfter check
      // TODO Promote Nexus repository
      task.enabled = false
    }
    def publish = project.tasks.create( TaskNames.PUBLISH_ASF_MAVEN ) { Task task ->
      task.group = TaskGroups.RELEASE
      task.description = 'Publishes maven artifacts to ASF Nexus.'
      task.dependsOn upload, close, check, promote
    }
    return publish
  }

  static Task applyPublishAsfDistributions( Project project )
  {
    def distributions = project.rootProject.project ':distributions'
    def checkout = project.tasks.create( TaskNames.CHECKOUT_ASF_DIST ) { Task task ->
      // TODO SVN checkout ASF distribution directory
      task.enabled = false
    }
    def copy = project.tasks.create( TaskNames.COPY_ASF_DIST, Copy ) { Copy task ->
      task.mustRunAfter checkout
      // TODO Consume distributions through configurations
      task.dependsOn "${ distributions.path }:${ LifecycleBasePlugin.ASSEMBLE_TASK_NAME }"
      task.from new File( distributions.buildDir, 'distributions' )
      task.into new File( project.rootProject.projectDir.parentFile, 'polygene-dist' )
    }
    def commit = project.tasks.create( TaskNames.COMMIT_ASF_DIST ) { Task task ->
      task.mustRunAfter copy
      // TODO SVN commit ASF distribution directory
      task.enabled = false
    }
    def publish = project.tasks.create( TaskNames.PUBLISH_ASF_DIST ) { Task task ->
      task.group = TaskGroups.RELEASE
      task.description = 'Publishes distributions to ASF SVN.'
      task.dependsOn checkout, copy, commit
    }
    // TODO SVN Upload DISTRIBUTIONS using svn command line so credentials are handled outside of the build
    return publish
  }

  static Task applyPublishAsfDocumentation( Project project )
  {
    def releaseSpec = project.extensions.getByType ReleaseSpecExtension
    def manual = project.rootProject.project ':manual'
    def reports = project.rootProject.project ':reports'
    def checkout = project.tasks.create( TaskNames.CHECKOUT_ASF_DOC ) { Task task ->
      // TODO SVN checkout ASF distribution directory
      task.enabled = false
    }
    def copy = project.tasks.create( TaskNames.COPY_ASF_DOC, Copy ) { Copy task ->
      task.mustRunAfter checkout
      // TODO Consume documentation and reports through configurations
      task.dependsOn "${ manual.path }:${ ManualPlugin.TaskNames.WEBSITE }"
      task.dependsOn "${ reports.path }:${ ReportsPlugin.TaskNames.JAVADOCS }"
      def webRoot = new File( project.rootProject.projectDir.parentFile, 'polygene-website' )
      def dirName = releaseSpec.releaseVersion ? project.version : 'develop'
      task.destinationDir = webRoot
      task.from( new File( manual.buildDir, 'docs/website' ) ) { CopySpec spec ->
        spec.into "content/java/$dirName"
      }
      task.from( new File( reports.buildDir, 'docs/javadocs' ) ) { CopySpec spec ->
        spec.into "content/java/$dirName/javadocs"
      }
    }
    project.tasks.create( TaskNames.COPY_ASF_DOC_LATEST, Copy ) { Copy task ->
      def webRoot = new File( project.rootProject.projectDir.parentFile, 'polygene-website' )
      task.from new File( webRoot, "content/java/$project.version" )
      task.into new File( webRoot, "content/java/latest" )
      task.doFirst {
        if( !releaseSpec.releaseVersion )
        {
          throw new InvalidUserDataException( 'Development version cannot be `latest`.' )
        }
      }
    }
    def commit = project.tasks.create( TaskNames.COMMIT_ASF_DOC ) { Task task ->
      task.mustRunAfter copy
      // TODO SVN commit ASF documentation directory
      task.enabled = false
    }
    def publish = project.tasks.create( TaskNames.PUBLISH_ASF_DOC ) { Task task ->
      task.group = TaskGroups.RELEASE
      task.description = 'Publishes documentation to ASF HTTP.'
      task.dependsOn checkout, copy, commit
    }
    return publish
  }
}
