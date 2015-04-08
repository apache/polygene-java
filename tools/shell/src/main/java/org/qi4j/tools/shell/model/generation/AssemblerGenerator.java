package org.qi4j.tools.shell.model.generation;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.qi4j.api.injection.scope.This;
import org.qi4j.tools.shell.FileUtils;
import org.qi4j.tools.shell.StringUtils;
import org.qi4j.tools.shell.model.AssemblerModel;
import org.qi4j.tools.shell.model.Layer;
import org.qi4j.tools.shell.model.Project;

public abstract class AssemblerGenerator
    implements AssemblerModel
{
    @This
    private AssemblerModel self;

    @Override
    public void generate( Project project, File projectDir )
        throws IOException
    {
        File bootDir = self.mainJavaRootPackageDirectory( projectDir );
        File topAssembler = new File( bootDir, project.applicationName() + "Assembler.java" );

        Map<String, String> variables = new HashMap<>();
        variables.put( "APPLICATION_NAME", project.applicationName() );
        variables.put( "IMPORTS", createImports() );
        variables.put( "LAYER_CREATION", createLayerCreation( project ) );
        variables.put( "LAYER_CREATE_METHODS", createLayerMethods( project ) );
        variables.put( "ROOT_PACKAGE", project.rootPackageName() );
        variables.put( "MAIN_JAVA_ROOT_PACKAGE_PATH", bootDir.getAbsolutePath() );
        variables.put( "MAIN_RESOURCES_ROOT_PACKAGE_PATH",
                       self.mainResourcesRootPackageDirectory( projectDir ).getAbsolutePath() );
        variables.put( "TEST_JAVA_ROOT_PACKAGE_PATH",
                       self.testJavaRootPackageDirectory( projectDir ).getAbsolutePath() );
        variables.put( "TEST_RESOURCES_ROOT_PACKAGE_PATH",
                       self.testResourcesRootPackageDirectory( projectDir ).getAbsolutePath() );
        String application = project.template().evaluate( variables );
        FileUtils.writeFile( topAssembler, application );
    }

    private String createLayerCreation( Project project )
    {
        StringBuilder builder = new StringBuilder();
        for( Layer layer : project.layers() )
        {
            builder.append( "        " );
            builder.append( "LayerAssembly " );
            builder.append( StringUtils.camelCase( layer.name(), false ) );
            builder.append( "Assembler = new " );
            builder.append( StringUtils.camelCase( layer.name(), true ) );
            builder.append( "().assemble( assembly );" );
            builder.append( "\n" );
        }
        builder.append( "\n" );

        for( Layer layer : project.layers() )
        {
            for( String use : layer.uses() )
            {
                builder.append( "        " );
                builder.append( StringUtils.camelCase( layer.name(), false ) );
                builder.append( ".uses( " );
                builder.append( StringUtils.camelCase( use, false ) );
                builder.append( ");\n" );
            }
        }
        return builder.toString();
    }

    private String createLayerMethods( Project project )
    {
        StringBuilder builder = new StringBuilder();
        for( Layer layer : project.layers() )
        {
            builder.append( "\n    private LayerAssembly create" );
            builder.append( StringUtils.camelCase( layer.name(), true ) );
            builder.append( "( ApplicationAssembly assembly )\n    {\n" );
            builder.append( "        LayerAssembly layer = assembly.layer( \"" + layer.name() + "\" );\n" );
            builder.append( "        return layer;\n" );
            builder.append( "    }\n" );
        }
        return builder.toString();
    }

    private String createImports()
    {
        return "";
    }
}
