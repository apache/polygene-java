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
import java.lang.reflect.ParameterizedType;
import static java.lang.reflect.Proxy.newProxyInstance;
import java.lang.reflect.Type;
import org.qi4j.api.entity.association.Qualifier;
import org.qi4j.api.query.grammar.AssociationReference;
import org.qi4j.api.util.Classes;
import org.qi4j.runtime.query.grammar.impl.ManyAssociationReferenceImpl;

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
        ManyAssociationReferenceImpl associationReference =
            new ManyAssociationReferenceImpl( accessor, traversedAssociation, AssociationReference.ReferenceType.NONE );

        // Create any proxy
        ClassLoader loader = ManyAssociationReferenceProxy.class.getClassLoader();
        Type associationType = associationReference.associationType();

        if( Classes.getRawClass( associationType ).equals( Qualifier.class ) )
        {
            ParameterizedType associatedRoleType = (ParameterizedType) associationType;
            Class associatedType = (Class) associatedRoleType.getActualTypeArguments()[ 0 ];
            Object associated = newProxyInstance(
                this.getClass().getClassLoader(),
                new Class[]{ associatedType },
                new MixinTypeProxy( associatedType, associationReference.withQualifier( AssociationReference.ReferenceType.ASSOCIATION ) ) );

            Class roleType = (Class) associatedRoleType.getActualTypeArguments()[ 1 ];
            Object role = newProxyInstance(
                this.getClass().getClassLoader(),
                new Class[]{ roleType },
                new MixinTypeProxy( roleType, associationReference.withQualifier( AssociationReference.ReferenceType.ROLE ) ) );

            Qualifier associationQualifier = Qualifier.qualifier( associated, role );
            anyproxy = associationQualifier;
        }
        else
        {
            Class<?> associationClass = (Class<?>) associationType;
            MixinTypeProxy mixinTypeProxy = new MixinTypeProxy( associationClass, associationReference );
            anyproxy = newProxyInstance( loader, new Class[]{ associationClass }, mixinTypeProxy );
        }
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
