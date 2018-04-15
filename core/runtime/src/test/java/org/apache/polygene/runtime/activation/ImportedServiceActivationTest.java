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
package org.apache.polygene.runtime.activation;

import org.apache.polygene.api.activation.Activator;
import org.apache.polygene.api.identity.StringIdentity;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.api.service.ImportedServiceDescriptor;
import org.apache.polygene.api.service.ServiceComposite;
import org.apache.polygene.api.service.ServiceImporter;
import org.apache.polygene.api.service.ServiceImporterException;
import org.apache.polygene.api.service.ServiceReference;
import org.apache.polygene.api.structure.Application;
import org.apache.polygene.bootstrap.ImportedServiceDeclaration;
import org.apache.polygene.bootstrap.SingletonAssembler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.fail;

public class ImportedServiceActivationTest
{

    private static int activationLevel = 0;

    private static int passivationLevel = 0;

    public static class TestedActivator
        implements Activator<ServiceReference<TestedService>>
    {

        public void beforeActivation( ServiceReference<TestedService> activating )
        {
            assertThat( "Service should not be active before activation", activating.isActive(), is( false ) );
            try
            {
                activating.get();
                fail( "Service is not activated yet, the reference get method should throw IllegalStateException." );
            }
            catch( IllegalStateException expected )
            {
            }
            activationLevel++;
        }

        public void afterActivation( ServiceReference<TestedService> activated )
        {
            assertThat( "Service should be active after activation", activated.isActive(), is( true ) );
            assertThat( "After activation", activated.get().foo(), equalTo( "bar" ) );
            activationLevel++;
        }

        public void beforePassivation( ServiceReference<TestedService> passivating )
        {
            assertThat( "Service should be active before passivation", passivating.isActive(), is( true ) );
            assertThat( "Before passivation", passivating.get().foo(), equalTo( "bar" ) );
            passivationLevel++;
        }

        public void afterPassivation( ServiceReference<TestedService> passivated )
        {
            assertThat( "Service should not be active after passivation", passivated.isActive(), is( false ) );
            try
            {
                passivated.get();
                fail( "Service is passivated, the reference get method should throw IllegalStateException." );
            }
            catch( IllegalStateException expected )
            {
            }
            passivationLevel++;
        }
    }

    public interface TestedService
    {

        String foo();
    }

    public static class TestedServiceInstance
        implements TestedService
    {

        public String foo()
        {
            return "bar";
        }
    }

    @Mixins( TestedServiceImporterService.Mixin.class )
    interface TestedServiceImporterService
        extends ServiceComposite, ServiceImporter<TestedService>
    {

        class Mixin
            implements ServiceImporter<TestedService>
        {

            public TestedService importService( ImportedServiceDescriptor serviceDescriptor )
                throws ServiceImporterException
            {
                return new TestedServiceInstance();
            }

            public boolean isAvailable( TestedService instance )
            {
                return true;
            }
        }
    }

    @BeforeEach
    public void beforeEachTest()
    {
        activationLevel = 0;
        passivationLevel = 0;
    }

    @Test
    public void testNewInstanceImportedServiceActivators()
        throws Exception
    {
        SingletonAssembler assembler = new SingletonAssembler(
            module -> module.importedServices( TestedService.class )
                .withActivators( TestedActivator.class )
                .setMetaInfo( new TestedServiceInstance() )
                .importOnStartup()
        );
        Application application = assembler.application();
        assertThat( "Activation Level", activationLevel, equalTo( 2 ) );
        application.passivate();
        assertThat( "Passivation Level", passivationLevel, equalTo( 2 ) );
    }

    @Test
    public void testNewObjectImportedServiceActivators()
        throws Exception
    {
        SingletonAssembler assembler = new SingletonAssembler(
            module -> {
                module.importedServices( TestedService.class ).
                    importedBy( ImportedServiceDeclaration.NEW_OBJECT ).
                    withActivators( TestedActivator.class ).
                    importOnStartup();
                module.objects( TestedServiceInstance.class );
            }
        );
        Application application = assembler.application();
        assertThat( "Activation Level", activationLevel, equalTo( 2 ) );
        application.passivate();
        assertThat( "Passivation Level", passivationLevel, equalTo( 2 ) );
    }

    @Test
    public void testServiceImporterImportedServiceActivators()
        throws Exception
    {
        SingletonAssembler assembler = new SingletonAssembler(
            module -> {
                module.importedServices( TestedService.class ).
                    importedBy( ImportedServiceDeclaration.SERVICE_IMPORTER ).
                    setMetaInfo( StringIdentity.identityOf( "testimporter" ) ).
                    withActivators( TestedActivator.class ).
                    importOnStartup();
                module.services( TestedServiceImporterService.class ).identifiedBy( "testimporter" );
            }
        );
        Application application = assembler.application();
        assertThat( "Activation Level", activationLevel, equalTo( 2 ) );
        application.passivate();
        assertThat( "Passivation Level", passivationLevel, equalTo( 2 ) );
    }
}