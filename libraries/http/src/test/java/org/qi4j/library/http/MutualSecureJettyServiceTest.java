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

import static org.junit.Assert.assertEquals;
import org.junit.Test;

import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import static org.qi4j.library.http.Servlets.addServlets;
import static org.qi4j.library.http.Servlets.serve;

import org.apache.http.client.methods.HttpGet;
import org.qi4j.test.EntityTestAssembler;

public class MutualSecureJettyServiceTest
        extends AbstractSecureJettyTest
{

    public void assemble( ModuleAssembly module )
            throws AssemblyException
    {
        new EntityTestAssembler().assemble( module );
        new SecureJettyServiceAssembler().assemble( module );
        // START SNIPPET: config
        SecureJettyConfiguration config = module.forMixin( SecureJettyConfiguration.class ).declareDefaults();
        config.hostName().set( "127.0.0.1" );
        config.port().set( HTTPS_PORT );

        config.keystorePath().set( SERVER_KEYSTORE_PATH );
        config.keystoreType().set( "JCEKS" );
        config.keystorePassword().set( KS_PASSWORD );

        config.truststorePath().set( TRUSTSTORE_PATH );
        config.truststoreType().set( "JCEKS" );
        config.truststorePassword().set( KS_PASSWORD );

        config.wantClientAuth().set( Boolean.TRUE );
        // END SNIPPET: config

        addServlets( serve( "/hello" ).with( HelloWorldServletService.class ) ).to( module );
    }

    @Test
    public void testWithoutClientCertificate()
            throws IOException
    {
        // As we set wantClientAuth we can request without a client certificate ...
        String output = trustHttpClient.execute( new HttpGet( "https://127.0.0.1:8441/hello" ), stringResponseHandler );
        assertEquals( "Hello World", output );
    }

    @Test
    public void testWithClientCertificate()
            throws IOException
    {
        // ... and with one
        String output = mutualHttpClient.execute( new HttpGet( "https://127.0.0.1:8441/hello" ), stringResponseHandler );
        assertEquals( "Hello Mutual World", output );
    }

}
