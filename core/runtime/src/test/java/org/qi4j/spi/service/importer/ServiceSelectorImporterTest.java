/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.qi4j.spi.service.importer;

import org.junit.Test;
import org.qi4j.api.activation.ActivationException;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.service.qualifier.ServiceQualifier;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.SingletonAssembler;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.qi4j.bootstrap.ImportedServiceDeclaration.SERVICE_SELECTOR;

/**
 * Test of service selector importer
 */
public class ServiceSelectorImporterTest
{
    @Test
    public void givenManyServicesWhenInjectServiceThenGetFirstOne()
        throws ActivationException, AssemblyException
    {
        SingletonAssembler assembler = new SingletonAssembler()
        {
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                module.objects( ServiceConsumer.class );
                module.services( TestServiceComposite1.class,
                                 TestServiceComposite2.class );
            }
        };

        TestService service = assembler.module().newObject( ServiceConsumer.class ).getService();

        assertThat( "service is first one", service.test(), equalTo( "mixin1" ) );
    }

    @Test
    public void givenManyServicesAndFilterWhenInjectServiceThenGetSpecifiedOne()
        throws ActivationException, AssemblyException
    {
        SingletonAssembler assembler = new SingletonAssembler()
        {
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                module.objects( ServiceConsumer.class );

                module.importedServices( TestService.class )
                    .importedBy( SERVICE_SELECTOR )
                    .setMetaInfo( ServiceQualifier.withId( TestServiceComposite2.class.getSimpleName() ) );

                ModuleAssembly module2 = module.layer().module( "Other module" );
                module2.services( TestServiceComposite2.class, TestServiceComposite2.class )
                    .visibleIn( Visibility.layer );
            }
        };

        TestService service = assembler.module().newObject( ServiceConsumer.class ).getService();

        assertThat( "service is specified one", service.test(), equalTo( "mixin2" ) );
    }

    @Test
    public void givenManyServicesAndSelectFirstWhenInjectServiceThenDontGetSelf()
        throws ActivationException, AssemblyException
    {
        SingletonAssembler assembler = new SingletonAssembler()
        {
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                module.objects( ServiceConsumer.class );

                module.importedServices( TestService.class )
                    .importedBy( SERVICE_SELECTOR )
                    .setMetaInfo( ServiceQualifier.withId( "TestServiceComposite2_1" ) );

                ModuleAssembly module2 = module.layer().module( "Other module" );
                module2.addServices( TestServiceComposite2.class, TestServiceComposite2.class )
                    .visibleIn( Visibility.layer );
            }
        };

        TestService service = assembler.module().newObject( ServiceConsumer.class ).getService();

        assertThat( "service is specified one", service.test(), equalTo( "mixin2" ) );
    }

    public static class ServiceConsumer
    {
        private
        @Service
        TestService service;

        public TestService getService()
        {
            return service;
        }
    }

    @Mixins( TestMixin1.class )
    public interface TestServiceComposite1
        extends TestServiceComposite
    {
    }

    @Mixins( TestMixin2.class )
    public interface TestServiceComposite2
        extends TestServiceComposite
    {
    }

    public interface TestServiceComposite
        extends TestService, ServiceComposite
    {
    }

    public interface TestService
    {
        String test();
    }

    public static class TestMixin1
        implements TestService
    {
        public String test()
        {
            return "mixin1";
        }
    }

    public static class TestMixin2
        implements TestService
    {
        public String test()
        {
            return "mixin2";
        }
    }
}
