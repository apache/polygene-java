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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.qi4j.api.activation.ActivationException;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.service.ServiceFinder;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.service.qualifier.ServiceQualifier;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.SingletonAssembler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.qi4j.api.service.qualifier.ServiceQualifier.firstService;
import static org.qi4j.api.service.qualifier.ServiceQualifier.withId;

@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration
public final class Qi4jImportServiceTest
{
    @Autowired ApplicationContext appContext;

    @Service CommentService service;

    @Test
    public final void givenImportedSpringServicesWhenServiceIsInjectedThenUseSpringService()
        throws ActivationException, AssemblyException
    {
        SingletonAssembler assembler = new SingletonAssembler()
        {
            public void assemble( ModuleAssembly module ) throws AssemblyException
            {
                module.objects( Qi4jImportServiceTest.class );

                new SpringImporterAssembler( appContext ).assemble( module );
            }
        };

        assembler.module().injectTo( this );

        assertThat( "service can be called", service.comment( "beer" ), equalTo( "beer is good." ) );
    }

    @Service Iterable<ServiceReference<CommentService>> services;

    @Test
    public final void givenImportedSpringServicesWhenServicesAreInjectedThenCanIdentifyByName()
        throws ActivationException, AssemblyException
    {
        SingletonAssembler assembler = new SingletonAssembler()
        {
            public void assemble( ModuleAssembly module ) throws AssemblyException
            {
                module.objects( Qi4jImportServiceTest.class );

                new SpringImporterAssembler( appContext ).assemble( module );
            }
        };

        assembler.module().injectTo(this);

        CommentService service = firstService( withId( "commentService2" ), services );
        assertThat( "service with correct id has been selected", service.comment( "pizza" ), equalTo( "pizza is good." ) );
    }

    @Structure ServiceFinder finder;

    @Test
    public final void givenImportedSpringServicesWhenServicesAreFoundThenCanIdentifyByName()
        throws ActivationException, AssemblyException
    {
        SingletonAssembler assembler = new SingletonAssembler()
        {
            public void assemble( ModuleAssembly module ) throws AssemblyException
            {
                module.objects( Qi4jImportServiceTest.class );

                new SpringImporterAssembler( appContext ).assemble( module );
            }
        };

        assembler.module().injectTo( this );

        CommentService foundService = ServiceQualifier.firstService( withId( "commentService2" ), finder.<CommentService>findServices( CommentService.class ));
        assertThat( "service with correct id has been selected", foundService.comment( "pizza" ), equalTo( "pizza is good." ) );
    }
}