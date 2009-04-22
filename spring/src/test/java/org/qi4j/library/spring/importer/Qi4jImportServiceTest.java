/*  Copyright 2008 Rickard Öberg.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
* implied.
*
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.qi4j.library.spring.importer;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.service.ServiceFinder;
import org.qi4j.api.service.ServiceReference;
import static org.qi4j.api.service.ServiceSelector.service;
import static org.qi4j.api.service.ServiceSelector.withId;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.SingletonAssembler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration
public final class Qi4jImportServiceTest
{
    @Autowired ApplicationContext appContext;

    @Service CommentService service;

    @Test
    public final void givenImportedSpringServicesWhenServiceIsInjectedThenUseSpringService()
    {
        SingletonAssembler assembler = new SingletonAssembler()
        {
            public void assemble( ModuleAssembly module ) throws AssemblyException
            {
                module.addObjects( Qi4jImportServiceTest.class );

                new SpringImporterAssembler( appContext ).assemble( module );
            }
        };

        assembler.objectBuilderFactory().newObjectBuilder( Qi4jImportServiceTest.class ).injectTo( this );

        assertThat( "service can be called", service.comment( "beer" ), equalTo( "beer is good." ) );
    }

    @Service Iterable<ServiceReference<CommentService>> services;

    @Test
    public final void givenImportedSpringServicesWhenServicesAreInjectedThenCanIdentifyByName()
    {
        SingletonAssembler assembler = new SingletonAssembler()
        {
            public void assemble( ModuleAssembly module ) throws AssemblyException
            {
                module.addObjects( Qi4jImportServiceTest.class );

                new SpringImporterAssembler( appContext ).assemble( module );
            }
        };

        assembler.objectBuilderFactory().newObjectBuilder( Qi4jImportServiceTest.class ).injectTo( this );

        CommentService service = service( services, withId( "commentService2" ) );
        assertThat( "service with correct id has been selected", service.comment( "pizza" ), equalTo( "pizza is good." ) );
    }

    @Structure ServiceFinder finder;

    @Test
    public final void givenImportedSpringServicesWhenServicesAreFoundThenCanIdentifyByName()
    {
        SingletonAssembler assembler = new SingletonAssembler()
        {
            public void assemble( ModuleAssembly module ) throws AssemblyException
            {
                module.addObjects( Qi4jImportServiceTest.class );

                new SpringImporterAssembler( appContext ).assemble( module );
            }
        };

        assembler.objectBuilderFactory().newObjectBuilder( Qi4jImportServiceTest.class ).injectTo( this );

        CommentService foundService = service( finder.<CommentService>findServices( CommentService.class ), withId( "commentService2" ) );
        assertThat( "service with correct id has been selected", foundService.comment( "pizza" ), equalTo( "pizza is good." ) );
    }
}