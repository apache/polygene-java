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

    private static final Logger LOGGER = LoggerFactory.getLogger( AbstractQi4jRealm.class );
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
