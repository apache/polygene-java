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
import org.qi4j.api.common.Visibility;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Test import of singleton services
 */
public class InstanceImporterTest
    extends AbstractQi4jTest
{
    @Service
    TestInterface service;

    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        ModuleAssembly serviceModule = module.layer().module( "Service module" );
        serviceModule.importedServices( TestInterface.class )
            .setMetaInfo( new TestService() )
            .visibleIn( Visibility.layer );
        module.objects( InstanceImporterTest.class );
    }

    @Test
    public void givenSingletonServiceObjectWhenServicesAreInjectedThenSingletonIsFound()
    {
        module.injectTo( this );

        assertThat( "service is injected properly", service.helloWorld(), equalTo( "Hello World" ) );
    }

    public interface TestInterface
    {
        public String helloWorld();
    }

    public static class TestService
        implements TestInterface
    {
        public String helloWorld()
        {
            return "Hello World";
        }
    }
}
