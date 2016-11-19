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
package org.apache.zest.library.http;

import java.io.IOException;
import org.apache.http.client.methods.HttpGet;
import org.junit.Test;
import org.apache.zest.api.common.Visibility;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.test.EntityTestAssembler;

import static org.junit.Assert.assertEquals;
import static org.apache.zest.library.http.Servlets.addServlets;
import static org.apache.zest.library.http.Servlets.serve;

public class MutualSecureJettyServiceTest
    extends AbstractSecureJettyTest
{
    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        ModuleAssembly configModule = module;
        new EntityTestAssembler().assemble( configModule );
        new SecureJettyServiceAssembler().withConfig( configModule, Visibility.layer ).assemble( module );
        // START SNIPPET: config
        SecureJettyConfiguration config = configModule.forMixin( SecureJettyConfiguration.class ).declareDefaults();
        config.hostName().set( "127.0.0.1" );
        config.port().set( HTTPS_PORT );

        config.keystorePath().set( getKeyStoreFile( SERVER_KEYSTORE_FILENAME ).getAbsolutePath() );
        config.keystoreType().set( "JCEKS" );
        config.keystorePassword().set( KS_PASSWORD );

        config.truststorePath().set( getKeyStoreFile( TRUSTSTORE_FILENAME ).getAbsolutePath() );
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
