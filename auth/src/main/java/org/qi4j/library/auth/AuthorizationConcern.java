/*
 * Copyright 2006 Niclas Hedhman.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.qi4j.library.auth;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.util.Date;
import javax.security.auth.Subject;
import org.qi4j.composite.AppliesTo;
import org.qi4j.composite.CompositeBuilder;
import org.qi4j.composite.CompositeBuilderFactory;
import org.qi4j.composite.PropertyValue;
import static org.qi4j.composite.PropertyValue.property;
import org.qi4j.composite.scope.ConcernFor;
import org.qi4j.composite.scope.Invocation;
import org.qi4j.composite.scope.Service;
import org.qi4j.composite.scope.Structure;
import org.qi4j.composite.scope.ThisCompositeAs;

@AppliesTo( RequiresPermission.class )
public class AuthorizationConcern
    implements InvocationHandler
{
    @Invocation RequiresPermission requiresPermission;

    @Service AuthorizationService authorizor;
    @ThisCompositeAs HasRoleAssignments roleAssignments;
    @Structure CompositeBuilderFactory cbf;
    @ConcernFor InvocationHandler next;

    public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable
    {
        CompositeBuilder<NamedPermissionComposite> cb = cbf.newCompositeBuilder( NamedPermissionComposite.class );
        cb.propertiesOfComposite().name().set( requiresPermission.value() );
        Permission permission = cb.newInstance();

        Subject subject = Subject.getSubject( AccessController.getContext() );
        UserComposite user = subject.getPrincipals( UserComposite.class ).iterator().next();

        CompositeBuilder<AuthorizationContextComposite> authBuilder = cbf.newCompositeBuilder( AuthorizationContextComposite.class );
        AuthorizationContext context = PropertyValue.name( AuthorizationContext.class );
        authBuilder.properties( AuthorizationContext.class, property( context.user(), user ), property( context.time(), new Date() ), property( context.authenticationMethod(), "BASIC" ) );
        context = authBuilder.newInstance();

        if( !authorizor.hasPermission( permission, roleAssignments, context ) )
        {
            throw new SecurityException( "User " + user + " does not have the required permission " + requiresPermission.value() );
        }

        return next.invoke( proxy, method, args );
    }
}
