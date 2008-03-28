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

/**
 * TODO Add JavaDoc
 *
 * @author Alin Dreghiciu
 * @since March 25, 2008
 */
public class AssociationExpressionProxy
    implements InvocationHandler
{

    /**
     * Traversed association.
     */
    private final AssociationExpression traversed;

    /**
     * Constructor.
     *
     * @param traversed traversed association
     */
    AssociationExpressionProxy( final AssociationExpression traversed )
    {
        this.traversed = traversed;
    }

    public Object invoke( final Object proxy,
                          final Method method,
                          final Object[] args )
    {
        if( args == null && "get".equals( method.getName() ) )
        {
            return Proxy.newProxyInstance(
                this.getClass().getClassLoader(),
                new Class[]{ traversed.getType() },
                new MixinTypeProxyFactory( traversed )
            );
        }
        throw new UnsupportedOperationException( "Only property methods can be used for queries" );
    }

}