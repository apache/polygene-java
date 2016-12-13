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

import java.io.IOException;
import org.apache.http.client.methods.HttpGet;
import org.apache.polygene.api.common.Visibility;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.library.http.dns.LocalManagedDns;
import org.apache.polygene.test.EntityTestAssembler;
import org.apache.polygene.test.util.FreePortFinder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.apache.polygene.library.http.Servlets.addServlets;
import static org.apache.polygene.library.http.Servlets.serve;
import static org.apache.polygene.test.util.Assume.assumeNoIbmJdk;
import static org.junit.Assert.assertEquals;

public class VirtualHostJettyServiceTest
    extends AbstractJettyTest
{
    private static final String HOST1 = "host1.http.library.polygene";
    private static final String HOST2 = "host2.http.library.polygene";

    private final int httpPort = FreePortFinder.findFreePortOnLoopback();

    @BeforeClass
    public static void beforeVirtualHostsClass()
    {
        assumeNoIbmJdk();
        LocalManagedDns.putName( HOST1, "127.0.0.1" );
        LocalManagedDns.putName( HOST2, "127.0.0.1" );
    }

    @AfterClass
    public static void afterVirtualHostsClass()
    {
        LocalManagedDns.removeName( HOST1 );
        LocalManagedDns.removeName( HOST2 );
    }

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        ModuleAssembly configModule = module;
        new EntityTestAssembler().assemble( configModule );
        new JettyServiceAssembler().withConfig( configModule, Visibility.layer ).assemble( module );

        SecureJettyConfiguration config = configModule.forMixin( SecureJettyConfiguration.class ).declareDefaults();
        config.hostName().set( "127.0.0.1" );
        config.port().set( httpPort );
        config.virtualHosts().set( HOST1 + "," + HOST2 );

        addServlets( serve( "/hello" ).with( HelloWorldServletService.class ) ).to( module );
    }

    @Test
    public void test()
        throws IOException
    {
        // Available on HOST1 and HOST2
        String output = defaultHttpClient.execute( new HttpGet( "http://" + HOST1 + ":" + httpPort + "/hello" ),
                                                   stringResponseHandler );
        assertEquals( "Hello World", output );

        output = defaultHttpClient.execute( new HttpGet( "http://" + HOST2 + ":" + httpPort + "/hello" ),
                                            stringResponseHandler );
        assertEquals( "Hello World", output );
    }
}
