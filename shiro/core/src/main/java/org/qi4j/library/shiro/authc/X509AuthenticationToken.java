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
package org.qi4j.library.shiro.authc;

import java.security.NoSuchProviderException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.HostAuthenticationToken;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.x509.NoSuchStoreException;
import org.bouncycastle.x509.X509CertStoreSelector;
import org.bouncycastle.x509.X509CollectionStoreParameters;
import org.bouncycastle.x509.X509Store;

public class X509AuthenticationToken
        implements AuthenticationToken, HostAuthenticationToken
{

    private static final long serialVersionUID = 1L;
    private final X509Certificate[] clientX509CertChain;
    private final String host;

    public X509AuthenticationToken( X509Certificate[] clientX509CertChain, String host )
    {
        this.clientX509CertChain = clientX509CertChain;
        this.host = host;
    }

    public X509Certificate getClientX509Certificate()
    {
        if ( clientX509CertChain == null || clientX509CertChain.length < 1 ) {
            return null;
        }
        return clientX509CertChain[0];
    }

    public X509CertStoreSelector getClientX509CertSelector()
    {
        X509CertStoreSelector certSelector = new X509CertStoreSelector();
        certSelector.setCertificate( getClientX509Certificate() );
        return certSelector;
    }

    public X509Store getClientCertChainStore()
    {
        try {
            X509CollectionStoreParameters params = new X509CollectionStoreParameters( Arrays.asList( clientX509CertChain ) );
            return X509Store.getInstance( "CERTIFICATE/COLLECTION", params, BouncyCastleProvider.PROVIDER_NAME );
        } catch ( NoSuchStoreException ex ) {
            return null;
        } catch ( NoSuchProviderException ex ) {
            return null;
        }
    }

    public Object getPrincipal()
    {
        X509Certificate clientCert = getClientX509Certificate();
        if ( clientCert == null ) {
            return null;
        }
        return clientCert.getSubjectX500Principal();
    }

    @SuppressWarnings( "ReturnOfCollectionOrArrayField" )
    public Object getCredentials()
    {
        return clientX509CertChain;
    }

    public String getHost()
    {
        return host;
    }

}
