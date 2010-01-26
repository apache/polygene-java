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

package org.qi4j.runtime.service;

import org.junit.Test;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.service.ServiceSelector;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.SingletonAssembler;
import org.qi4j.spi.service.importer.ServiceSelectorImporter;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

/**
 * Test of service selector importer
 */
public class ServiceSelectorImporterTest
{
    @Test
    public void givenManyServicesWhenInjectServiceThenGetFirstOne()
    {
        SingletonAssembler assembler = new SingletonAssembler()
        {
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                module.addObjects( ServiceConsumer.class );
                module.addServices( TestServiceComposite1.class,
                                    TestServiceComposite2.class );
            }
        };

        TestService service = assembler.objectBuilderFactory().newObject( ServiceConsumer.class ).getService();

        assertThat( "service is first one", service.test(), equalTo( "mixin1" ) );
    }

    @Test
    public void givenManyServicesAndFilterWhenInjectServiceThenGetSpecifiedOne()
    {
        SingletonAssembler assembler = new SingletonAssembler()
        {
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                module.addObjects( ServiceConsumer.class );

                module.importServices( TestService.class )
                    .importedBy( ServiceSelectorImporter.class )
                    .setMetaInfo( ServiceSelector.withId( TestServiceComposite2.class.getSimpleName() ) );

                ModuleAssembly module2 = module.layerAssembly().moduleAssembly( "Other module" );
                module2.addServices( TestServiceComposite2.class, TestServiceComposite2.class )
                    .visibleIn( Visibility.layer );
            }
        };

        TestService service = assembler.objectBuilderFactory().newObject( ServiceConsumer.class ).getService();

        assertThat( "service is specified one", service.test(), equalTo( "mixin2" ) );
    }

    @Test
    public void givenManyServicesAndSelectFirstWhenInjectServiceThenDontGetSelf()
    {
        SingletonAssembler assembler = new SingletonAssembler()
        {
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                module.addObjects( ServiceConsumer.class );

                module.importServices( TestService.class )
                    .importedBy( ServiceSelectorImporter.class )
                    .setMetaInfo( ServiceSelector.first() );

                ModuleAssembly module2 = module.layerAssembly().moduleAssembly( "Other module" );
                module2.addServices( TestServiceComposite2.class, TestServiceComposite2.class )
                    .visibleIn( Visibility.layer );
            }
        };

        TestService service = assembler.objectBuilderFactory().newObject( ServiceConsumer.class ).getService();

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
