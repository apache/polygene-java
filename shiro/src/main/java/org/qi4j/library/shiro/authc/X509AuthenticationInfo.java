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

import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.Set;
import org.apache.shiro.authc.SimpleAuthenticationInfo;

public class X509AuthenticationInfo
        extends SimpleAuthenticationInfo
{

    private static final long serialVersionUID = 1L;
    private final Set<X509Certificate> grantedIssuers;

    public X509AuthenticationInfo( X509Certificate clientCert, Set<X509Certificate> grantedIssuers, String realmName )
    {
        super( clientCert, null, realmName );
        this.grantedIssuers = grantedIssuers;
    }

    public Set<TrustAnchor> getGrantedTrustAnchors()
    {
        Set<TrustAnchor> trustAnchors = new HashSet<TrustAnchor>();
        for ( X509Certificate eachCert : grantedIssuers ) {
            trustAnchors.add( new TrustAnchor( eachCert, eachCert.getExtensionValue( "2.5.29.30" ) ) ); // NamedConstraints
        }
        return trustAnchors;

    }

}
