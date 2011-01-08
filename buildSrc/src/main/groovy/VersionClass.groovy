
import org.gradle.api.Project
import org.gradle.api.Plugin
import org.gradle.api.plugins.JavaPlugin
import java.util.Date;

class VersionClass implements Plugin<Project>  {

    VersionClass() {
    }

    def void apply(Project project) {
        project.getPlugins().apply( JavaPlugin.class )
        def genSrc = 'generated-src/version'
        def generatedSrcDir = new File(project.buildDir, genSrc)

        def makeVersionClassTask = project.task('makeVersionClass') << {
            def now = new Date()
            def outFilename = "java/"+project.group.replace('.','/')+"/"+project.name+"/BuildVersion.java"
            def outFile = new File(generatedSrcDir, outFilename)
            outFile.getParentFile().mkdirs()
            def f = new FileWriter(outFile)
            f.write('package  '+project.group+"."+project.name+';\n')
            f.write("""
/**
 * Simple class for storing the version derived from the gradle build.gradle file.
 *
 */
public interface BuildVersion
{
    /** the version of the project from the gradle build.gradle file. */
    String version = \""""+project.version+"""\";

    /** the name of the project from the gradle build.gradle file. */
    String name = \""""+project.name+"""\";

    /** the group of the project from the gradle build.gradle file. */
    String group = \""""+project.group+"""\";

    /** the date this file was generated, usually the last date that the project was modified. */
    String date = \""""+now+"""\";

    String detailedVersion = group+":"+name+":"+version+" "+date;
}\n
""")
            f.close()
        }
        project.sourceSets {
            version {
                java {
                    srcDir project.buildDir.name+'/'+genSrc+'/java'
                }
            }
        }
        makeVersionClassTask.getInputs().files(project.sourceSets.main.getAllSource() )
        makeVersionClassTask.getOutputs().files(generatedSrcDir)
        if (project.getBuildFile() != null && project.getBuildFile().exists()) {
            makeVersionClassTask.getInputs().files(project.getBuildFile())
        }
        project.getTasks().getByName('compileJava').dependsOn('compileVersionJava')
        project.getTasks().getByName('compileVersionJava').dependsOn('makeVersionClass')
        project.getTasks().getByName('jar') {
            from project.sourceSets.version.classes
        }
    }
}


