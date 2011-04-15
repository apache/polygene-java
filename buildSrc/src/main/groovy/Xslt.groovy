/**
 *  Gradle plug-in for running a set of one or more
 *  files through an XSLT transform.  A styleSheet
 *  file must be specified.  The source file(s) are
 *  configured just like any other source task:
 *     source <file>
 *       ...or...
 *     source <directory>
 *       ...and then optionally...
 *     include '*.xml'
 *     exclude, etc.
 *
 *  One of destDir or destFile must be specified, though if
 *  there are multiple source files then destFile will just
 *  keep getting rewritten.
 *
 *  The extension is stripped from the source files and the
 *  specified extension is appended (whether it is set or not)
 *  it defaults to no extension.
 *
 *  Example task formatting a check style report:
 *
 *  task checkstyleReport(type: Xslt, dependsOn: check) {*      source project.checkstyleResultsDir
 *      include '*.xml'
 *
 *      destDir = project.checkstyleResultsDir
 *      extension = 'html'
 *
 *      stylesheetFile = file( 'config/checkstyle/checkstyle-noframes.xsl' )
 *}*
 *  The above definition requires that the specified XSL file be
 *  copied in with the other checkstyle configuration files.  (The
 *  file in the example is part of the checkstyle distribution.)
 *
 */

import org.gradle.api.InvalidUserDataException
import org.gradle.api.tasks.SourceTask
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.file.FileVisitDetails

import javax.xml.transform.TransformerFactory
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource

class Xslt extends SourceTask
{

  @OutputDirectory @Optional
  File destDir

  @Optional
  String extension

  @InputFile
  File stylesheetFile

  @TaskAction
  def transform()
  {
    def factory = TransformerFactory.newInstance()
    def transformer = factory.newTransformer(new StreamSource(stylesheetFile));

    source.visit { FileVisitDetails fvd ->
      if( fvd.isDirectory() )
      {
        return
      }
      // Remove the extension from the file name
      name = fvd.file.name.replaceAll('[.][^\\.]*$', '')
      if( extension == null )
      {
        extension = 'html'
      }
      name += '.' + extension
      def destFile = new File(destDir, name)
      transformer.transform(new StreamSource(fvd.file), new StreamResult(destFile))

    }
  }
}
