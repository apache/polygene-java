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
import org.apache.polygene.gradle.structure.manual.AsciidocBuildInfoPlugin
import org.apache.polygene.gradle.structure.release.ReleaseSpecExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.external.javadoc.StandardJavadocDocletOptions
import org.gradle.jvm.tasks.Jar
import org.nosphere.honker.gradle.HonkerCheckTask
import org.nosphere.honker.gradle.HonkerExtension
import org.nosphere.honker.gradle.HonkerGenDependenciesTask
import org.nosphere.honker.gradle.HonkerGenLicenseTask
import org.nosphere.honker.gradle.HonkerGenNoticeTask
import org.nosphere.honker.gradle.HonkerLicenseOverrideCandidate

@CompileStatic
class PublishedCodePlugin implements Plugin<Project>
{
  @Override
  void apply( final Project project )
  {
    project.plugins.apply CodePlugin
    configureJavadoc project
    configureHonker( project )
    project.plugins.apply VersionClassPlugin
    project.plugins.apply AsciidocBuildInfoPlugin
    applySupplementaryArchives project
    project.plugins.apply PublishingPlugin
  }

  static void configureJavadoc( Project project )
  {
    def releaseSpec = project.extensions.getByType ReleaseSpecExtension
    project.tasks.withType( Javadoc ) { Javadoc task ->
      task.onlyIf { !releaseSpec.developmentVersion }
      def options = task.options as StandardJavadocDocletOptions
      options.encoding = 'UTF-8'
      options.docEncoding = 'UTF-8'
      options.charSet = 'UTF-8'
      options.noTimestamp = true
      options.links = [
        'http://docs.oracle.com/javase/8/docs/api/',
        'https://stleary.github.io/JSON-java/',
        'http://junit.org/junit4/javadoc/latest/'
      ]
      // exclude '**/internal/**'
    }
  }

  private static void configureHonker( Project project )
  {
    def releaseSpec = project.extensions.getByType ReleaseSpecExtension
    if( releaseSpec.developmentVersion )
    {
      return
    }
    project.plugins.apply 'org.nosphere.honker'
    def honkerGenDependencies = project.tasks.getByName( 'honkerGenDependencies' ) as HonkerGenDependenciesTask
    def honkerGenLicense = project.tasks.getByName( 'honkerGenLicense' ) as HonkerGenLicenseTask
    def honkerGenNotice = project.tasks.getByName( 'honkerGenNotice' ) as HonkerGenNoticeTask
    def honkerCheck = project.tasks.getByName( 'honkerCheck' ) as HonkerCheckTask
    [ honkerGenDependencies, honkerGenLicense, honkerGenNotice, honkerCheck ].group = null
    def javaConvention = project.convention.getPlugin JavaPluginConvention
    def mainSourceSet = javaConvention.sourceSets.getByName 'main'
    mainSourceSet.output.dir( [ builtBy: honkerGenDependencies ] as Map<String, Object>,
                              honkerGenDependencies.outputDir )
    mainSourceSet.output.dir( [ builtBy: honkerGenLicense ] as Map<String, Object>,
                              honkerGenLicense.outputDir )
    mainSourceSet.output.dir( [ builtBy: honkerGenNotice ] as Map<String, Object>,
                              honkerGenNotice.outputDir )
    def honker = project.extensions.getByType HonkerExtension
    // Project License, applied to all submodules
    honker.license 'Apache 2'
    // Dependencies (transitive or not) with no license information, overriding them
    honker.licenseOverride { HonkerLicenseOverrideCandidate candidate ->
      if( candidate.group == 'asm' || candidate.module == 'prefuse-core' )
      {
        candidate.license = 'BSD 3-Clause'
      }
      if( candidate.group == 'javax.json'
        || candidate.group == 'javax.websocket'
        || candidate.group == 'javax.xml.bind' )
      {
        candidate.license = 'CDDL'
      }
      if( candidate.group == 'org.apache.httpcomponents'
        || candidate.group == 'net.java.dev.jna'
        || candidate.group == 'lucene'
        || candidate.group == 'jdbm'
        || candidate.group == 'org.osgi'
        || candidate.group.startsWith( 'org.restlet' ) )
      {
        candidate.license = 'Apache 2'
      }
    }
    honkerGenNotice.header = 'Apache Polygene'
    honkerGenNotice.footer = 'This product includes software developed at\n' +
                             'The Apache Software Foundation (http://www.apache.org/).\n'
    project.tasks.getByName( 'check' ).dependsOn honkerCheck
  }

  private static void applySupplementaryArchives( Project project )
  {
    def releaseSpec = project.extensions.getByType ReleaseSpecExtension
    def javaConvention = project.convention.getPlugin JavaPluginConvention
    def sourceJar = project.tasks.create( 'sourceJar', Jar ) { Jar task ->
      task.description = 'Builds -sources.jar'
      task.classifier = 'sources'
      task.from javaConvention.sourceSets.getByName( 'main' ).allSource
    }
    def testSourceJar = project.tasks.create( 'testSourceJar', Jar ) { Jar task ->
      task.description = 'Builds -testsources.jar'
      task.classifier = 'testsources'
      task.onlyIf { !releaseSpec.developmentVersion }
      task.from javaConvention.sourceSets.getByName( 'test' ).allSource
    }
    def javadoc = project.tasks.getByName( 'javadoc' ) as Javadoc
    def javadocJar = project.tasks.create( 'javadocJar', Jar ) { Jar task ->
      task.description = 'Builds -javadoc.jar'
      task.classifier = 'javadoc'
      task.onlyIf { !releaseSpec.developmentVersion }
      task.from javadoc.destinationDir
      task.dependsOn javadoc
    }
    project.artifacts.add 'archives', sourceJar
    if( !releaseSpec.developmentVersion )
    {
      project.artifacts.add 'archives', testSourceJar
      project.artifacts.add 'archives', javadocJar
    }
  }
}
