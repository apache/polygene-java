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
package org.apache.zest.library.http;

import java.io.IOException;
import javax.net.ssl.SSLPeerUnverifiedException;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.methods.HttpGet;
import org.junit.Test;
import org.apache.zest.api.common.Visibility;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.test.EntityTestAssembler;

import static javax.servlet.DispatcherType.REQUEST;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.apache.zest.library.http.Servlets.addFilters;
import static org.apache.zest.library.http.Servlets.addServlets;
import static org.apache.zest.library.http.Servlets.filter;
import static org.apache.zest.library.http.Servlets.serve;

public class SecureJettyServiceTest
    extends AbstractSecureJettyTest
{
    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        ModuleAssembly configModule = module;
        new EntityTestAssembler().assemble( configModule );
        // START SNIPPET: assemblyssl
        new SecureJettyServiceAssembler().withConfig( configModule, Visibility.layer ).assemble( module );
        // END SNIPPET: assemblyssl

        // START SNIPPET: configssl
        SecureJettyConfiguration config = configModule.forMixin( SecureJettyConfiguration.class ).declareDefaults();
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
        try
        {
            HttpGet get = new HttpGet( "http://127.0.0.1:8441/hello" );
            defaultHttpClient.execute( get );
            fail( "We could reach the HTTPS connector using a HTTP url, that's no good" );
        }
        catch( NoHttpResponseException ex )
        {
            // Expected
        }
    }

    @Test
    // This test exists for demonstration purpose only, it do not test usefull things but it's on purpose
    public void testNoTruststore()
        throws IOException
    {
        try
        {
            defaultHttpClient.execute( new HttpGet( "https://127.0.0.1:8441/hello" ) );
            fail( "We could reach the HTTPS connector without proper truststore, this should not happen" );
        }
        catch( SSLPeerUnverifiedException ex )
        {
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
