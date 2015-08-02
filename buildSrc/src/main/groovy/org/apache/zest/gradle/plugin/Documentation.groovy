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
package org.apache.zest.gradle.plugin;


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
  @InputDirectory def File getBuildSrcDir() { project.rootProject.file( 'buildSrc/src' ) }

  @InputFiles def getSubProjectsDocsDirs() { project.rootProject.subprojects.collect { p -> p.file( 'src/docs' ) } }
  @InputFiles def getSubProjectsTestDirs() { project.rootProject.subprojects.collect { p -> p.file( 'src/test' ) } }

  @OutputDirectory def File getOutputDir() { project.file( "${project.buildDir}/docs/${docName}/" ) }

  def File getTempAsciidocDir() { project.file( "${project.buildDir}/tmp-asciidoc" ) }
  def File getTempDir() { project.file( "${project.buildDir}/tmp/docs/${docName}") }

  @TaskAction
  def void generate()
  {
    installAsciidocFilters()

    [ outputDir, tempAsciidocDir, tempDir ]*.deleteDir()
    [ outputDir, tempAsciidocDir, tempDir ]*.mkdirs()

    copySubProjectsDocsResources()
    generateAsciidocAccordingToReleaseSpecification()
    generateXDoc()
    generateChunkedHtml()
    // generateSingleHtml()
    // generatePdf()
  }

  def void installAsciidocFilters()
  {
    def digester = java.security.MessageDigest.getInstance( 'SHA' )
    def filtersDir = project.rootProject.file( 'buildSrc/src/asciidoc/filters' )
    def userHome = new File( System.getProperty( 'user.home' ) )
    def dotAsciidocFiltersDir = new File( userHome, '.asciidoc/filters' )
    def installSnippets = false
    filtersDir.eachFileRecurse( groovy.io.FileType.FILES ) { originalFile ->
      def targetFile = new File( dotAsciidocFiltersDir, (originalFile.toURI() as String) - (filtersDir.toURI() as String) )
      if( !targetFile.exists() )
      {
        installSnippets = true
      }
      else
      {
        def originalDigest = digester.digest( originalFile.bytes )
        def targetDigest = digester.digest( targetFile.bytes )
        if( originalDigest != targetDigest )
        {
          installSnippets = true
        }
      }
    }
    if( installSnippets )
    {
      dotAsciidocFiltersDir.mkdirs()
      project.rootProject.copy {
        from filtersDir
        into dotAsciidocFiltersDir
      }
      dotAsciidocFiltersDir.eachFileRecurse( groovy.io.FileType.FILES ) { file ->
        if( file.name.endsWith( '.py' ) ) {
          ant.chmod( file: file.absolutePath, perm: '755' )
        }
      }
      println "Zest Asciidoc Filters Installed!"
    }
  }

  def void copySubProjectsDocsResources()
  {
    project.rootProject.subprojects.each { p ->
      p.copy {
        from p.file( 'src/docs/resources' )
        into outputDir
        include '**'
      }
    }
  }

  def void generateAsciidocAccordingToReleaseSpecification()
  {
    project.copy {
      from docsDir
      into tempAsciidocDir
      include '**'
    }
    if( project.version != '0' && !project.version.contains( 'SNAPSHOT' ) ) {
      def licenseFile = new File( tempAsciidocDir, 'userguide/libraries.txt' )
      def extensionsFile = new File( tempAsciidocDir, 'userguide/extensions.txt' )
      def toolsFile = new File( tempAsciidocDir, 'userguide/tools.txt' )
      [ licenseFile, extensionsFile, toolsFile ].each { asciidocFile ->
        def filteredFileContent = ''
        asciidocFile.readLines().each { line ->
          if( line.startsWith( 'include::' ) ) {
            def approved = false
            project.rootProject.releaseApprovedProjects.collect{it.projectDir}.each { approvedProjectDir ->
              if( line.contains( "${approvedProjectDir.parentFile.name}/${approvedProjectDir.name}" ) ) {
                approved = true
              }
            }
            if( approved ) {
              filteredFileContent += "$line\n"
            }
          } else {
            filteredFileContent += "$line\n"
          }
        }
        asciidocFile.text = filteredFileContent
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
      def asciidocIndexPath = relativePath( project.rootDir, new File( tempAsciidocDir, "$docName/index.txt" ) )
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
