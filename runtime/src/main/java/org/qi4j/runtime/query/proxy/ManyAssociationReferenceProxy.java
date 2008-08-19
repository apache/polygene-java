/*  Copyright 2008 Edward Yakop.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.runtime.query.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import static java.lang.reflect.Proxy.newProxyInstance;
import org.qi4j.query.grammar.AssociationReference;
import org.qi4j.runtime.query.grammar.impl.AssociationReferenceImpl;

/**
 * TODO: Add javadoc
 *
 * @author edward.yakop@gmail.com
 */
public class ManyAssociationReferenceProxy
    implements InvocationHandler
{
    private Object anyproxy;

    /**
     * Constructor.
     *
     * @param accessor association accessor method
     */
    ManyAssociationReferenceProxy( final Method accessor )
    {
        this( accessor, null );
    }

    /**
     * Constructor.
     *
     * @param accessor             association accessor method
     * @param traversedAssociation traversed association
     */
    ManyAssociationReferenceProxy( final Method accessor,
                                   final AssociationReference traversedAssociation )
    {
        // TODO: This doesn't sounds right
        AssociationReference associationReference = new AssociationReferenceImpl( accessor, traversedAssociation );

        // Create any proxy
        ClassLoader loader = ManyAssociationReferenceProxy.class.getClassLoader();
        Class<?> associationType = associationReference.associationType();
        MixinTypeProxy mixinTypeProxy = new MixinTypeProxy( associationType, associationReference );
        anyproxy = newProxyInstance( loader, new Class[]{ associationType }, mixinTypeProxy );
    }

    public Object invoke( Object proxy, Method method, Object[] args )
        throws Throwable
    {
        throw new UnsupportedOperationException( "No methods can be used" );
    }

    @SuppressWarnings( "unchecked" )
    public Object getAnyProxy()
    {
        return anyproxy;
    }
}
