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
package org.apache.polygene.gradle

import groovy.transform.CompileStatic
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.plugins.HelpTasksPlugin
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.language.base.plugins.LifecycleBasePlugin

@CompileStatic
class TaskGroups
{
  static final String HELP = HelpTasksPlugin.HELP_GROUP
  static final String BUILD = LifecycleBasePlugin.BUILD_GROUP
  static final String VERIFICATION = LifecycleBasePlugin.VERIFICATION_GROUP
  static final String DOCUMENTATION = JavaBasePlugin.DOCUMENTATION_GROUP
  static final String DISTRIBUTION = 'distribution'
  static final String DISTRIBUTION_VERIFICATION = 'distribution verification'
  static final String PERFORMANCE = 'performance'
  static final String PERFORMANCE_VERIFICATION = 'performance verification'
  static final String RELEASE = 'release'
  static final String RELEASE_VERIFICATION = 'release verification'
  static final String UPLOAD = BasePlugin.UPLOAD_GROUP
  static final String SAMPLES = 'samples'
}
