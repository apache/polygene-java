/*
 * Copyright (c) 2011, Niclas Hedhman. All Rights Reserved.
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
package org.qi4j.gradle.plugin.documentation;


import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

// TODO: use proper project variables for 'build' and other locations
// TODO: extract out every location into configurable property
// TODO: try to use dependencies for FOP and execute within the same JVM.
// TODO: move the bulk of resources into this plugin, instead of sitting in the project.
class Documentation extends DefaultTask
{
  String getVersion()
  { return project.version }

  @TaskAction
  def void generate()
  {
    new File('build/docs/manual').mkdirs()
    new File('build/tmp/docs').mkdirs()
    generateXDoc()
    generateChunkedHtml()
    generateSingleHtml()
    generatePdf()
  }


  def void generateXDoc()
  {
    project.exec {
      executable = 'asciidoc'
      def resourcesDir = 'src/docs/resources'
      def asciidocConfigFile = 'src/conf/asciidoc.conf'
      def docbookConfigFile = 'src/conf/docbook45.conf'
      def linkimagesConfigFile = 'src/conf/linkedimages.conf'
      def xdocOutputFile = 'build/tmp/docs/xdoc-temp.xml'
      def asciiDocFile = 'src/docs/index.txt'
      args = [
              '--attribute', 'revnumber=' + getVersion(),
              '--attribute', 'importdir=' + resourcesDir,
              '--backend', 'docbook',
              '--attribute', 'docinfo1',
              '--doctype', 'book',
              '--conf-file=' + asciidocConfigFile,
              '--conf-file=' + docbookConfigFile,
              '--conf-file=' + linkimagesConfigFile,
              '--out-file', xdocOutputFile,
              asciiDocFile
      ]
    }
  }

  def void generateChunkedHtml() {
    project.copy {
      from 'src/resources'
      into 'build/docs/manual/'
      include '**'
    }

    project.exec {
      String xsltFile = 'src/xsl/chunked.xsl'
      executable = 'xsltproc'
      args = [
              '--nonet',
              '--output', 'build/docs/manual/',
              xsltFile,
              'build/tmp/docs/xdoc-temp.xml'
      ]
    }
  }

  def void generateSingleHtml()
  {
    project.exec {
      String xsltFile = 'src/xsl/xhtml.xsl'
      executable = 'xsltproc'
      args = [
              '--nonet',
              '--output', 'build/docs/manual/qi4j-manual.html',
              xsltFile,
              'build/tmp/docs/xdoc-temp.xml'
      ]
    }
  }

  def void generatePdf()
  {
// $ xsltproc --nonet ../docbook-xsl/fo.xsl article.xml > article.fo
// $ fop article.fo article.pdf

    project.exec {
      String xsltFile = 'src/xsl/fo.xsl'
      executable = 'xsltproc'
      args = [
              '--nonet',
              '--output', 'build/tmp/docs/qi4j-manual.fo',
              xsltFile,
              'build/tmp/docs/xdoc-temp.xml'
      ]
    }
    project.exec {
      executable = 'fop'
      args = [
              'build/tmp/docs/qi4j-manual.fo',
              'build/docs/manual/qi4j-manual.pdf'
      ]
    }
  }
}
