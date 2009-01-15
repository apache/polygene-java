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
package org.qi4j.library.swing.binding;

import static java.lang.reflect.Proxy.newProxyInstance;
import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.association.Association;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilder;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.property.Property;
import static org.qi4j.api.util.NullArgumentException.validateNotNull;
import org.qi4j.library.swing.binding.internal.BoundAssociation;
import org.qi4j.library.swing.binding.internal.BoundField;
import org.qi4j.library.swing.binding.internal.BoundProperty;
import org.qi4j.library.swing.binding.internal.StateInvocationHandler;

public final class StateModel<T>
{
    private @Uses @Optional BoundField fieldOwnedThis;

    private final Class<T> type;
    private final ObjectBuilderFactory obf;
    private final StateInvocationHandler childModel;

    private T currentData;

    public StateModel( @Uses Class<T> aType, @Structure ObjectBuilderFactory anObjectBuilderFactory )
    {
        type = aType;
        obf = anObjectBuilderFactory;

        ObjectBuilder<StateInvocationHandler> objectBuilder = obf.newObjectBuilder( StateInvocationHandler.class );
        objectBuilder.use( aType );
        childModel = objectBuilder.newInstance();
    }

    /**
     * Sets the data to this component model and its children.
     *
     * @param aData The data to set.
     */
    @SuppressWarnings( "unchecked" )
    public void use( T aData )
    {
        if( fieldOwnedThis != null )
        {
            fieldOwnedThis.stateToUse( aData );
        }

        currentData = aData;
        childModel.use( aData );
    }

    public T stateInUse()
    {
        return currentData;
    }

    @SuppressWarnings( "unchecked" )
    public final T state()
    {
        ClassLoader loader = StateInvocationHandler.class.getClassLoader();
        Class[] typeInterfaces = new Class[]{ type };
        return (T) newProxyInstance( loader, typeInterfaces, childModel );
    }

    @SuppressWarnings( "unchecked" )
    public <T> SwingBinding<T> bind( Property<T> aProperty )
    {
        validateNotNull( "aProperty", aProperty );
        if( aProperty instanceof BoundProperty )
        {
            return (BoundProperty) aProperty;
        }
        throw new IllegalArgumentException( "Unknown property template: " + aProperty );
    }

    @SuppressWarnings( "unchecked" )
    public <T> SwingBinding<T> bind( Association<T> anAssociation )
    {
        validateNotNull( "anAssociation", anAssociation );

        if( anAssociation instanceof BoundAssociation )
        {
            return (BoundAssociation) anAssociation;
        }

        throw new IllegalArgumentException( "Unknown association template: " + anAssociation );
    }
}
