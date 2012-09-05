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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManagerFactory;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 * Base class for SecureJettyMixin tests.
 *
 * Use HttpClient in order to easily use different {@link SSLContext}s between server and client.
 */
public abstract class AbstractSecureJettyTest
        extends AbstractJettyTest
{

    private static final String HTTPS = "https";
    protected static final int HTTPS_PORT = 8441;
    protected static final String KS_PASSWORD = "changeit";
    protected static final String CLIENT_KEYSTORE_PATH = "src/test/resources/org/qi4j/library/http/qi4j-lib-http-unittests-client-cert.jceks";
    protected static final File CLIENT_KEYSTORE_FILE = new File( CLIENT_KEYSTORE_PATH );
    protected static final String SERVER_KEYSTORE_PATH = "src/test/resources/org/qi4j/library/http/qi4j-lib-http-unittests-server-cert.jceks";
    protected static final File SERVER_KEYSTORE_FILE = new File( SERVER_KEYSTORE_PATH );
    protected static final String TRUSTSTORE_PATH = "src/test/resources/org/qi4j/library/http/qi4j-lib-http-unittests-ca.jceks";
    protected static final File TRUSTSTORE_FILE = new File( TRUSTSTORE_PATH );
    // These two clients use a HostnameVerifier that don't do any check, don't do this in production code
    protected HttpClient trustHttpClient;
    protected HttpClient mutualHttpClient;

    @Before
    public void beforeSecure()
            throws GeneralSecurityException, IOException
    {
        // Trust HTTP Client
        KeyStore truststore = KeyStore.getInstance( "JCEKS" );
        truststore.load( new FileInputStream( TRUSTSTORE_FILE ), KS_PASSWORD.toCharArray() );

        AllowAllHostnameVerifier verifier = new AllowAllHostnameVerifier();

        DefaultHttpClient trustClient = new DefaultHttpClient();
        SSLSocketFactory trustSslFactory = new SSLSocketFactory( truststore );
        trustSslFactory.setHostnameVerifier( verifier );
        SchemeRegistry trustSchemeRegistry = trustClient.getConnectionManager().getSchemeRegistry();
        trustSchemeRegistry.unregister( HTTPS );
        trustSchemeRegistry.register( new Scheme( HTTPS, HTTPS_PORT, trustSslFactory ) );
        trustHttpClient = trustClient;

        // Mutual HTTP Client
        KeyStore keystore = KeyStore.getInstance( "JCEKS" );
        keystore.load( new FileInputStream( CLIENT_KEYSTORE_FILE ), KS_PASSWORD.toCharArray() );

        DefaultHttpClient mutualClient = new DefaultHttpClient();
        SSLSocketFactory mutualSslFactory = new SSLSocketFactory( keystore, KS_PASSWORD, truststore );
        mutualSslFactory.setHostnameVerifier( verifier );
        SchemeRegistry mutualSchemeRegistry = mutualClient.getConnectionManager().getSchemeRegistry();
        mutualSchemeRegistry.unregister( HTTPS );
        mutualSchemeRegistry.register( new Scheme( HTTPS, HTTPS_PORT, mutualSslFactory ) );
        mutualHttpClient = mutualClient;
    }

    private static HostnameVerifier defaultHostnameVerifier;
    private static javax.net.ssl.SSLSocketFactory defaultSSLSocketFactory;

    @BeforeClass
    public static void beforeSecureClass()
            throws IOException, GeneralSecurityException
    {
        defaultHostnameVerifier = HttpsURLConnection.getDefaultHostnameVerifier();
        defaultSSLSocketFactory = HttpsURLConnection.getDefaultSSLSocketFactory();
        HttpsURLConnection.setDefaultHostnameVerifier( new HostnameVerifier()
        {

            public boolean verify( String string, SSLSession ssls )
            {
                return true;
            }

        } );
        KeyStore truststore = KeyStore.getInstance( "JCEKS" );
        truststore.load( new FileInputStream( TRUSTSTORE_FILE ), KS_PASSWORD.toCharArray() );
        SSLContext sslCtx = SSLContext.getInstance( "TLS" );
        TrustManagerFactory caTrustManagerFactory = TrustManagerFactory.getInstance( getX509Algorithm() );
        caTrustManagerFactory.init( truststore );
        sslCtx.init( null, caTrustManagerFactory.getTrustManagers(), null );
        HttpsURLConnection.setDefaultSSLSocketFactory( sslCtx.getSocketFactory() );
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

}
