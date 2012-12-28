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
package org.qi4j.library.shiro.concerns;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.UnauthenticatedException;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.PermissionUtils;
import org.qi4j.api.common.AppliesTo;
import org.qi4j.api.common.Optional;
import org.qi4j.api.concern.ConcernOf;
import org.qi4j.api.injection.scope.Invocation;
import org.qi4j.library.shiro.Shiro;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AppliesTo( { RequiresAuthentication.class,
              RequiresGuest.class,
              RequiresPermissions.class,
              RequiresRoles.class,
              RequiresUser.class } )
public class SecurityConcern
        extends ConcernOf<InvocationHandler>
        implements InvocationHandler
{

    private static final Logger LOGGER = LoggerFactory.getLogger( Shiro.LOGGER_NAME );

    @Optional
    @Invocation
    private RequiresAuthentication requiresAuthentication;

    @Optional
    @Invocation
    private RequiresGuest requiresGuest;

    @Optional
    @Invocation
    private RequiresPermissions requiresPermissions;

    @Optional
    @Invocation
    private RequiresRoles requiresRoles;

    @Optional
    @Invocation
    private RequiresUser requiresUser;

    @Override
    public Object invoke( Object proxy, Method method, Object[] args )
            throws Throwable
    {
        Subject subject = SecurityUtils.getSubject();

        handleRequiresGuest( subject );
        handleRequiresUser( subject );
        handleRequiresAuthentication( subject );
        handleRequiresRoles( subject );
        handleRequiresPermissions( subject );

        return next.invoke( proxy, method, args );
    }

    private void handleRequiresGuest( Subject subject )
    {
        if ( requiresGuest != null ) {
            LOGGER.debug( "SecurityConcern::RequiresGuest" );
            if ( subject.getPrincipal() != null ) {
                throw new UnauthenticatedException(
                        "Attempting to perform a guest-only operation. The current Subject is "
                        + "not a guest (they have been authenticated or remembered from a previous login).  Access "
                        + "denied." );

            }
        } else {
            LOGGER.debug( "SecurityConcern::RequiresGuest: not concerned" );
        }
    }

    private void handleRequiresUser( Subject subject )
    {
        if ( requiresUser != null ) {
            LOGGER.debug( "SecurityConcern::RequiresUser" );
            if ( subject.getPrincipal() == null ) {
                throw new UnauthenticatedException(
                        "Attempting to perform a user-only operation. The current Subject is "
                        + "not a user (they haven't been authenticated or remembered from a previous login).  "
                        + "Access denied." );
            }
        } else {
            LOGGER.debug( "SecurityConcern::RequiresUser: not concerned" );
        }
    }

    private void handleRequiresAuthentication( Subject subject )
    {
        if ( requiresAuthentication != null ) {
            LOGGER.debug( "SecurityConcern::RequiresAuthentication" );
            if ( !subject.isAuthenticated() ) {
                throw new UnauthenticatedException( "The current Subject is not authenticated.  Access denied." );
            }
        } else {
            LOGGER.debug( "SecurityConcern::RequiresAuthentication: not concerned" );
        }
    }

    private void handleRequiresRoles( Subject subject )
    {
        if ( requiresRoles != null ) {
            LOGGER.debug( "SecurityConcern::RequiresRoles" );
            String roleId = requiresRoles.value();
            String[] roles = roleId.split( "," );
            if ( roles.length == 1 ) {
                if ( !subject.hasRole( roles[ 0] ) ) {
                    String msg = "Calling Subject does not have required role [" + roleId + "].  "
                                 + "MethodInvocation denied.";
                    throw new UnauthorizedException( msg );
                }
            } else {
                Set<String> rolesSet = new LinkedHashSet<String>( Arrays.asList( roles ) );
                if ( !subject.hasAllRoles( rolesSet ) ) {
                    String msg = "Calling Subject does not have required roles [" + roleId + "].  "
                                 + "MethodInvocation denied.";
                    throw new UnauthorizedException( msg );
                }
            }
        } else {
            LOGGER.debug( "SecurityConcern::RequiresRoles: not concerned" );
        }

    }

    private void handleRequiresPermissions( Subject subject )
    {
        if ( requiresPermissions != null ) {
            LOGGER.debug( "SecurityConcern::RequiresPermissions" );
            String permsString = requiresPermissions.value();
            Set<String> permissions = PermissionUtils.toPermissionStrings( permsString );
            if ( permissions.size() == 1 ) {
                if ( !subject.isPermitted( permissions.iterator().next() ) ) {
                    String msg = "Calling Subject does not have required permission [" + permsString + "].  "
                                 + "Method invocation denied.";
                    throw new UnauthorizedException( msg );
                }
            } else {
                String[] permStrings = new String[ permissions.size() ];
                permStrings = permissions.toArray( permStrings );
                if ( !subject.isPermittedAll( permStrings ) ) {
                    String msg = "Calling Subject does not have required permissions [" + permsString + "].  "
                                 + "Method invocation denied.";
                    throw new UnauthorizedException( msg );
                }

            }
        } else {
            LOGGER.debug( "SecurityConcern::RequiresPermissions: not concerned" );
        }

    }

}
