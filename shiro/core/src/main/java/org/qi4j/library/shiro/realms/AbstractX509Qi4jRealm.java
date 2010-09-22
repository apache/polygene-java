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
package org.qi4j.library.shiro.realms;

import java.security.cert.X509Certificate;
import java.util.Set;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.PrincipalCollection;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.library.shiro.authc.X509AuthenticationInfo;
import org.qi4j.library.shiro.authc.X509AuthenticationToken;
import org.qi4j.library.shiro.authc.X509CredentialsPKIXPathMatcher;
import org.qi4j.library.shiro.domain.permissions.RoleAssignee;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractX509Qi4jRealm
        extends AbstractQi4jRealm
{

    private static final Logger LOGGER = LoggerFactory.getLogger( AbstractX509Qi4jRealm.class );

    public AbstractX509Qi4jRealm()
    {
        super();
        setAuthenticationTokenClass( X509AuthenticationToken.class );
        setCredentialsMatcher( new X509CredentialsPKIXPathMatcher() );
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo( AuthenticationToken token )
            throws AuthenticationException
    {
        LOGGER.debug( "Will doGetAuthenticationInfo" );
        try {
            UnitOfWork uow = uowf.newUnitOfWork();

            X509Certificate clientCert = ( ( X509AuthenticationToken ) token ).getClientX509Certificate();
            Set<X509Certificate> grantedIssuers = getGrantedIssuers( clientCert );

            if ( grantedIssuers == null || grantedIssuers.isEmpty() ) {
                return null;
            }

            AuthenticationInfo authc = new X509AuthenticationInfo( clientCert, grantedIssuers, getName() );

            uow.complete();
            return authc;

        } catch ( UnitOfWorkCompletionException ex ) {
            LOGGER.error( "Unable to get AuthenticationInfo", ex );
            return null;
        }
    }

    protected abstract Set<X509Certificate> getGrantedIssuers( X509Certificate userCertificate );

    @Override
    protected RoleAssignee getRoleAssignee( PrincipalCollection principals )
    {
        return getRoleAssignee( ( X509Certificate ) principals.fromRealm( getName() ).iterator().next() );
    }

    protected abstract RoleAssignee getRoleAssignee( X509Certificate userCertificate );

}
