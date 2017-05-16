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
package org.apache.polygene.gradle.dependencies

import groovy.transform.CompileStatic
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency

@CompileStatic
class PolygeneExtension
{
  private final Project project
  final Core core
  final Internals internals

  PolygeneExtension( Project project )
  {
    this.project = project
    this.core = new Core()
    this.internals = new Internals()
  }

  class Core
  {
    Dependency api = core( 'api' )
    Dependency spi = core( 'spi' )
    Dependency runtime = core( 'runtime' )
    Dependency bootstrap = core( 'bootstrap' )
    Dependency testsupport = core( 'testsupport' )
  }

  class Internals
  {
    Dependency testsupport = internal( 'testsupport-internal' )
  }

  private Dependency core( String name )
  {
    return dependency( 'core', name )
  }

  Dependency library( String name )
  {
    return dependency( 'libraries', name )
  }

  Dependency extension( String name )
  {
    return dependency( 'extensions', name )
  }

  Dependency tool( String name )
  {
    return dependency( 'tools', name )
  }

  Dependency internal( String name )
  {
    return dependency( 'internals', name )
  }

  private Dependency dependency( String group, String name )
  {
    project.dependencies.project( path: ":$group:$name" )
  }
}
