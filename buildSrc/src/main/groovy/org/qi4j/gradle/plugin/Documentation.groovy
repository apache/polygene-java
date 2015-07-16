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
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory

// TODO: try to use dependencies for FOP and execute within the same JVM.
// TODO: move the bulk of resources into this plugin, instead of sitting in the project.
class Documentation extends DefaultTask
{
  @Input def String docName
  @Input def String docType
  void setDocName( String docName ) { this.docName = docName }
  void setDocType( String docType ) { this.docType = docType }

  @InputDirectory def File getCommonResourcesDir() { project.file( 'src/resources' ) }
  @InputDirectory def File getConfigDir() { project.file( 'src/conf' ) }
  @InputDirectory def File getDocsDir() { project.file( 'src/docs') }
  @InputDirectory def File getSrcMainDir() { project.file( 'src/main') }
  @InputDirectory def File getXslDir() { project.file( 'src/xsl') }

  @InputFiles def getSubProjectsDocsDirs() { project.parent.subprojects.collect { p -> p.file( 'src/docs' ) } }
  @InputFiles def getSubProjectsTestDirs() { project.parent.subprojects.collect { p -> p.file( 'src/test' ) } }

  @OutputDirectory def File getOutputDir() { project.file( "${project.buildDir}/docs/${docName}/" ) }

  def File getTempDir() { project.file( "${project.buildDir}/tmp/docs/${docName}") }

  @TaskAction
  def void generate()
  {
    installAsciidocFilters()

    [ outputDir, tempDir ]*.deleteDir()
    [ outputDir, tempDir ]*.mkdirs()

    copySubProjectsDocsResources()
    generateXDoc()
    generateChunkedHtml()
    // generateSingleHtml()
    // generatePdf()
  }

  def void installAsciidocFilters()
  {
    def userHome = new File( System.getProperty( 'user.home' ) )
    def snippetDir = new File( userHome, '.asciidoc/filters/snippet' ).absoluteFile
    if( !snippetDir.exists() )
    {
      println "Installing [snippet] into $snippetDir"
      snippetDir.mkdirs()
      project.copy {
        from "${project.rootDir}/buildSrc/src/bin"
        into snippetDir
        include 'snippet.*'
      }
      ant.chmod( dir: snippetDir, perm: '755', includes: 'snippet.py' )
    }

    def devstatusDir = new File( userHome, '.asciidoc/filters/devstatus' ).absoluteFile
    if( !devstatusDir.exists() )
    {
      println "Installing [devstatus] into $devstatusDir"
      snippetDir.mkdirs()
      project.copy {
        from "${project.rootDir}/buildSrc/src/bin"
        into devstatusDir
        include 'devstatus.*'
      }
      ant.chmod( dir: devstatusDir, perm: '755', includes: 'devstatus.py' )
    }
  }

  def void copySubProjectsDocsResources()
  {
    project.parent.subprojects.each { p ->
      p.copy {
        from p.file( 'src/docs/resources' )
        into outputDir
        include '**'
      }
    }
  }

  def void generateXDoc()
  {
    project.exec {
      executable = 'asciidoc'
      workingDir = '..'
      def commonResourcesPath = relativePath( project.rootDir, commonResourcesDir )
      def asciidocConfigPath = relativePath( project.rootDir, new File( configDir, 'asciidoc.conf' ) )
      def docbookConfigPath = relativePath( project.rootDir, new File( configDir, 'docbook45.conf' ) )
      def linkimagesConfigPath = relativePath( project.rootDir, new File( configDir, 'linkedimages.conf' ) )
      def xdocOutputPath =  relativePath( project.rootDir, new File( tempDir, 'xdoc-temp.xml' ) )
      def asciidocIndexPath = relativePath( project.rootDir, new File( docsDir, "$docName/index.txt" ) )
      args = [
              '--attribute', 'revnumber=' + project.version,
              '--attribute', 'level1=' + (docType.equals('article') ? 1 : 0),
              '--attribute', 'level2=' + (docType.equals('article') ? 2 : 1),
              '--attribute', 'level3=' + (docType.equals('article') ? 3 : 2),
              '--attribute', 'level4=' + (docType.equals('article') ? 4 : 3),
              '--attribute', 'importdir=' + commonResourcesPath,
              '--backend', 'docbook',
              '--attribute', 'docinfo1',
              '--doctype', docType,
              '--conf-file=' + asciidocConfigPath,
              '--conf-file=' + docbookConfigPath,
              '--conf-file=' + linkimagesConfigPath,
              '--out-file', xdocOutputPath,
              asciidocIndexPath
      ]
    }
  }

  def void generateChunkedHtml()
  {
    project.copy {
      from commonResourcesDir
      into outputDir
      include '**'
    }
    project.copy {
      from "$docsDir/$docName/resources"
      into outputDir
      include '**'
    }

    project.exec {
      def xsltFile = "$docsDir/$docName/xsl/chunked.xsl"
      def outputPath = relativePath( project.projectDir, outputDir ) + '/'
      executable = 'xsltproc'
      args = [
              '--nonet',
              '--noout',
              '--output', outputPath,
              xsltFile,
              "$tempDir/xdoc-temp.xml"
      ]
    }
  }

  def void generateSingleHtml()
  {
    project.exec {
      // XML_CATALOG_FILES=
      String xsltFile = "$xslDir/xhtml.xsl"
      executable = 'xsltproc'
      args = [
              '--nonet',
              '--noout',
              '--output', "$outputDir/${docName}.html",
              xsltFile,
              "$tempDir/xdoc-temp.xml"
      ]
    }
  }

  def void generatePdf()
  {
    // $ xsltproc --nonet ../docbook-xsl/fo.xsl article.xml > article.fo
    // $ fop article.fo article.pdf
    project.exec {
      String xsltFile = "$xslDir/fo.xsl"
      executable = 'xsltproc'
      args = [
        '--nonet',
        '--output', "$tempDir/${docName}.fo",
        xsltFile,
        "$tempDir/xdoc-temp.xml"
      ]
    }
    project.exec {
      executable = 'fop'
      args = [
        "$tempDir/${docName}.fo",
        "$outputDir/${docName}.pdf"
      ]
    }
  }

  def String relativePath( File root, File target )
  {
    new File( root.toURI().relativize( target.toURI() ).toString() ).path
  }
}
