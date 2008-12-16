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
import org.qi4j.api.query.grammar.AssociationReference;
import org.qi4j.api.query.grammar.PropertyReference;
import org.qi4j.runtime.query.grammar.impl.PropertyReferenceImpl;

/**
 * TODO Add JavaDoc
 */
final class PropertyReferenceProxy
    implements InvocationHandler
{

    private final PropertyReference propertyReference;

    /**
     * Constructor.
     *
     * @param accessor property accessor method
     */
    PropertyReferenceProxy( final Method accessor )
    {
        this( accessor, null );
    }

    /**
     * Constructor.
     *
     * @param accessor             property accessor method
     * @param traversedAssociation traversed association
     */
    PropertyReferenceProxy( final Method accessor,
                            final AssociationReference traversedAssociation )
    {
        propertyReference = new PropertyReferenceImpl( accessor, traversedAssociation );
    }

    public Object invoke( final Object proxy,
                          final Method method,
                          final Object[] args )
        throws Throwable
    {
        if( method.getDeclaringClass().equals( PropertyReference.class ) )
        {
            // TODO Shall we handle reflection exceptions here?
            return method.invoke( propertyReference, args );
        }
        if( "toString".equals( method.getName() ) )
        {
            return propertyReference.toString();
        }
        // TODO handle equals/hashcode?
        throw new UnsupportedOperationException( "Only property methods can be used. Not " + method.getName() + "()." );
    }

    @Override public String toString()
    {
        return propertyReference.toString();
    }

}