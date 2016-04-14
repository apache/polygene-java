package org.apache.zest.tools.shell.create.project.restapp;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

public class InfrastructureLayerWriter
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
                "import java.util.function.Function;\n" +
                "import org.apache.zest.api.structure.Application;\n" +
                "import org.apache.zest.api.structure.Module;\n" +
                "import org.apache.zest.bootstrap.AssemblyException;\n" +
                "import org.apache.zest.bootstrap.LayerAssembly;\n" +
                "import org.apache.zest.bootstrap.ModuleAssembly;\n" +
                "import org.apache.zest.bootstrap.layered.LayerAssembler;\n" +
                "import org.apache.zest.bootstrap.layered.LayeredLayerAssembler;\n" +
                "\n" +
                "public class InfrastructureLayer extends LayeredLayerAssembler\n" +
                "    implements LayerAssembler\n" +
                "{\n" +
                "    public static final String NAME = \"Infrastructure Layer\";\n" +
                "    private final ModuleAssembly configModule;\n" +
                "    private final Function<Application, Module> typeFinder;\n" +
                "\n" +
                "    public InfrastructureLayer( ModuleAssembly configModule, Function<Application, Module> typeFinder )\n" +
                "    {\n" +
                "        this.configModule = configModule;\n" +
                "        this.typeFinder = typeFinder;\n" +
                "    }\n" +
                "\n" +
                "    @Override\n" +
                "    public LayerAssembly assemble( LayerAssembly layer )\n" +
                "        throws AssemblyException\n" +
                "    {\n" +
                "        createModule( layer, FileConfigurationModule.class );\n" +
                "\n" +
                "        new StorageModule( configModule ).assemble( layer, layer.module( StorageModule.NAME ) );\n" +
                "        new IndexingModule( configModule ).assemble( layer, layer.module( IndexingModule.NAME ) );\n" +
                "        new SerializationModule( typeFinder ).assemble( layer, layer.module( SerializationModule.NAME ) );\n" +
                "\n" +
                "        return layer;\n" +
                "    }\n" +
                "}\n"
            );
        }
    }

    private PrintWriter createPrinter( Map<String, String> properties )
        throws IOException
    {
        String packagename = properties.get( "root.package" ).replaceAll( "\\.", "/" ) + "/bootstrap/infrastructure/";
        String classname = "InfrastructureLayer";
        File projectDir = new File( properties.get( "project.dir" ) );
        return new PrintWriter( new FileWriter( new File( projectDir, "bootstrap/src/main/java/" + packagename + classname + ".java" ) ) );
    }
}
