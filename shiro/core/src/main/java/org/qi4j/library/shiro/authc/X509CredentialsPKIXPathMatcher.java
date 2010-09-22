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

import java.security.GeneralSecurityException;
import java.security.cert.CertPathBuilder;
import java.security.cert.CertPathBuilderResult;
import java.security.cert.X509Certificate;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.x509.CertPathReviewerException;
import org.bouncycastle.x509.ExtendedPKIXBuilderParameters;
import org.bouncycastle.x509.PKIXCertPathReviewer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * See http://java.sun.com/javase/6/docs/technotes/guides/security/certpath/CertPathProgGuide.html for reference
 * and http://stackoverflow.com/questions/2457795/x-509-certificate-validation-with-java-and-bouncycastle/2458343
 * for a quick example using Sun API.
 * 
 * This implementation use the BouncyCastle PKIX API as it behave much better and will make CRLs support easily
 * implemented when needed.
 */
public class X509CredentialsPKIXPathMatcher
        implements CredentialsMatcher
{

    private static final Logger LOGGER = LoggerFactory.getLogger( X509CredentialsPKIXPathMatcher.class );

    public boolean doCredentialsMatch( AuthenticationToken token, AuthenticationInfo info )
    {
        try {

            X509AuthenticationToken x509AuthToken = ( X509AuthenticationToken ) token;
            X509AuthenticationInfo x509AuthInfo = ( X509AuthenticationInfo ) info;

            ExtendedPKIXBuilderParameters params = new ExtendedPKIXBuilderParameters( x509AuthInfo.getGrantedTrustAnchors(),
                                                                                      x509AuthToken.getClientX509CertSelector() );
            params.addStore( x509AuthToken.getClientCertChainStore() );
            params.setRevocationEnabled( false );

            CertPathBuilder pathBuilder = CertPathBuilder.getInstance( "PKIX", BouncyCastleProvider.PROVIDER_NAME );
            CertPathBuilderResult result = pathBuilder.build( params );

            if ( LOGGER.isDebugEnabled() ) {
                PKIXCertPathReviewer reviewer = new PKIXCertPathReviewer( result.getCertPath(), params );
                String certPathEnd = ( ( X509Certificate ) reviewer.getCertPath().getCertificates().get( reviewer.getCertPathSize() - 1 ) ).getSubjectX500Principal().getName();
                LOGGER.debug( "A valid ({}) certification path (length: {}) was found for the following certificate: '{}' ending on: '{}'",
                              new Object[]{ reviewer.isValidCertPath(),
                                            reviewer.getCertPathSize(),
                                            x509AuthToken.getClientX509Certificate().getSubjectX500Principal().getName(),
                                            certPathEnd } );
            }

            return true;

        } catch ( GeneralSecurityException ex ) {
            LOGGER.trace( "Unable to do credentials matching", ex );
            return false;
        } catch ( CertPathReviewerException ex ) {
            LOGGER.trace( "Unable to do credentials matching", ex );
            return false;
        }
    }

}
