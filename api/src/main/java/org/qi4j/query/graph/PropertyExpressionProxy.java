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
package org.qi4j.query.graph;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * TODO Add JavaDoc
 *
 * @author Alin Dreghiciu
 * @since March 26, 2008
 */
class PropertyExpressionProxy
    implements InvocationHandler
{

    private final PropertyExpression property;

    /**
     * Constructor.
     *
     * @param propertyMethod method that acts as property
     */
    PropertyExpressionProxy( final Method propertyMethod )
    {
        property = new PropertyExpressionImpl( propertyMethod );
    }

    public Object invoke( final Object proxy,
                          final Method method,
                          final Object[] args )
        throws Throwable
    {
        if( method.getDeclaringClass().equals( PropertyExpression.class ) )
        {
            // TODO Shall we handle reflection exceptions here?
            return method.invoke( property, args );
        }
        if( "toString".equals( method.getName() ) )
        {
            return property.toString();
        }
        // TODO handle toString/equals/hashcode
        throw new UnsupportedOperationException( "Only property methods can be used for queries" );
    }

    @Override public String toString()
    {
        return property.toString();
    }

}