/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package org.apache.polygene.bootstrap;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import org.apache.polygene.api.activation.ActivationException;
import org.apache.polygene.api.common.Visibility;
import org.apache.polygene.api.property.Property;
import org.apache.polygene.api.service.importer.InstanceImporter;
import org.apache.polygene.api.service.importer.NewObjectImporter;
import org.apache.polygene.api.structure.Application;
import org.apache.polygene.api.structure.ApplicationDescriptor;
import org.apache.polygene.api.structure.Module;

@SuppressWarnings( { "unused", "ConstantConditionalExpression", "MethodNameSameAsClassName" } )
public class DocumentationSupport
{
    public static Predicate<ObjectAssembly> hasMyTypeSpecification =
        item -> item.types().anyMatch( type -> type.equals( String.class ) );

    public static class declarations
    {
        static class MyObject {}

        interface MyTransient {}

        interface MyValue {}

        interface MyEntity {}

        class MyService {}

        void declaration()
        {
            Assembler objects =
                // START SNIPPET: objects
                ( ModuleAssembly module ) -> module.objects( MyObject.class ).visibleIn( Visibility.layer )
                // END SNIPPET: objects
                ;
            Assembler transients =
                // START SNIPPET: transients
                ( ModuleAssembly module ) -> module.transients( MyTransient.class ).visibleIn( Visibility.layer )
                // END SNIPPET: transients
                ;
            Assembler values =
                // START SNIPPET: values
                ( ModuleAssembly module ) -> module.values( MyValue.class ).visibleIn( Visibility.layer )
                // END SNIPPET: values
                ;
            Assembler entities =
                // START SNIPPET: entities
                ( ModuleAssembly module ) -> module.entities( MyEntity.class ).visibleIn( Visibility.layer )
                // END SNIPPET: entities
                ;
            Assembler services =
                // START SNIPPET: services
                ( ModuleAssembly module ) -> module.services( MyService.class ).visibleIn( Visibility.layer )
                // END SNIPPET: services
                ;
            Assembler taggedServices =
                // START SNIPPET: tagged-services
                ( ModuleAssembly module ) -> module.services( MyService.class ).taggedWith( "foo", "bar" )
                // END SNIPPET: tagged-services
                ;
            List<Assembler> importedServices = Arrays.asList(
                // START SNIPPET: imported-services
                ( ModuleAssembly module ) -> module.importedServices( MyService.class )
                                                   .importedBy( InstanceImporter.class )
                                                   .setMetaInfo( new MyService() ),

                // OR

                ( ModuleAssembly module ) -> {
                    module.objects( MyService.class );
                    module.importedServices( MyService.class ).importedBy( NewObjectImporter.class );
                }
                // END SNIPPET: imported-services
            );
        }
    }

    static class defaultPropertyValues
    {
        interface MyValue
        {
            Property<String> foo();
        }

        interface MyEntity
        {
            Property<String> cathedral();
        }

        void defaultPropertyValues()
        {
            Assembler assembler =
                // START SNIPPET: properties-defaults
                ( ModuleAssembly module ) -> {
                    module.values( MyValue.class );
                    MyValue myValueDefaults = module.forMixin( MyValue.class ).declareDefaults();
                    myValueDefaults.foo().set( "bar" );

                    module.entities( MyEntity.class );
                    MyEntity myEntityDefaults = module.forMixin( MyEntity.class ).declareDefaults();
                    myEntityDefaults.cathedral().set( "bazar" );
                }
                // END SNIPPET: properties-defaults
                ;
        }
    }

    public static class singleton
    {
        interface MyService {}

        interface Stuff {}

        void singleton()
            throws ActivationException, AssemblyException
        {
            // START SNIPPET: singleton
            SingletonAssembler assembler = new SingletonAssembler(
                module -> {
                    module.services( MyService.class ).identifiedBy( "Foo" );
                    module.services( MyService.class ).identifiedBy( "Bar" );
                    module.objects( Stuff.class );
                }
            );
            Module module = assembler.module();
            Stuff stuff = module.newObject( Stuff.class );
            // END SNIPPET: singleton
        }
    }

    public static class pancake
    {

        public static class LoginAssembler implements Assembler
        {
            public void assemble( ModuleAssembly module ) { }
        }

        public static class MenuAssembler implements Assembler
        {
            public void assemble( ModuleAssembly module ) { }
        }

        public static class PerspectivesAssembler implements Assembler
        {
            public void assemble( ModuleAssembly module ) { }
        }

        public static class ViewsAssembler implements Assembler
        {
            public void assemble( ModuleAssembly module ) { }
        }

        public static class ReportingAssembler implements Assembler
        {
            public void assemble( ModuleAssembly module ) { }
        }

        public static class PdfAssembler implements Assembler
        {
            public void assemble( ModuleAssembly module ) { }
        }

        public static class BookkeepingAssembler implements Assembler
        {
            public void assemble( ModuleAssembly module ) { }
        }

        public static class CashFlowAssembler implements Assembler
        {
            public void assemble( ModuleAssembly module ) { }
        }

        public static class BalanceSheetAssembler implements Assembler
        {
            public void assemble( ModuleAssembly module ) { }
        }

        public static class PricingAssembler implements Assembler
        {
            public void assemble( ModuleAssembly module ) { }
        }

        public static class ProductAssembler implements Assembler
        {
            public void assemble( ModuleAssembly module ) { }
        }

        private static Energy4Java polygene;

        // START SNIPPET: pancake
        public static void main( String[] args )
            throws Exception
        {
            polygene = new Energy4Java();
            Assembler[][][] assemblers = new Assembler[][][] {
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
            Application runtime = model.newInstance( polygene.spi() );
            runtime.activate();
        }

        private static ApplicationDescriptor newApplication( final Assembler[][][] assemblers )
            throws AssemblyException
        {
            return polygene.newApplicationModel( factory -> factory.newApplicationAssembly( assemblers ) );
        }
        // END SNIPPET: pancake
    }

    public static class full
    {

        static class CustomerViewComposite {}

        static class CustomerEditComposite {}

        static class CustomerListViewComposite {}

        static class CustomerSearchComposite {}

        static class CustomerEntity {}

        static class CountryEntity {}

        public static class AddressValue {}

        public static class LdapAuthenticationAssembler implements Assembler
        {
            public void assemble( ModuleAssembly module ) throws AssemblyException { }
        }

        public static class ThrinkAuthorizationAssembler implements Assembler
        {
            public void assemble( ModuleAssembly module ) throws AssemblyException { }
        }

        public static class UserTrackingAuditAssembler implements Assembler
        {
            public void assemble( ModuleAssembly module ) throws AssemblyException { }
        }

        public static class NeoAssembler implements Assembler
        {
            NeoAssembler( String path ) {}

            public void assemble( ModuleAssembly module ) throws AssemblyException { }
        }

        // START SNIPPET: full
        private static Energy4Java polygene;

        private static Application application;

        public static void main( String[] args )
            throws Exception
        {
            // Create a Polygene Runtime
            polygene = new Energy4Java();
            // Create the application
            application = polygene.newApplication( factory -> buildAssembly( factory.newApplicationAssembly() ) );
            // Activate the application
            application.activate();
        }

        static ApplicationAssembly buildAssembly( ApplicationAssembly app ) throws AssemblyException
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

            return app;
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
