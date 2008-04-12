/*
 * Copyright (c) 2007-2008, Niclas Hedhman. All Rights Reserved.
 * Copyright (c) 2007-2008, Rickard Öberg. All Rights Reserved.
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
import org.qi4j.composite.ConcernOf;
import org.qi4j.composite.scope.Invocation;
import org.qi4j.composite.scope.Service;
import org.qi4j.composite.scope.Structure;
import org.qi4j.composite.scope.ThisCompositeAs;

@AppliesTo( RequiresPermission.class )
public class AuthorizationConcern extends ConcernOf<InvocationHandler>
    implements InvocationHandler
{
    @Invocation private RequiresPermission requiresPermission;

    @Service private Authorization authorizor;
    @ThisCompositeAs private ProtectedResource roleAssignments;
    @Structure private CompositeBuilderFactory cbf;

    public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable
    {
        CompositeBuilder<NamedPermission> cb = cbf.newCompositeBuilder( NamedPermission.class );
        cb.stateOfComposite().name().set( requiresPermission.value() );
        Permission permission = cb.newInstance();

        Subject subject = Subject.getSubject( AccessController.getContext() );
        User user = subject.getPrincipals( UserPrincipal.class ).iterator().next().getUser();

        CompositeBuilder<AuthorizationContext> authBuilder = cbf.newCompositeBuilder( AuthorizationContext.class );
        AuthorizationContext authProps = authBuilder.stateFor( AuthorizationContext.class );
        authProps.user().set( user );
        authProps.time().set( new Date() );
        authProps.authenticationMethod().set( new BasicAuthenticationMethod() );
        AuthorizationContext context = authBuilder.newInstance();
        if( !authorizor.hasPermission( permission, roleAssignments, context ) )
        {
            throw new SecurityException( "User " + user + " does not have the required permission " + requiresPermission.value() );
        }
        return next.invoke( proxy, method, args );
    }
}
