/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.qi4j.runtime.value;

import org.qi4j.api.common.ConstructionException;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.property.Property;
import org.qi4j.api.property.StateHolder;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.runtime.structure.ModelModule;

import java.util.Iterator;

/**
 * Implementation of ValueBuilder
 */
public final class ValueBuilderInstance<T>
    implements ValueBuilder<T>
{
    private final ModelModule<ValueModel> model;

    // lazy initialized in accessor
    private ValueInstance prototypeInstance;

    // lazy initialized in accessor
    private StateHolder state;

    public ValueBuilderInstance( ModelModule<ValueModel> model)
    {
        this.model = model;
    }

    public ValueBuilder<T> withPrototype( T value )
    {
        ValueInstance valueInstance = ValueInstance.getValueInstance( (ValueComposite) value );
        StateHolder state = valueInstance.state();
        this.state = model.model().newBuilderState( state );
        prototypeInstance = null;

        return this;
    }

    public ValueBuilder<T> withState( StateHolder state )
    {
        final StateHolder valueState = getState();
        state.visitProperties( new StateHolder.StateVisitor<RuntimeException>()
        {
            public void visitProperty( QualifiedName name, Object value )
            {
                Property<Object> property = valueState.getProperty( name );
                if( property != null )
                {
                    property.set( value );
                }
            }
        } );
        return this;
    }

    public T prototype()
    {
        // Instantiate given value type
        if( prototypeInstance == null )
        {
            prototypeInstance = model.model().newValueInstance( model.module(), getState() );
        }

        return prototypeInstance.<T>proxy();
    }

    public <K> K prototypeFor( Class<K> mixinType )
    {
        // Instantiate given value type
        if( prototypeInstance == null )
        {
            prototypeInstance = model.model().newValueInstance( model.module(), getState() );
        }

        return prototypeInstance.newProxy( mixinType );
    }

    public T newInstance()
        throws ConstructionException
    {
        StateHolder instanceState;
        if( state == null )
        {
            instanceState = model.model().newInitialState();
        }
        else
        {
            instanceState = model.model().newState( state );
        }

        model.model().checkConstraints( instanceState );
        ValueInstance valueInstance = model.model().newValueInstance( model.module(), instanceState );
        return valueInstance.<T>proxy();
    }

    public Iterator<T> iterator()
    {
        return new Iterator<T>()
        {
            public boolean hasNext()
            {
                return true;
            }

            public T next()
            {
                return newInstance();
            }

            public void remove()
            {
                throw new UnsupportedOperationException();
            }
        };
    }

    private StateHolder getState()
    {
        if( state == null )
        {
            state = model.model().newBuilderState();
        }

        return state;
    }
}