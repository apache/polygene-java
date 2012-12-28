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
package org.qi4j.gradle.plugin;


import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

// TODO: use proper project variables for 'src' and other locations
// TODO: extract out every location into configurable property
// TODO: try to use dependencies for FOP and execute within the same JVM.
// TODO: move the bulk of resources into this plugin, instead of sitting in the project.
class Documentation extends DefaultTask
{
  private String docName

  private String docType

  @TaskAction
  def void generate()
  {
    def userHome = new File(System.getProperty("user.home"))
    def snippetDir = new File(userHome, ".asciidoc/filters/snippet").absoluteFile
    if( !snippetDir.exists() )
    {
      println "Installing [snippet] into $snippetDir"
      snippetDir.mkdirs()
      project.copy {
        from "$project.rootDir/buildSrc/src/bin"
        into snippetDir
        include 'snippet.*'
      }
      ant.chmod(dir: snippetDir, perm: "755", includes: "snippet.py")
    }

    def devstatusDir = new File(userHome, ".asciidoc/filters/devstatus").absoluteFile
    if( !devstatusDir.exists() )
    {
      println "Installing [devstatus] into $devstatusDir"
      snippetDir.mkdirs()
      project.copy {
        from "$project.rootDir/buildSrc/src/bin"
        into devstatusDir
        include 'devstatus.*'
      }
      ant.chmod(dir: devstatusDir, perm: "755", includes: "devstatus.py")
    }

    new File(project.buildDir, "docs/$docName".toString()).mkdirs()
    new File(project.buildDir, "tmp/docs/$docName".toString()).mkdirs()

    copyResources()
    generateXDoc()
    generateChunkedHtml()
    generateSingleHtml()
    generatePdf()
  }

  def void copyResources()
  {
    project.parent.subprojects.each { p ->
      p.copy {
        from 'src/docs/resources'
        into "build/docs/$docName/"
        include '**'
      }
    }
  }

  def void generateXDoc()
  {
    project.exec {
      executable = 'asciidoc'
      workingDir = ".."
      def commonResourcesDir = 'manual/src/resources'
      def asciidocConfigFile = 'manual/src/conf/asciidoc.conf'
      def docbookConfigFile = 'manual/src/conf/docbook45.conf'
      def linkimagesConfigFile = 'manual/src/conf/linkedimages.conf'
      def xdocOutputFile = "manual/build/tmp/docs/$docName/xdoc-temp.xml".toString()
      def asciiDocFile = "manual/src/docs/$docName/index.txt".toString()
      args = [
              '--attribute', 'revnumber=' + project.version,
              '--attribute', 'level1=' + (docType.equals('article') ? 1 : 0),
              '--attribute', 'level2=' + (docType.equals('article') ? 2 : 1),
              '--attribute', 'level3=' + (docType.equals('article') ? 3 : 2),
              '--attribute', 'level4=' + (docType.equals('article') ? 4 : 3),
              '--attribute', 'importdir=' + commonResourcesDir,
              '--backend', 'docbook',
              '--attribute', 'docinfo1',
              '--doctype', docType,
              '--conf-file=' + asciidocConfigFile,
              '--conf-file=' + docbookConfigFile,
              '--conf-file=' + linkimagesConfigFile,
              '--out-file', xdocOutputFile,
              asciiDocFile
      ]
    }
  }

  def void generateChunkedHtml()
  {
    project.copy {
      from 'src/resources'
      into "build/docs/$docName/"
      include '**'
    }
    project.copy {
      from "src/docs/$docName/resources"
      into "build/docs/$docName/"
      include '**'
    }

    project.exec {
      String xsltFile = "src/docs/$docName/xsl/chunked.xsl"
      executable = 'xsltproc'
      args = [
              '--nonet',
              '--noout',
              '--output', "build/docs/$docName/",
              xsltFile,
              "build/tmp/docs/$docName/xdoc-temp.xml"
      ]
    }
  }

  def void generateSingleHtml()
  {
    project.exec {
      // XML_CATALOG_FILES=
      String xsltFile = 'src/xsl/xhtml.xsl'
      executable = 'xsltproc'
      args = [
              '--nonet',
              '--noout',
              '--output', "build/docs/$docName/$docName" + ".html",
              xsltFile,
              "build/tmp/docs/$docName/xdoc-temp.xml"
      ]
    }
  }

  def void generatePdf()
  {
// $ xsltproc --nonet ../docbook-xsl/fo.xsl article.xml > article.fo
    // $ fop article.fo article.pdf

    //    project.exec {
    //      String xsltFile = 'src/xsl/fo.xsl'
    //      executable = 'xsltproc'
    //      args = [
    //              '--nonet',
    //              '--output', "build/tmp/docs/$docName/$docName"+".fo",
    //              xsltFile,
    //              "build/tmp/docs/$docName/xdoc-temp.xml"
    //      ]
    //    }
    //    project.exec {
    //      executable = 'fop'
    //      args = [
    //              "build/tmp/docs/$docName/$docName"+".fo",
    //              "build/docs/$docName/$docName" + ".pdf"
    //      ]
    //    }
  }

  String getDocName( )
  {
    return docName
  }

  void setDocName( String docName )
  {
    this.docName = docName
  }

  String getDocType( )
  {
    return docType
  }

  void setDocType( String docType )
  {
    this.docType = docType
  }
}
