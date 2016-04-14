package org.apache.zest.tools.shell.create.project.restapp;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

public class FileConfigurationModuleWriter
{

    public void writeClass( Map<String, String> properties )
        throws IOException
    {
        String rootPackage = properties.get( "root.package" );
        String projectName = properties.get( "project.name" );
        try (PrintWriter pw = createPrinter( properties ))
        {
            pw.print( "package " );
            pw.print( properties.get( "root.package" ) );
            pw.println( ".bootstrap.infrastructure;" );
            pw.println();
            pw.println(
                "import org.apache.zest.api.common.Visibility;\n" +
                "import org.apache.zest.bootstrap.AssemblyException;\n" +
                "import org.apache.zest.bootstrap.LayerAssembly;\n" +
                "import org.apache.zest.bootstrap.ModuleAssembly;\n" +
                "import org.apache.zest.bootstrap.layered.ModuleAssembler;\n" +
                "import org.apache.zest.library.fileconfig.FileConfigurationAssembler;\n" +
                "\n" +
                "public class FileConfigurationModule\n" +
                "    implements ModuleAssembler\n" +
                "{\n" +
                "    public static String NAME;\n" +
                "\n" +
                "    @Override\n" +
                "    public ModuleAssembly assemble( LayerAssembly layer, ModuleAssembly module )\n" +
                "        throws AssemblyException\n" +
                "    {\n" +
                "        new FileConfigurationAssembler().visibleIn( Visibility.layer ).assemble( module );\n" +
                "        return module;\n" +
                "    }\n" +
                "}\n"
            );
        }
    }

    private PrintWriter createPrinter( Map<String, String> properties )
        throws IOException
    {
        String packagename = properties.get( "root.package" ).replaceAll( "\\.", "/" ) + "/bootstrap/infrastructure/";
        String classname = "FileConfigurationModule";
        File projectDir = new File( properties.get( "project.dir" ) );
        return new PrintWriter( new FileWriter( new File( projectDir, "bootstrap/src/main/java/" + packagename + classname + ".java" ) ) );
    }
}
