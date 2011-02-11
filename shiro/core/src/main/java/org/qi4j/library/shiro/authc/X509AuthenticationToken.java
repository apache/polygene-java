/*
 * Copyright (c) 2010, Paul Merlin. All Rights Reserved.
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
