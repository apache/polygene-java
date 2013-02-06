/*
 * Copyright (c) 2011, Paul Merlin. All Rights Reserved.
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
package org.qi4j.library.http;

import java.io.IOException;
import org.apache.http.client.methods.HttpGet;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.library.http.dns.LocalManagedDns;
import org.qi4j.test.EntityTestAssembler;

import static org.junit.Assert.assertEquals;
import static org.qi4j.library.http.Servlets.addServlets;
import static org.qi4j.library.http.Servlets.serve;

public class VirtualHostJettyServiceTest
        extends AbstractJettyTest
{

    private static final String HOST1 = "host1.http.library.qi4j";
    private static final String HOST2 = "host2.http.library.qi4j";

    public void assemble( ModuleAssembly module )
            throws AssemblyException
    {
        new EntityTestAssembler().assemble( module );
        new JettyServiceAssembler().assemble( module );

        SecureJettyConfiguration config = module.forMixin( SecureJettyConfiguration.class ).declareDefaults();
        config.hostName().set( "127.0.0.1" );
        config.port().set( HTTP_PORT );
        config.virtualHosts().set( HOST1 + "," + HOST2 );

        addServlets( serve( "/hello" ).with( HelloWorldServletService.class ) ).to( module );
    }

    @BeforeClass
    public static void beforeVirtualHostsClass()
    {
        // Ignore this test on IBM JDK
        Assume.assumeTrue( !( System.getProperty( "java.vendor" ).contains( "IBM" ) ) );

        LocalManagedDns.putName( HOST1, "127.0.0.1" );
        LocalManagedDns.putName( HOST2, "127.0.0.1" );
    }

    @AfterClass
    public static void afterVirtualHostsClass()
    {
        LocalManagedDns.removeName( HOST1 );
        LocalManagedDns.removeName( HOST2 );
    }

    @Test
    public void test()
            throws IOException
    {
        // Available on HOST1 and HOST2
        String output = defaultHttpClient.execute( new HttpGet( "http://" + HOST1 + ":8041/hello" ), stringResponseHandler );
        assertEquals( "Hello World", output );

        output = defaultHttpClient.execute( new HttpGet( "http://" + HOST2 + ":8041/hello" ), stringResponseHandler );
        assertEquals( "Hello World", output );
    }

}
