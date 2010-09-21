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
package org.qi4j.library.shiro.concerns;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.UnauthorizedException;
import org.qi4j.api.common.AppliesTo;
import org.qi4j.api.concern.ConcernOf;
import org.qi4j.api.injection.scope.Invocation;

/**
 * @deprecated Use {@link SecurityConcern} instead once QI-241 is resolved.
 */
@Deprecated
@AppliesTo( RequiresRoles.class )
public class RequiresRolesConcern
        extends ConcernOf<InvocationHandler>
        implements InvocationHandler
{

    @Invocation
    private RequiresRoles requiresRoles;

    public Object invoke( Object proxy, Method method, Object[] args )
            throws Throwable
    {
        String roleId = requiresRoles.value();
        String[] roles = roleId.split( "," );
        if ( roles.length == 1 ) {
            if ( !SecurityUtils.getSubject().hasRole( roles[0] ) ) {
                String msg = "Calling Subject does not have required role [" + roleId + "].  "
                        + "MethodInvocation denied.";
                throw new UnauthorizedException( msg );
            }
        } else {
            Set<String> rolesSet = new LinkedHashSet<String>( Arrays.asList( roles ) );
            if ( !SecurityUtils.getSubject().hasAllRoles( rolesSet ) ) {
                String msg = "Calling Subject does not have required roles [" + roleId + "].  "
                        + "MethodInvocation denied.";
                throw new UnauthorizedException( msg );
            }
        }
        return next.invoke( proxy, method, args );
    }

}
