/*
 * Copyright (c) 2010 Paul Merlin <paul@nosphere.org>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.qi4j.library.shiro.tests.x509;

import java.io.IOException;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import org.bouncycastle.openssl.PEMReader;
import org.qi4j.library.shiro.crypto.CryptoException;

/**
 * User, service and authorities certificates are valid til 2020/03, should be enough for qi4j unit tests : )
 */
public class X509FixturesData
{

    private static final String TEST_USER_PEM = "test-user.pem";
    private static final String TEST_USER_KEYSTORE = "test-user.p12";
    private static final String TEST_SERVICE_KEYSTORE = "test-service.p12";
    private static final String USERS_CA_PEM = "users-ca.pem";
    private static final String USERS_CA_JKS = "users-ca.jks";
    private static final String SERVICES_CA_PEM = "services-ca.pem";
    private static final String SERVICES_CA_JKS = "services-ca.jks";
    private static final String ROOT_CA_PEM = "root-ca.pem";
    private static final String ROOT_CA_JKS = "root-ca.jks";
    private static final String CA_BUNDLE_JKS = "ca-bundle.jks";
    public static final char[] PASSWORD = "changeit".toCharArray();

    private X509FixturesData()
    {
    }

    public static SSLContext clientSSLContext()
    {
        return createSSLContext( clientKeyStore(), PASSWORD, serviceCATrustStore() );
    }

    public static SSLContext serverSSLContext()
    {
        return createSSLContext( serviceKeyStore(), PASSWORD, clientCATrustStore() );
    }

    public static KeyStore clientKeyStore()
    {
        return loadKeyStore( "PKCS12", TEST_USER_KEYSTORE, PASSWORD );
    }

    public static KeyStore serviceKeyStore()
    {
        return loadKeyStore( "PKCS12", TEST_SERVICE_KEYSTORE, PASSWORD );
    }

    public static KeyStore rootCATrustStore()
    {
        return loadKeyStore( "JKS", ROOT_CA_JKS, PASSWORD );
    }

    public static KeyStore clientCATrustStore()
    {
        return loadKeyStore( "JKS", USERS_CA_JKS, PASSWORD );
    }

    public static KeyStore serviceCATrustStore()
    {
        return loadKeyStore( "JKS", SERVICES_CA_JKS, PASSWORD );
    }

    public static KeyStore caBundleTrustStore()
    {
        return loadKeyStore( "JKS", CA_BUNDLE_JKS, PASSWORD );
    }

    public static X509Certificate testUserCertificate()
    {
        return readPem( TEST_USER_PEM );
    }

    public static X509Certificate usersCertificateAuthority()
    {
        return readPem( USERS_CA_PEM );
    }

    public static X509Certificate servicesCertificateAuthority()
    {
        return readPem( SERVICES_CA_PEM );
    }

    public static X509Certificate rootCertificateAuthority()
    {
        return readPem( ROOT_CA_PEM );
    }

    private static KeyStore loadKeyStore( String type, String resource, char[] password )
    {
        try {
            KeyStore ks = KeyStore.getInstance( type );
            ks.load( X509FixturesData.class.getResourceAsStream( resource ), password );
            return ks;
        } catch ( GeneralSecurityException ex ) {
            throw new CryptoException( "Unable to create client SSLContext", ex );
        } catch ( IOException ex ) {
            throw new CryptoException( "Unable to create client SSLContext", ex );
        }
    }

    public static SSLContext createSSLContext( KeyStore ks, char[] password, KeyStore ts )
    {
        try {
            KeyManagerFactory kmf = KeyManagerFactory.getInstance( "SunX509" );
            kmf.init( ks, password );

            TrustManagerFactory tmf = TrustManagerFactory.getInstance( "SunX509" );
            tmf.init( ts );

            KeyManager[] keyManagers = kmf.getKeyManagers();
            TrustManager[] trustManagers = tmf.getTrustManagers();

            SSLContext sslCtx = SSLContext.getInstance( "TLS" );
            sslCtx.init( keyManagers, trustManagers, null );
            return sslCtx;
        } catch ( GeneralSecurityException ex ) {
            throw new CryptoException( "Unable to create client SSLContext", ex );
        }
    }

    private static X509Certificate readPem( String resource )
    {
        try {
            return ( X509Certificate ) new PEMReader( new InputStreamReader( X509Fixtures.class.getResourceAsStream( resource ) ) ).readObject();
        } catch ( IOException ex ) {
            throw new CryptoException( "Unable to read the test user X509Certificate PEM", ex );
        }
    }

}
