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
import javax.net.ssl.SSLPeerUnverifiedException;
import static javax.servlet.DispatcherType.REQUEST;

import org.apache.http.NoHttpResponseException;
import org.apache.http.client.methods.HttpGet;

import static org.junit.Assert.*;
import org.junit.Test;

import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.EntityTestAssembler;
import static org.qi4j.library.http.Servlets.*;

public class SecureJettyServiceTest
        extends AbstractSecureJettyTest
{

    public void assemble( ModuleAssembly module )
            throws AssemblyException
    {
        new EntityTestAssembler().assemble( module );
        // START SNIPPET: assemblyssl
        new SecureJettyServiceAssembler().assemble( module );
        // END SNIPPET: assemblyssl

        // START SNIPPET: configssl
        SecureJettyConfiguration config = module.forMixin( SecureJettyConfiguration.class ).declareDefaults();
        config.hostName().set( "127.0.0.1" );
        config.port().set( HTTPS_PORT );
        config.keystorePath().set( SERVER_KEYSTORE_PATH );
        config.keystoreType().set( "JCEKS" );
        config.keystorePassword().set( KS_PASSWORD );
        // END SNIPPET: configssl

        // START SNIPPET: assemblyssl
        addServlets( serve( "/hello" ).with( HelloWorldServletService.class ) ).to( module );
        addFilters( filter( "/*" ).through( UnitOfWorkFilterService.class ).on( REQUEST ) ).to( module );
        // END SNIPPET: assemblyssl
    }

    @Test
    // This test exists for demonstration purpose only, it do not test usefull things but it's on purpose
    public void testNoSSL()
            throws IOException
    {
        try {
            HttpGet get = new HttpGet( "http://127.0.0.1:8441/hello" );
            defaultHttpClient.execute( get );
            fail( "We could reach the HTTPS connector using a HTTP url, that's no good" );
        } catch ( NoHttpResponseException ex ) {
            // Expected
        }
    }

    @Test
    // This test exists for demonstration purpose only, it do not test usefull things but it's on purpose
    public void testNoTruststore()
            throws IOException
    {
        try {
            defaultHttpClient.execute( new HttpGet( "https://127.0.0.1:8441/hello" ) );
            fail( "We could reach the HTTPS connector without proper truststore, this should not happen" );
        } catch ( SSLPeerUnverifiedException ex ) {
            // Expected
        }
    }

    @Test
    public void testTrust()
            throws IOException, InterruptedException
    {
        String output = trustHttpClient.execute( new HttpGet( "https://127.0.0.1:8441/hello" ), stringResponseHandler );
        assertEquals( "Hello World", output );
    }

}
