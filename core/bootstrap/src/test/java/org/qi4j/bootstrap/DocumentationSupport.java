package org.qi4j.bootstrap;

import org.qi4j.api.common.Visibility;
import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.ApplicationDescriptor;
import org.qi4j.api.structure.Module;
import org.qi4j.functional.Iterables;
import org.qi4j.functional.Specification;

public class DocumentationSupport
{

    static Specification<ObjectAssembly> hasMyTypeSpecification = new Specification<ObjectAssembly>()
    {

        public boolean satisfiedBy( ObjectAssembly item )
        {
            return Iterables.toList( item.types() ).contains( String.class );
        }

    };

    static class objects
            implements Assembler
    {

        static class MyObject {}

        // START SNIPPET: objects
        @Override
        public void assemble( ModuleAssembly module )
                throws AssemblyException
        {
            module.objects( MyObject.class ).visibleIn( Visibility.layer );
        }
        // END SNIPPET: objects

    }

    static class transients
            implements Assembler
    {

        static interface MyTransient {}

        // START SNIPPET: transients
        @Override
        public void assemble( ModuleAssembly module )
                throws AssemblyException
        {
            module.transients( MyTransient.class ).visibleIn( Visibility.layer );
        }
        // END SNIPPET: transients

    }

    static class values
            implements Assembler
    {

        static interface MyValue {}

        // START SNIPPET: values
        @Override
        public void assemble( ModuleAssembly module )
                throws AssemblyException
        {
            module.values( MyValue.class ).visibleIn( Visibility.layer );
        }
        // END SNIPPET: values

    }

    static class entities
            implements Assembler
    {

        static interface MyEntity {}

        // START SNIPPET: entities
        @Override
        public void assemble( ModuleAssembly module )
                throws AssemblyException
        {
            module.entities( MyEntity.class ).visibleIn( Visibility.layer );
        }
        // END SNIPPET: entities

    }

    static class services
            implements Assembler
    {

        static interface MyService {}

        // START SNIPPET: services
        @Override
        public void assemble( ModuleAssembly module )
                throws AssemblyException
        {
            module.services( MyService.class ).visibleIn( Visibility.layer );
        }
        // END SNIPPET: services

    }

    static class singleton
    {

        interface MyService { }
        interface Stuff { }

        void singleton()
        {
            // START SNIPPET: singleton
            SingletonAssembler assembler = new SingletonAssembler()
            {

                @Override
                public void assemble( ModuleAssembly module )
                        throws AssemblyException
                {
                    module.services( MyService.class ).identifiedBy( "Foo" );
                    module.services( MyService.class ).identifiedBy( "Bar" );
                    module.objects( Stuff.class );
                }

            };
            Module module = assembler.module();
            Stuff stuff = module.newObject( Stuff.class );
            // END SNIPPET: singleton
        }

    }

    static class pancake
    {

        static class LoginAssembler implements Assembler { public void assemble( ModuleAssembly module ) throws AssemblyException { } }
        static class MenuAssembler implements Assembler { public void assemble( ModuleAssembly module ) throws AssemblyException { } }
        static class PerspectivesAssembler implements Assembler { public void assemble( ModuleAssembly module ) throws AssemblyException { } }
        static class ViewsAssembler implements Assembler { public void assemble( ModuleAssembly module ) throws AssemblyException { } }
        static class ReportingAssembler implements Assembler { public void assemble( ModuleAssembly module ) throws AssemblyException { } }
        static class PdfAssembler implements Assembler { public void assemble( ModuleAssembly module ) throws AssemblyException { } }
        static class BookkeepingAssembler implements Assembler { public void assemble( ModuleAssembly module ) throws AssemblyException { } }
        static class CashFlowAssembler implements Assembler { public void assemble( ModuleAssembly module ) throws AssemblyException { } }
        static class BalanceSheetAssembler implements Assembler { public void assemble( ModuleAssembly module ) throws AssemblyException { } }
        static class PricingAssembler implements Assembler { public void assemble( ModuleAssembly module ) throws AssemblyException { } }
        static class ProductAssembler implements Assembler { public void assemble( ModuleAssembly module ) throws AssemblyException { } }

        private static Energy4Java qi4j;

        // START SNIPPET: pancake
        public static void main( String[] args )
                throws Exception
        {
            qi4j = new Energy4Java();
            Assembler[][][] assemblers = new Assembler[][][]{
                { // View Layer
                    { // Login Module
                        new LoginAssembler()
                    // :
                    },
                    { // Main Workbench Module
                        new MenuAssembler(),
                        new PerspectivesAssembler(),
                        new ViewsAssembler()
                    // :
                    },
                    { // Printing Module
                        new ReportingAssembler(),
                        new PdfAssembler()
                    // :
                    }
                },
                { // Application Layer
                    { // Accounting Module
                        new BookkeepingAssembler(),
                        new CashFlowAssembler(),
                        new BalanceSheetAssembler()
                    // :
                    },
                    { // Inventory Module
                        new PricingAssembler(),
                        new ProductAssembler()
                    // :
                    }
                },
                { // Domain Layer
                // :
                },
                { // Infrastructure Layer
                // :
                }
            };
            ApplicationDescriptor model = newApplication( assemblers );
            Application runtime = model.newInstance( qi4j.spi() );
            runtime.activate();
        }

