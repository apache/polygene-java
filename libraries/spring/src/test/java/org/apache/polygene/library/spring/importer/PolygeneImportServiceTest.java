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
package org.apache.polygene.library.spring.importer;

import java.util.stream.StreamSupport;
import org.apache.polygene.api.activation.ActivationException;
import org.apache.polygene.api.injection.scope.Service;
import org.apache.polygene.api.injection.scope.Structure;
import org.apache.polygene.api.service.ServiceFinder;
import org.apache.polygene.api.service.ServiceReference;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.bootstrap.SingletonAssembler;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.apache.polygene.api.service.qualifier.ServiceQualifier.withId;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration
public final class PolygeneImportServiceTest
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
                module.objects( PolygeneImportServiceTest.class );
                // START SNIPPET: import
                new SpringImporterAssembler( appContext ).assemble( module );
                // END SNIPPET: import
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
                module.objects( PolygeneImportServiceTest.class );

                new SpringImporterAssembler( appContext ).assemble( module );
            }
        };

        assembler.module().injectTo(this);

        CommentService service = StreamSupport.stream( services.spliterator(), false )
                                              .filter( withId( "commentService2" ) )
                                              .findFirst().map( ServiceReference::get ).orElse( null );
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
                module.objects( PolygeneImportServiceTest.class );

                new SpringImporterAssembler( appContext ).assemble( module );
            }
        };

        assembler.module().injectTo( this );

        CommentService foundService = finder.findServices( CommentService.class )
                                            .filter( withId( "commentService2" ) )
                                            .findFirst().map( ServiceReference::get )
                                            .orElse( null );
        assertThat( "service with correct id has been selected", foundService.comment( "pizza" ), equalTo( "pizza is good." ) );
    }
}