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
import org.gradle.api.Project

/**
 * Provide release approved projects.
 *
 * There's no up-to-date checking on Gradle extensions.
 * Depend on {@link ReleaseApprovedProjectsTask} to get a good up-to-date behavior.
 */
@CompileStatic
class ReleaseSpecExtension
{
  static final String NAME = 'releaseSpec'

  private final Project project

  final Set<Project> approvedProjects
  final Set<Project> unapprovedProjects
  final Set<Project> publishedProjects

  ReleaseSpecExtension( Project project )
  {
    this.project = project
    def spec = new ModuleReleaseSpec()
    def candidateProjects = project.allprojects.findAll { p -> p.file( 'dev-status.xml' ).exists() }
    approvedProjects = candidateProjects.findAll { p -> spec.satisfiedBy p }
    unapprovedProjects = candidateProjects.minus approvedProjects
    publishedProjects = releaseVersion ? approvedProjects : candidateProjects
  }

  boolean isDevelopmentVersion()
  {
    return project.version == '0'
  }

  boolean isSnapshotVersion()
  {
    return project.version.toString().contains( 'SNAPSHOT' )
  }

  boolean isReleaseVersion()
  {
    return !snapshotVersion && !developmentVersion
  }
}
