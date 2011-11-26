/*
 * Copyright 2008 Niclas Hedhman. All rights Reserved.
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
package org.qi4j.lib.swing.binding;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectFactory;
import org.qi4j.api.property.Property;
import org.qi4j.lib.swing.binding.internal.BoundProperty;
import org.qi4j.lib.swing.binding.internal.StateInvocationHandler;

import static java.lang.reflect.Proxy.newProxyInstance;

public class StateModel<T>
{
    private T currentData;
    private final Class<T> type;
    private StateInvocationHandler<T> childModel;

    public StateModel( @Uses Class<T> aType, @Structure ObjectFactory objectBuilderFactory )
    {
        type = aType;
        //noinspection unchecked
        childModel = objectBuilderFactory.newObject( StateInvocationHandler.class, aType );
    }

    /**
     * Sets the data to this component model and its children.
     *
     * @param aData The data to set.
     */
    public void use( T aData )
    {
        currentData = aData;
        childModel.use( aData );
    }

    public T stateInUse()
    {
        return currentData;
    }

    @SuppressWarnings( { "unchecked" } )
    public final T state()
    {
        ClassLoader loader = StateInvocationHandler.class.getClassLoader();
        Class[] typeInterfaces = new Class[]{ type };
        return (T) newProxyInstance( loader, typeInterfaces, childModel );
    }

    public Binding bind( Property property )
    {
        if( property instanceof BoundProperty )
        {
            return (BoundProperty) property;
        }
        throw new IllegalArgumentException( "Unknown property template: " + property );
    }
}
