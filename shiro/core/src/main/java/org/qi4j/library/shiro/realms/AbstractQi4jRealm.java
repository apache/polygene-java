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

import java.util.LinkedHashSet;
import java.util.Set;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.library.shiro.Shiro;
import org.qi4j.library.shiro.domain.permissions.Permission;
import org.qi4j.library.shiro.domain.permissions.Role;
import org.qi4j.library.shiro.domain.permissions.RoleAssignee;
import org.qi4j.library.shiro.domain.permissions.RoleAssignment;
import org.qi4j.library.shiro.bootstrap.RealmActivator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractQi4jRealm
        extends AuthorizingRealm
        implements RealmActivator
{

    private static final Logger LOGGER = LoggerFactory.getLogger( Shiro.LOGGER_NAME );
    @Structure
    protected UnitOfWorkFactory uowf;

    public AbstractQi4jRealm()
    {
        super();
        setCachingEnabled( false );
    }

    public void activateRealm()
    {
        SecurityUtils.setSecurityManager( new DefaultSecurityManager( this ) );
    }

    @Override
    protected final AuthorizationInfo doGetAuthorizationInfo( PrincipalCollection principals )
    {
        try {

            UnitOfWork uow = uowf.newUnitOfWork();

            RoleAssignee roleAssignee = getRoleAssignee( principals );
            if ( roleAssignee == null ) {
                return null;
            }

            Set<String> roleNames = new LinkedHashSet<String>();
            Set<String> permissions = new LinkedHashSet<String>();
            for ( RoleAssignment eachAssignment : roleAssignee.roleAssignments() ) {
                Role eachRole = eachAssignment.role().get();
                roleNames.add( eachRole.name().get() );
                for ( Permission eachPermission : eachRole.permissions() ) {
                    permissions.add( eachPermission.string().get() );
                }
            }

            // Building AuthorizationInfo
            SimpleAuthorizationInfo authz = new SimpleAuthorizationInfo( roleNames );
            authz.setStringPermissions( permissions );

            uow.complete();
            return authz;

        } catch ( UnitOfWorkCompletionException ex ) {
            LOGGER.error( "Unable to get AuthorizationInfo", ex );
            return null;
        }
    }

    protected abstract RoleAssignee getRoleAssignee( PrincipalCollection principals );

}
