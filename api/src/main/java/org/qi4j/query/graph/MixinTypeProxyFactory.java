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
import java.lang.reflect.Proxy;
import org.qi4j.association.Association;
import org.qi4j.property.Property;

/**
 * TODO Add JavaDoc
 *
 * @author Alin Dreghiciu
 * @since March 25, 2008
 */
public class MixinTypeProxyFactory
    implements InvocationHandler
{

    /**
     * Traversed association.
     */
    private final AssociationExpression traversed;

    /**
     * Constructor.
     */
    public MixinTypeProxyFactory()
    {
        this( null );
    }

    /**
     * Constructor.
     *
     * @param traversed traversed association
     */
    public MixinTypeProxyFactory( final AssociationExpression traversed )
    {
        this.traversed = traversed;
    }

    public Object invoke( final Object proxy,
                          final Method method,
                          final Object[] args )
    {
        if( args == null )
        {
            if( Property.class.isAssignableFrom( method.getReturnType() ) )
            {
                return Proxy.newProxyInstance(
                    this.getClass().getClassLoader(),
                    new Class[]{ method.getReturnType(), PropertyExpression.class },
                    new PropertyExpressionProxy( method, traversed )
                );
            }
            else if( Association.class.isAssignableFrom( method.getReturnType() ) )
            {
                return Proxy.newProxyInstance(
                    this.getClass().getClassLoader(),
                    new Class[]{ method.getReturnType() },
                    new AssociationExpressionProxy( new AssociationExpressionImpl( method, traversed ) )
                );
            }
        }
        throw new UnsupportedOperationException( "Only property methods can be used for queries" );
    }

}