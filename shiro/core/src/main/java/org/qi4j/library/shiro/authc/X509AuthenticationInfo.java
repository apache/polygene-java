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
