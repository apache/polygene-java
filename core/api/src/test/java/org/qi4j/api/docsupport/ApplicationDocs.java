package org.qi4j.api.docsupport;

import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.ApplicationDescriptor;
import org.qi4j.bootstrap.ApplicationAssembler;
import org.qi4j.bootstrap.ApplicationAssembly;
import org.qi4j.bootstrap.ApplicationAssemblyFactory;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.Energy4Java;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.SingletonAssembler;

public class ApplicationDocs
{
    public static void someMethod( String[] args )
        throws Exception
    {
        {
// START SNIPPET: application1
            SingletonAssembler qi4j = new SingletonAssembler()
            {
                public void assemble( ModuleAssembly assembly )
                    throws AssemblyException
                {
                    assembly.values( MyStuffValueComposite.class );
                }
            };
// END SNIPPET: application1
        }
        {
            Assembler customerListEditAssembler = new DummyAssembler();
            Assembler customerEditAssembler = new DummyAssembler();
            Assembler customerSearchAssembler = new DummyAssembler();
            Assembler accountsListEditAssembler = new DummyAssembler();
            Assembler accountsEditAssembler = new DummyAssembler();
            Assembler accountsSearchAssembler = new DummyAssembler();
            Assembler customerDomainAssembler = new DummyAssembler();
            Assembler accountsDomainAssembler = new DummyAssembler();
// START SNIPPET: application2
            final Assembler[][][] assemblers =
                {
                    { // web layer
                      { // Customer Module
                        customerListEditAssembler,
                        customerEditAssembler,
                        customerSearchAssembler
                      },
                      { // Accounts Module
                        accountsListEditAssembler,
                        accountsEditAssembler,
                        accountsSearchAssembler
                      }
                    },
                    { // domain layer
                      { // Customer Module
                        customerDomainAssembler,
                      },
                      { // Accounts Module
                        accountsDomainAssembler,
                      }
                    }
                };
            Energy4Java qi4j = new Energy4Java();
            Application app = qi4j.newApplication( new ApplicationAssembler()
            {

                @Override
                public ApplicationAssembly assemble( ApplicationAssemblyFactory applicationFactory )
                    throws AssemblyException
                {
                    return applicationFactory.newApplicationAssembly( assemblers );
                }
            } );
            app.activate();
// END SNIPPET: application2
        }
    }

    public interface MyStuffValueComposite
    {
    }

    private static class DummyAssembler implements Assembler
    {
        @Override
        public void assemble( ModuleAssembly module )
            throws AssemblyException
        {

        }
    }

    // START SNIPPET: application3
    private static Energy4Java qi4j;

    public static void main( String[] args )
        throws Exception
    {
        qi4j = new Energy4Java();
        ApplicationDescriptor model = qi4j.newApplicationModel( new ApplicationAssembler()
        {
            @Override
            public ApplicationAssembly assemble( ApplicationAssemblyFactory applicationFactory )
                throws AssemblyException
            {
                return createAssembly( applicationFactory );
            }
        } );
        Application application = model.newInstance( qi4j.spi() );
    }

    private static ApplicationAssembly createAssembly( ApplicationAssemblyFactory factory )
        throws AssemblyException
    {
        String applicationName = "Example Application";
        ApplicationAssembly app = factory.newApplicationAssembly();
        app.setName( applicationName );
        LayerAssembly webLayer = createWebLayer( app );
        LayerAssembly domainLayer = createDomainLayer( app );
        LayerAssembly infraLayer = createInfrastructureLayer( app );
        webLayer.uses( domainLayer );
        webLayer.uses( infraLayer );  // Accesses the WebService
        domainLayer.uses( infraLayer ); // For persistence
        return app;
    }

    private static LayerAssembly createWebLayer(
        ApplicationAssembly application
    )
    {
        LayerAssembly layer = application.layer( "Web Layer" );
        createCustomerWebModule( layer );
        return layer;
    }

    private static LayerAssembly createDomainLayer(
        ApplicationAssembly application
    )
    {
        LayerAssembly layer = application.layer( "Domain Layer" );
        createCustomerDomainModule( layer );
        // :
        // :
        return layer;
    }

    private static LayerAssembly createInfrastructureLayer(
        ApplicationAssembly application
    )
        throws AssemblyException
    {
        LayerAssembly layer = application.layer( "Infrastructure Layer" );
        createWebServiceModule( layer );
        createPersistenceModule( layer );
        return layer;
    }

    private static void createCustomerWebModule( LayerAssembly layer )
    {
        ModuleAssembly assembly = layer.module( "Customer Web Module" );
        assembly.transients( CustomerViewComposite.class );
        assembly.transients( CustomerEditComposite.class );
        assembly.transients( CustomerListViewComposite.class );
        assembly.transients( CustomerSearchComposite.class );
    }

    private static void createCustomerDomainModule( LayerAssembly layer )
    {
        ModuleAssembly assembly = layer.module( "Customer Domain Module" );
        assembly.entities( CustomerEntity.class );
        assembly.entities( CountryEntity.class );
        assembly.transients( AddressComposite.class );
    }

    private static void createWebServiceModule( LayerAssembly layer )
        throws AssemblyException
    {
        ModuleAssembly assembly = layer.module( "Web Service Module" );
        // Someone has created an assembler for a Jetty Web Service.
        JettyAssembler jetty = new JettyAssembler( 8080 );
        jetty.assemble( assembly );
    }

    private static void createPersistenceModule( LayerAssembly layer )
        throws AssemblyException
    {
        ModuleAssembly assembly = layer.module( "Persistence Module" );
        // Someone has created an assembler for the Neo EntityStore
        NeoAssembler neo = new NeoAssembler( "./neostore" );
        neo.assemble( assembly );
    }
// START SNIPPET: application3

    public static class CustomerViewComposite
    {

    }
    public static class CustomerEditComposite
    {

    }
    public static class CustomerListViewComposite
    {

    }
    public static class CustomerSearchComposite
    {

    }


    public static class CustomerEntity
    {

    }
    public static class CountryEntity
    {

    }
    public static class AddressComposite
    {

    }

    public static class JettyAssembler
        implements Assembler
    {

        public JettyAssembler( int port )
        {
        }

        @Override
        public void assemble( ModuleAssembly module )
            throws AssemblyException
        {
        }
    }
    public static class NeoAssembler
        implements Assembler
    {

        public NeoAssembler( String s )
        {
        }

        @Override
        public void assemble( ModuleAssembly module )
            throws AssemblyException
        {
        }
    }
}

