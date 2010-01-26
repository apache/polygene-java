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

import java.util.Iterator;
import org.qi4j.api.common.ConstructionException;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.property.StateHolder;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.runtime.structure.ModuleInstance;

/**
 * Implementation of ValueBuilder
 */
public final class ValueBuilderInstance<T>
    implements ValueBuilder<T>
{
    private final ModuleInstance moduleInstance;
    private final ValueModel valueModel;

    // lazy initialized in accessor
    private ValueInstance prototypeInstance;

    // lazy initialized in accessor
    private StateHolder state;

    public ValueBuilderInstance( ModuleInstance moduleInstance, ValueModel valueModel )
    {
        this.moduleInstance = moduleInstance;

        this.valueModel = valueModel;
    }

    public ValueBuilder<T> withPrototype( T value )
    {
        ValueInstance valueInstance = ValueInstance.getValueInstance( (ValueComposite) value );
        StateHolder state = valueInstance.state();
        this.state = valueModel.newBuilderState( state );
        prototypeInstance = null;

        return this;
    }

    public ValueBuilder<T> withState( StateHolder state )
    {
        final StateHolder valueState = getState();
        state.visitProperties( new StateHolder.StateVisitor()
        {
            public void visitProperty( QualifiedName name, Object value )
            {
                valueModel.state().setProperty( name, value, valueState );
            }
        } );
        return this;
    }

    public T prototype()
    {
        // Instantiate given value type
        if( prototypeInstance == null )
        {
            prototypeInstance = valueModel.newValueInstance( moduleInstance, getState() );
        }

        return prototypeInstance.<T>proxy();
    }

    public <K> K prototypeFor( Class<K> mixinType )
    {
        // Instantiate given value type
        if( prototypeInstance == null )
        {
            prototypeInstance = valueModel.newValueInstance( moduleInstance, getState() );
        }

        return prototypeInstance.newProxy( mixinType );
    }

    public T newInstance()
        throws ConstructionException
    {
        StateHolder instanceState;
        if( state == null )
        {
            instanceState = valueModel.newInitialState();
        }
        else
        {
            instanceState = valueModel.newState( state );
        }

        valueModel.checkConstraints( instanceState );
        ValueInstance valueInstance = valueModel.newValueInstance( moduleInstance, instanceState );
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
            state = valueModel.newBuilderState();
        }

        return state;
    }
}