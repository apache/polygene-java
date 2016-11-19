package org.apache.zest.gradle

import org.gradle.api.plugins.ApplicationPlugin
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.plugins.HelpTasksPlugin
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.language.base.plugins.LifecycleBasePlugin

class TaskGroups
{
  static final String HELP = HelpTasksPlugin.HELP_GROUP
  static final String BUILD = LifecycleBasePlugin.BUILD_GROUP
  static final String VERIFICATION = LifecycleBasePlugin.VERIFICATION_GROUP
  static final String DOCUMENTATION = JavaBasePlugin.DOCUMENTATION_GROUP
  static final String DISTRIBUTION = ApplicationPlugin.APPLICATION_GROUP
  static final String DISTRIBUTION_VERIFICATION = 'distribution verification'
  static final String RELEASE = 'release'
  static final String RELEASE_VERIFICATION = 'release verification'
  static final String UPLOAD = BasePlugin.UPLOAD_GROUP
}
