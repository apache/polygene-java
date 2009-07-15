package org.qi4j.test;

import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.ApplicationAssembler;
import org.qi4j.bootstrap.ApplicationAssembly;
import org.qi4j.bootstrap.ApplicationAssemblyFactory;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.Energy4Java;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.spi.structure.ApplicationSPI;

public class Main
{
    private static Energy4Java qi4j;
    private static ApplicationSPI application;

    public static void main( String[] args )
        throws Exception
    {
        // Bootstrap Qi4j Runtime
        // Create a Qi4j Runtime
        qi4j = new Energy4Java();

        // Instantiate the Application Model.
        application = qi4j.newApplication( new ApplicationAssembler()
        {
            public ApplicationAssembly assemble( ApplicationAssemblyFactory factory )
                throws AssemblyException
            {
                ApplicationAssembly assembly = factory.newApplicationAssembly();
                LayerAssembly runtime = createRuntimeLayer( assembly );
                LayerAssembly designer = createDesignerLayer( assembly );
                LayerAssembly domain = createDomainLayer( assembly );
                LayerAssembly messaging = createMessagingLayer( assembly );
                LayerAssembly persistence = createPersistenceLayer( assembly );

                // declare structure between layers
                domain.uses( messaging );
                domain.uses( persistence );
                designer.uses( persistence );
                designer.uses( domain );
                runtime.uses( domain );

                return assembly;
            }
        } );

        // We need to handle shutdown.
        installShutdownHook();

        // Activate the Application Runtime.
        application.activate();
    }

    private static LayerAssembly createDomainLayer( ApplicationAssembly app )
    {
        LayerAssembly layer = app.newLayerAssembly( "domain-layer" );

        createAccountModule( layer );
        createInventoryModule( layer );
        createReceivablesModule( layer );
        createPayablesModule( layer );

        return layer;
    }

    private static void createAccountModule( LayerAssembly layer )
    {
        ModuleAssembly module = layer.newModuleAssembly( "account-module" );

        module.addEntities( AccountEntity.class,
                            EntryEntity.class
        );

        module.addServices(
            AccountRepositoryService.class,
            AccountFactoryService.class,
            EntryFactoryService.class,
            EntryRepositoryService.class
        ).visibleIn( Visibility.layer );
    }

    private static void installShutdownHook()
    {
        Runtime.getRuntime().addShutdownHook( new Thread( new Runnable()
        {
            public void run()
            {
                if( application != null )
                {
                    try
                    {
                        application.passivate();
                    }
                    catch( Exception e )
                    {
                        e.printStackTrace();
                    }
                }
            }
        } ) );
    }
}