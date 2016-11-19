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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

/**
 * Base class for SecureJettyMixin tests.
 *
 * Use HttpClient in order to easily use different {@link SSLContext}s between server and client.
 */
public abstract class AbstractSecureJettyTest
    extends AbstractJettyTest
{
    protected static final int HTTPS_PORT = 8441;
    protected static final String KS_PASSWORD = "changeit";
    protected static final String CLIENT_KEYSTORE_FILENAME = "zest-lib-http-unittests-client-cert.jceks";
    protected static final String SERVER_KEYSTORE_FILENAME = "zest-lib-http-unittests-server-cert.jceks";
    protected static final String TRUSTSTORE_FILENAME = "zest-lib-http-unittests-ca.jceks";

    // These two clients use a HostnameVerifier that don't do any check, don't do this in production code
    protected CloseableHttpClient trustHttpClient;
    protected CloseableHttpClient mutualHttpClient;

    @Rule
    public final TemporaryFolder tmpDir = new TemporaryFolder();

    @Before
    public void beforeSecure()
        throws GeneralSecurityException, IOException
    {
        // Trust HTTP Client
        trustHttpClient = HttpClients.custom()
                                     .setSSLHostnameVerifier( NoopHostnameVerifier.INSTANCE )
                                     .setSSLContext( buildTrustSSLContext() )
                                     .build();
        // Mutual HTTP Client
        mutualHttpClient = HttpClients.custom()
                                      .setSSLHostnameVerifier( NoopHostnameVerifier.INSTANCE )
                                      .setSSLContext( buildMutualSSLContext() )
                                      .build();
    }

    @After
    public void afterSecure()
        throws IOException
    {
        if( trustHttpClient != null )
        {
            trustHttpClient.close();
        }
        if( mutualHttpClient != null )
        {
            mutualHttpClient.close();
        }
    }

    private static HostnameVerifier defaultHostnameVerifier;
    private static javax.net.ssl.SSLSocketFactory defaultSSLSocketFactory;

    @BeforeClass
    public static void beforeSecureClass()
        throws IOException, GeneralSecurityException
    {
        defaultHostnameVerifier = HttpsURLConnection.getDefaultHostnameVerifier();
        defaultSSLSocketFactory = HttpsURLConnection.getDefaultSSLSocketFactory();
        HttpsURLConnection.setDefaultHostnameVerifier( ( string, ssls ) -> true );
        HttpsURLConnection.setDefaultSSLSocketFactory( buildTrustSSLContext().getSocketFactory() );
    }

    @AfterClass
    public static void afterSecureClass()
    {
        HttpsURLConnection.setDefaultHostnameVerifier( defaultHostnameVerifier );
        HttpsURLConnection.setDefaultSSLSocketFactory( defaultSSLSocketFactory );
    }

    protected static String getX509Algorithm()
    {
        return System.getProperty( "java.vendor" ).contains( "IBM" ) ? "IbmX509" : "SunX509";
    }

    private static SSLContext buildTrustSSLContext()
        throws IOException, GeneralSecurityException
    {
        SSLContext sslCtx = SSLContext.getInstance( "TLS" );
        TrustManagerFactory caTrustManagerFactory = TrustManagerFactory.getInstance( getX509Algorithm() );
        caTrustManagerFactory.init( loadTrustStore() );
        sslCtx.init( null, caTrustManagerFactory.getTrustManagers(), null );
        return sslCtx;
    }

    private static SSLContext buildMutualSSLContext()
        throws IOException, GeneralSecurityException
    {
        SSLContext sslCtx = SSLContext.getInstance( "TLS" );
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance( getX509Algorithm() );
        keyManagerFactory.init( loadKeyStore(), KS_PASSWORD.toCharArray() );
        TrustManagerFactory caTrustManagerFactory = TrustManagerFactory.getInstance( getX509Algorithm() );
        caTrustManagerFactory.init( loadTrustStore() );
        sslCtx.init( keyManagerFactory.getKeyManagers(), caTrustManagerFactory.getTrustManagers(), null );
        return sslCtx;
    }

    private static KeyStore loadTrustStore()
        throws IOException, GeneralSecurityException
    {
        KeyStore truststore = KeyStore.getInstance( "JCEKS" );
        try( InputStream stream = AbstractSecureJettyTest.class.getResourceAsStream( TRUSTSTORE_FILENAME ) )
        {
            truststore.load( stream, KS_PASSWORD.toCharArray() );
        }
        return truststore;
    }

    private static KeyStore loadKeyStore()
        throws IOException, GeneralSecurityException
    {
        KeyStore keystore = KeyStore.getInstance( "JCEKS" );
        try( InputStream stream = AbstractSecureJettyTest.class.getResourceAsStream( CLIENT_KEYSTORE_FILENAME ) )
        {
            keystore.load( stream, KS_PASSWORD.toCharArray() );
        }
        return keystore;
    }

    protected synchronized File getKeyStoreFile( String filename )
    {
        try
        {
            File file = new File( tmpDir.getRoot(), filename );
            if( file.exists() )
            {
                return file;
            }
            try( InputStream stream = AbstractSecureJettyTest.class.getResourceAsStream( filename ) )
            {
                Files.copy( stream, file.toPath() );
            }
            return file;
        }
        catch( IOException ex )
        {
            throw new UncheckedIOException( ex );
        }
    }
}
