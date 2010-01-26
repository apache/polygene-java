/*
 * Copyright 2007 Niclas Hedhman.
 * Copyright 2008 Alin Dreghiciu.
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
package org.qi4j.runtime.query.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import org.qi4j.api.query.QueryExpressionException;
import org.qi4j.api.query.grammar.AssociationReference;
import org.qi4j.runtime.query.grammar.impl.AssociationReferenceImpl;

import static java.lang.reflect.Proxy.*;

/**
 * JAVADOC Add JavaDoc
 */
public final class AssociationReferenceProxy
    implements InvocationHandler
{

    /**
     * Traversed association.
     */
    private final AssociationReferenceImpl associationReference;

    /**
     * Constructor.
     *
     * @param accessor association accessor method
     */
    AssociationReferenceProxy( final Method accessor )
    {
        this( accessor, null );
    }

    /**
     * Constructor.
     *
     * @param accessor             association accessor method
     * @param traversedAssociation traversed association
     */
    AssociationReferenceProxy( final Method accessor,
                               final AssociationReference traversedAssociation
    )
    {
        associationReference = new AssociationReferenceImpl( accessor, traversedAssociation );
    }

    public Object invoke( final Object proxy,
                          final Method method,
                          final Object[] args
    )
        throws Throwable
    {
        if( method.getDeclaringClass().equals( AssociationReference.class ) )
        {
            // TODO Shall we handle reflection exceptions here?
            return method.invoke( associationReference, args );
        }
        if( args == null && "get".equals( method.getName() ) )
        {
            Type associationType = associationReference.associationType();
            Class<?> associationClass = (Class<?>) associationType;
            return newProxyInstance(
                this.getClass().getClassLoader(),
                new Class[]{ associationClass },
                new MixinTypeProxy( associationClass, associationReference )
            );
        }
        if( "toString".equals( method.getName() ) )
        {
            return associationReference.toString();
        }
        // TODO handle equals/hashcode?
        throw new QueryExpressionException( "Only association methods can be used" );
    }

    @Override
    public String toString()
    {
        return associationReference.toString();
    }
}