        private static ApplicationDescriptor newApplication( final Assembler[][][] assemblers )
                throws AssemblyException
        {
            return qi4j.newApplicationModel( new ApplicationAssembler()
            {

                @Override
                public ApplicationAssembly assemble( ApplicationAssemblyFactory appFactory )
                        throws AssemblyException
                {
                    return appFactory.newApplicationAssembly( assemblers );
                }

            } );
        }
        // END SNIPPET: pancake

    }

    static class full
    {

        static class CustomerViewComposite{}
        static class CustomerEditComposite{}
        static class CustomerListViewComposite{}
        static class CustomerSearchComposite{}
        static class CustomerEntity{}
        static class CountryEntity{}
        static class AddressValue{}
        static class LdapAuthenticationAssembler implements Assembler{ public void assemble( ModuleAssembly module ) throws AssemblyException { } }
        static class ThrinkAuthorizationAssembler implements Assembler{ public void assemble( ModuleAssembly module ) throws AssemblyException { } }
        static class UserTrackingAuditAssembler implements Assembler{ public void assemble( ModuleAssembly module ) throws AssemblyException { } }
        static class NeoAssembler implements Assembler{ NeoAssembler( String path ) {} public void assemble( ModuleAssembly module ) throws AssemblyException { } }

        // START SNIPPET: full
        private static Energy4Java qi4j;

        private static Application application;

        public static void main( String[] args )
                throws Exception
        {
            // Create a Qi4j Runtime
            qi4j = new Energy4Java();
            application = qi4j.newApplication( new ApplicationAssembler()
            {

                @Override
                public ApplicationAssembly assemble( ApplicationAssemblyFactory appFactory )
                        throws AssemblyException
                {
                    ApplicationAssembly assembly = appFactory.newApplicationAssembly();
                    buildAssembly( assembly );
                    return assembly;
                }

            } );
            // activate the application
            application.activate();
        }

        static void buildAssembly( ApplicationAssembly app ) throws AssemblyException
        {
            LayerAssembly webLayer = createWebLayer( app );
            LayerAssembly domainLayer = createDomainLayer( app );
            LayerAssembly persistenceLayer = createInfrastructureLayer( app );
            LayerAssembly authLayer = createAuth2Layer( app );
            LayerAssembly messagingLayer = createMessagingLayer( app );

            webLayer.uses( domainLayer );
            domainLayer.uses( authLayer );
            domainLayer.uses( persistenceLayer );
            domainLayer.uses( messagingLayer );
        }

        static LayerAssembly createWebLayer( ApplicationAssembly app ) throws AssemblyException
        {
            LayerAssembly layer = app.layer( "web-layer" );
            createCustomerWebModule( layer );
            return layer;
        }

        static LayerAssembly createDomainLayer( ApplicationAssembly app ) throws AssemblyException
        {
            LayerAssembly layer = app.layer( "domain-layer" );
            createCustomerDomainModule( layer );
            // :
            // :
            return layer;
        }

        static LayerAssembly createInfrastructureLayer( ApplicationAssembly app ) throws AssemblyException
        {
            LayerAssembly layer = app.layer( "infrastructure-layer" );
            createPersistenceModule( layer );
            return layer;
        }

        static LayerAssembly createMessagingLayer( ApplicationAssembly app ) throws AssemblyException
        {
            LayerAssembly layer = app.layer( "messaging-layer" );
            createWebServiceModule( layer );
            createMessagingPersistenceModule( layer );
            return layer;
        }

        static LayerAssembly createAuth2Layer( ApplicationAssembly application ) throws AssemblyException
        {
            LayerAssembly layer = application.layer( "auth2-layer" );
            createAuthModule( layer );
            return layer;
        }

        static void createCustomerWebModule( LayerAssembly layer ) throws AssemblyException
        {
            ModuleAssembly assembly = layer.module( "customer-web-module" );
            assembly.transients( CustomerViewComposite.class, CustomerEditComposite.class,
                                 CustomerListViewComposite.class, CustomerSearchComposite.class );
        }

        static void createCustomerDomainModule( LayerAssembly layer ) throws AssemblyException
        {
            ModuleAssembly assembly = layer.module( "customer-domain-module" );
            assembly.entities( CustomerEntity.class, CountryEntity.class );
            assembly.values( AddressValue.class );
        }

        static void createAuthModule( LayerAssembly layer ) throws AssemblyException
        {
            ModuleAssembly assembly = layer.module( "auth-module" );
            new LdapAuthenticationAssembler().assemble( assembly );
            new ThrinkAuthorizationAssembler().assemble( assembly );
            new UserTrackingAuditAssembler().assemble( assembly );
        }

        static void createPersistenceModule( LayerAssembly layer ) throws AssemblyException
        {
            ModuleAssembly assembly = layer.module( "persistence-module" );
            // Someone has created an assembler for the Neo EntityStore
            new NeoAssembler( "./neostore" ).assemble( assembly );
        }

        // END SNIPPET: full
        private static void createWebServiceModule( LayerAssembly layer ) throws AssemblyException
        {
        }

        private static void createMessagingPersistenceModule( LayerAssembly layer ) throws AssemblyException
        {
        }

    }

}
