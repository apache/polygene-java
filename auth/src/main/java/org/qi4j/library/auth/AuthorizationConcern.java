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
import org.qi4j.property.ImmutableProperty;
import org.qi4j.property.Property;
import org.qi4j.spi.property.PropertyInstance;

@AppliesTo( RequiresPermission.class )
public class AuthorizationConcern
    implements InvocationHandler
{
    @Invocation RequiresPermission requiresPermission;

    @Service AuthorizationService authorizor;
    @ThisCompositeAs ProtectedResource roleAssignments;
    @Structure CompositeBuilderFactory cbf;
    @ConcernFor InvocationHandler next;

    public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable
    {
        CompositeBuilder<NamedPermissionComposite> cb = cbf.newCompositeBuilder( NamedPermissionComposite.class );
        cb.propertiesOfComposite().name().set( requiresPermission.value() );
        Permission permission = cb.newInstance();

        Subject subject = Subject.getSubject( AccessController.getContext() );
        UserComposite user = subject.getPrincipals( UserPrincipal.class ).iterator().next().getUser();

        CompositeBuilder<AuthorizationContextComposite> authBuilder = cbf.newCompositeBuilder( AuthorizationContextComposite.class );
        AuthorizationContext context = PropertyValue.name( AuthorizationContext.class );
        AuthorizationContext authorizationContext = authBuilder.propertiesFor( AuthorizationContext.class );
        Object userObject = authorizationContext.user();
        System.out.println( userObject instanceof Property );
        Property<UserComposite> compositeImmutableProperty = (Property<UserComposite>) userObject;
        compositeImmutableProperty.set( user );
        authBuilder.propertiesFor( AuthorizationContext.class ).time().set( new Date() );
        authBuilder.propertiesFor( AuthorizationContext.class).authenticationMethod().set( new BasicAuthenticationMethod() );
        context = authBuilder.newInstance();

        if( !authorizor.hasPermission( permission, roleAssignments, context ) )
        {
            throw new SecurityException( "User " + user + " does not have the required permission " + requiresPermission.value() );
        }

        return next.invoke( proxy, method, args );
    }
}
