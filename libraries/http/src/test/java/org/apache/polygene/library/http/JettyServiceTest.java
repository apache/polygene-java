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
package org.apache.polygene.library.http;

import java.util.Iterator;
import java.util.stream.Collectors;
import org.apache.http.client.methods.HttpGet;
import org.apache.polygene.test.util.FreePortFinder;
import org.junit.Test;
import org.apache.polygene.api.common.Visibility;
import org.apache.polygene.api.service.ServiceReference;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.test.EntityTestAssembler;

import static javax.servlet.DispatcherType.REQUEST;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.apache.polygene.library.http.Servlets.addFilters;
import static org.apache.polygene.library.http.Servlets.addServlets;
import static org.apache.polygene.library.http.Servlets.filter;
import static org.apache.polygene.library.http.Servlets.serve;

public final class JettyServiceTest
    extends AbstractJettyTest
{
    private final int httpPort = FreePortFinder.findFreePortOnLoopback();

    @Override
    public final void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        ModuleAssembly configModule = module;
        new EntityTestAssembler().assemble( configModule );

        // START SNIPPET: assembly
        // Assemble the JettyService
        new JettyServiceAssembler().withConfig( configModule, Visibility.layer ).assemble( module );

        // Set HTTP port as JettyConfiguration default
        JettyConfiguration config = configModule.forMixin( JettyConfiguration.class ).declareDefaults();
        config.hostName().set( "127.0.0.1" );
        config.port().set( httpPort );

        // Serve /helloWorld with HelloWorldServletService
        addServlets( serve( "/helloWorld" ).with( HelloWorldServletService.class ) ).to( module );

        // Filter requests on /* through provided UnitOfWorkFilterService
        addFilters( filter( "/*" ).through( UnitOfWorkFilterService.class ).on( REQUEST ) ).to( module );
        // END SNIPPET: assembly
    }

    @Test
    public final void testInstantiation()
        throws Throwable
    {
        Iterable<ServiceReference<JettyService>> services = serviceFinder.findServices( JettyService.class )
                                                                         .collect( Collectors.toList() );
        assertNotNull( services );

        Iterator<ServiceReference<JettyService>> iterator = services.iterator();
        assertTrue( iterator.hasNext() );

        ServiceReference<JettyService> serviceRef = iterator.next();
        assertNotNull( serviceRef );

        JettyService jettyService = serviceRef.get();
        assertNotNull( jettyService );

        String output = defaultHttpClient.execute( new HttpGet( "http://127.0.0.1:" + httpPort + "/helloWorld" ),
                                                   stringResponseHandler );
        assertEquals( "Hello World", output );
    }
}
