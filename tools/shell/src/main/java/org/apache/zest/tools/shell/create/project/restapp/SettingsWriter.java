package org.apache.zest.tools.shell.create.project.restapp;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

public class SettingsWriter
{

    public void writeClass( Map<String, String> properties )
        throws IOException
    {
        String rootPackage = properties.get( "root.package" );
        String projectName = properties.get( "project.name" );
        try (PrintWriter pw = createPrinter( properties ))
        {
            pw.println(
                String.format(
                "\n" +
                "include 'app',\n" +
                "        'bootstrap',\n" +
                "        'model',\n" +
                "        'rest'\n" +
                "\n" +
                "rootProject.name = \"%s\"\n" +
                "\n" +
                "validateProject(rootProject, \"\")\n" +
                "\n" +
                "def validateProject(project, parentName)\n" +
                "{\n" +
                "  assert project.projectDir.isDirectory()\n" +
                "  if( new File(\"$project.projectDir/src/main/java\").exists() )\n" +
                "  {\n" +
                "    assert project.buildFile.isFile()\n" +
                "  }\n" +
                "  if( parentName.length() > 0 )\n" +
                "  println \"Project: \" + project.name\n" +
                "  project.children.each { child ->\n" +
                "    validateProject(child, project.name)\n" +
                "  }\n" +
                "}\n" +
                "\n", projectName
                ));
        }
    }

    private PrintWriter createPrinter( Map<String, String> properties )
        throws IOException
    {
        File projectDir = new File( properties.get( "project.dir" ) );
        return new PrintWriter( new FileWriter( new File( projectDir, "settings.gradle" ) ) );
    }
}
