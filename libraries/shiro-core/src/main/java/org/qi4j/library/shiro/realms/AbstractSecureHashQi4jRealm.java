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

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.PrincipalCollection;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.library.shiro.Shiro;
import org.qi4j.library.shiro.authc.SecureHashAuthenticationInfo;
import org.qi4j.library.shiro.authc.SecureHashCredentialsMatcher;
import org.qi4j.library.shiro.domain.permissions.RoleAssignee;
import org.qi4j.library.shiro.domain.securehash.SecureHashSecurable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractSecureHashQi4jRealm
        extends AbstractQi4jRealm
{

    private static final Logger LOGGER = LoggerFactory.getLogger( Shiro.LOGGER_NAME );

    public AbstractSecureHashQi4jRealm()
    {
        super();
        setCredentialsMatcher( new SecureHashCredentialsMatcher() );
    }

    @Override
    protected final AuthenticationInfo doGetAuthenticationInfo( AuthenticationToken token )
    {
        try {

            UnitOfWork uow = uowf.newUnitOfWork();

            String username = ( ( UsernamePasswordToken ) token ).getUsername();
            SecureHashSecurable secured = getSecureHashSecurable( username );
            if ( secured == null ) {
                return null;
            }

            AuthenticationInfo authc = new SecureHashAuthenticationInfo( username, secured.secureHash().get(), getName() );

            uow.complete();
            return authc;

        } catch ( UnitOfWorkCompletionException ex ) {
            LOGGER.error( "Unable to get AuthenticationInfo", ex );
            return null;
        }
    }

    protected abstract SecureHashSecurable getSecureHashSecurable( String username );

    @Override
    protected final RoleAssignee getRoleAssignee( PrincipalCollection principals )
    {
        return getRoleAssignee( ( String ) principals.fromRealm( getName() ).iterator().next() );
    }

    protected abstract RoleAssignee getRoleAssignee( String username );

}
