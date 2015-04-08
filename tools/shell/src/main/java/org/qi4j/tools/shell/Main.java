package org.qi4j.tools.shell;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import org.qi4j.api.activation.ActivationException;
import org.qi4j.api.activation.PassivationException;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.structure.Module;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.SingletonAssembler;
import org.qi4j.entitystore.file.assembly.FileEntityStoreAssembler;
import org.qi4j.entitystore.memory.MemoryEntityStoreService;
import org.qi4j.functional.Specification;
import org.qi4j.tools.shell.create.project.CreateProject;
import org.qi4j.tools.shell.generate.Generate;
import org.qi4j.tools.shell.model.Model;
import org.qi4j.tools.shell.help.HelpCommand;
import org.qi4j.tools.shell.model.Layer;
import org.qi4j.tools.shell.model.Project;
import org.qi4j.valueserialization.jackson.JacksonValueSerializationAssembler;

import static org.qi4j.functional.Iterables.filter;
import static org.qi4j.functional.Iterables.first;

public class Main
{
    public static void main( String[] args )
        throws Exception
    {
        new Main().run( args );
    }

    private void run( String[] args )
        throws ActivationException, AssemblyException, IOException
    {
        if( !contains( args, "-q" ) )
        {
            System.out.println( "Qi4j - Classes are Dead. Long Live Interfaces!" );
            System.out.println( "----------------------------------------------\n" );
        }
        String commandText;
        if( args.length == 0 )
        {
            commandText = "help";
        }
        else
        {
            commandText = args[ 0 ];
        }
        final SingletonAssembler assembler = new SingletonAssembler()
        {
            @Override
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                module.services( HelpCommand.class ).identifiedBy( "help" ).instantiateOnStartup();
                module.services( CreateProject.class ).identifiedBy( "create-project" ).instantiateOnStartup();
                module.services( Model.class ).instantiateOnStartup();
                module.services( Generate.class ).instantiateOnStartup();

                module.entities( Project.class, Layer.class, org.qi4j.tools.shell.model.Module.class );
                new JacksonValueSerializationAssembler().assemble( module );

                ModuleAssembly configModule = module.layer().module( "config" );
                new FileEntityStoreAssembler().withConfig( configModule, Visibility.layer ).assemble( module );
                new JacksonValueSerializationAssembler().assemble( configModule );
                configModule.services( MemoryEntityStoreService.class );
            }
        };
        Runtime.getRuntime().addShutdownHook( new Thread( new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    assembler.application().passivate();
                }
                catch( PassivationException e )
                {
                    e.printStackTrace();
                }
            }
        } ) );
        executeCommand( commandText, args, assembler.module() );
        output().flush();
    }

    private void executeCommand( final String command, String[] args, Module module )
        throws IOException
    {
        ServiceReference<Command> ref = first( filter( new Specification<ServiceReference<Command>>()
        {
            @Override
            public boolean satisfiedBy( ServiceReference<Command> item )
            {
                return item.get().name().equals( command );
            }
        }, module.findServices( Command.class ) ) );
        ref.get().execute( args, input(), output() );
    }

    private boolean contains( String[] args, String s )
    {
        for( String arg : args )
        {
            if( s.equals( arg ) )
            {
                return true;
            }
        }
        return false;
    }

    private PrintWriter output()
    {
        return new PrintWriter( System.out );
    }

    private BufferedReader input()
    {
        return new BufferedReader( new InputStreamReader( System.in ) );
    }
}
