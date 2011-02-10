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
package org.qi4j.library.shiro.realms;

import java.security.cert.X509Certificate;
import java.util.Set;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.PrincipalCollection;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.library.shiro.Shiro;
import org.qi4j.library.shiro.authc.X509AuthenticationInfo;
import org.qi4j.library.shiro.authc.X509AuthenticationToken;
import org.qi4j.library.shiro.authc.X509CredentialsPKIXPathMatcher;
import org.qi4j.library.shiro.domain.permissions.RoleAssignee;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractX509Qi4jRealm
        extends AbstractQi4jRealm
{

    private static final Logger LOGGER = LoggerFactory.getLogger( Shiro.LOGGER_NAME );

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
