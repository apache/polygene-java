/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.zest.bootstrap;

import org.apache.zest.api.activation.ActivationException;
import org.apache.zest.api.common.Visibility;
import org.apache.zest.api.property.Property;
import org.apache.zest.api.service.importer.InstanceImporter;
import org.apache.zest.api.service.importer.NewObjectImporter;
import org.apache.zest.api.structure.Application;
import org.apache.zest.api.structure.ApplicationDescriptor;
import org.apache.zest.api.structure.Module;
import org.apache.zest.functional.Iterables;
import org.apache.zest.functional.Specification;

public class DocumentationSupport
{

    public static Specification<ObjectAssembly> hasMyTypeSpecification = new Specification<ObjectAssembly>()
    {

        public boolean satisfiedBy( ObjectAssembly item )
        {
            return Iterables.toList( item.types() ).contains( String.class );
        }

    };

    public static class objects
            implements Assembler
    {

        public static class MyObject {}

        // START SNIPPET: objects
        @Override
        public void assemble( ModuleAssembly module )
                throws AssemblyException
        {
            module.objects( MyObject.class ).visibleIn( Visibility.layer );
        }
        // END SNIPPET: objects

    }

    public static class transients
            implements Assembler
    {

        public static interface MyTransient {}

        // START SNIPPET: transients
        @Override
        public void assemble( ModuleAssembly module )
                throws AssemblyException
        {
            module.transients( MyTransient.class ).visibleIn( Visibility.layer );
        }
        // END SNIPPET: transients

    }

    public static class values
            implements Assembler
    {

        public static interface MyValue {}

        // START SNIPPET: values
        @Override
        public void assemble( ModuleAssembly module )
                throws AssemblyException
        {
            module.values( MyValue.class ).visibleIn( Visibility.layer );
        }
        // END SNIPPET: values

    }

    public static class entities
            implements Assembler
    {

        public static interface MyEntity {}

        // START SNIPPET: entities
        @Override
        public void assemble( ModuleAssembly module )
                throws AssemblyException
        {
            module.entities( MyEntity.class ).visibleIn( Visibility.layer );
        }
        // END SNIPPET: entities

    }

    public static class services
            implements Assembler
    {

        public static interface MyService {}

        // START SNIPPET: services
        @Override
        public void assemble( ModuleAssembly module )
                throws AssemblyException
        {
            module.services( MyService.class ).visibleIn( Visibility.layer );
        }
        // END SNIPPET: services

    }

    public static class taggedServices
        implements Assembler
    {

        public static interface MyService {}

        // START SNIPPET: tagged-services
        @Override
        public void assemble( ModuleAssembly module )
            throws AssemblyException
        {
            module.services( MyService.class ).taggedWith( "foo", "bar" );
        }
        // END SNIPPET: tagged-services
    }

    public static class importedServices
        implements Assembler
    {

        public static class MyService {}

        // START SNIPPET: imported-services
        @Override
        public void assemble( ModuleAssembly module )
            throws AssemblyException
        {
            module.importedServices( MyService.class ).
                importedBy( InstanceImporter.class ).
                setMetaInfo( new MyService() );

            // OR

            module.objects( MyService.class );
            module.importedServices( MyService.class ).
                importedBy( NewObjectImporter.class );
        }
        // END SNIPPET: imported-services
    }

    public static class defaultPropertyValues
        implements Assembler
    {

        public interface MyValue { Property<String> foo(); }
        public interface MyEntity { Property<String> cathedral(); }

        // START SNIPPET: properties-defaults
        @Override
        public void assemble( ModuleAssembly module )
            throws AssemblyException
        {
            module.values( MyValue.class );
            MyValue myValueDefaults = module.forMixin( MyValue.class ).declareDefaults();
            myValueDefaults.foo().set( "bar" );

            module.entities( MyEntity.class );
            MyEntity myEntityDefaults = module.forMixin( MyEntity.class ).declareDefaults();
            myEntityDefaults.cathedral().set( "bazar" );
        }
        // END SNIPPET: properties-defaults
    }

    public static class singleton
    {

        public interface MyService { }
        public interface Stuff { }

        void singleton()
            throws ActivationException, AssemblyException
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

    public static class pancake
    {

        public static class LoginAssembler implements Assembler { public void assemble( ModuleAssembly module ) throws AssemblyException { } }
        public static class MenuAssembler implements Assembler { public void assemble( ModuleAssembly module ) throws AssemblyException { } }
        public static class PerspectivesAssembler implements Assembler { public void assemble( ModuleAssembly module ) throws AssemblyException { } }
        public static class ViewsAssembler implements Assembler { public void assemble( ModuleAssembly module ) throws AssemblyException { } }
        public static class ReportingAssembler implements Assembler { public void assemble( ModuleAssembly module ) throws AssemblyException { } }
        public static class PdfAssembler implements Assembler { public void assemble( ModuleAssembly module ) throws AssemblyException { } }
        public static class BookkeepingAssembler implements Assembler { public void assemble( ModuleAssembly module ) throws AssemblyException { } }
        public static class CashFlowAssembler implements Assembler { public void assemble( ModuleAssembly module ) throws AssemblyException { } }
        public static class BalanceSheetAssembler implements Assembler { public void assemble( ModuleAssembly module ) throws AssemblyException { } }
        public static class PricingAssembler implements Assembler { public void assemble( ModuleAssembly module ) throws AssemblyException { } }
        public static class ProductAssembler implements Assembler { public void assemble( ModuleAssembly module ) throws AssemblyException { } }

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

    public static class full
    {

        public static class CustomerViewComposite{}
        public static class CustomerEditComposite{}
        public static class CustomerListViewComposite{}
        public static class CustomerSearchComposite{}
        public static class CustomerEntity{}
        public static class CountryEntity{}
        public static class AddressValue{}
        public static class LdapAuthenticationAssembler implements Assembler{ public void assemble( ModuleAssembly module ) throws AssemblyException { } }
        public static class ThrinkAuthorizationAssembler implements Assembler{ public void assemble( ModuleAssembly module ) throws AssemblyException { } }
        public static class UserTrackingAuditAssembler implements Assembler{ public void assemble( ModuleAssembly module ) throws AssemblyException { } }
        public static class NeoAssembler implements Assembler{ NeoAssembler( String path ) {} public void assemble( ModuleAssembly module ) throws AssemblyException { } }

        // START SNIPPET: full
        private static Energy4Java qi4j;

        private static Application application;

        public static void main( String[] args )
                throws Exception
        {
            // Create a Zest Runtime
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
