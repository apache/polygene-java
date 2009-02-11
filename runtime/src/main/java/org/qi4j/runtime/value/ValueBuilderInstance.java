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

import java.lang.reflect.Method;
import java.util.Iterator;
import org.qi4j.api.common.ConstructionException;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.property.StateHolder;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.runtime.composite.ValueCompositeInstance;
import org.qi4j.runtime.composite.ValueModel;
import org.qi4j.runtime.structure.ModuleInstance;

/**
 * TODO
 */
public final class ValueBuilderInstance<T>
    implements ValueBuilder<T>
{
    private static final Method TYPE_METHOD;
    private static final Method METAINFO_METHOD;

    static
    {
        try
        {
            TYPE_METHOD = Composite.class.getMethod( "type" );
            METAINFO_METHOD = Composite.class.getMethod( "metaInfo", Class.class );
        }
        catch( NoSuchMethodException e )
        {
            throw new InternalError( "Qi4j Core Runtime codebase is corrupted. Contact Qi4j team: ValueBuilderInstance" );
        }
    }

    private final ModuleInstance moduleInstance;
    private final ValueModel valueModel;
    private final Class<T> valueType;

    // lazy initialized in accessor
    private T prototypeInstance;

    // lazy initialized in accessor
    private StateHolder state;

    public ValueBuilderInstance( ModuleInstance moduleInstance, ValueModel valueModel )
    {
        this.moduleInstance = moduleInstance;

        this.valueModel = valueModel;
        valueType = (Class<T>) valueModel.type();
    }

    public Class<T> valueType()
    {
        return valueType;
    }

    public ValueBuilder<T> withPrototype( T value )
    {
        ValueCompositeInstance valueInstance = ValueCompositeInstance.getValueInstance( (ValueComposite) value );
        StateHolder state = valueInstance.state();
        this.state = valueModel.newBuilderState( state );

        return this;
    }

    public T prototype()
    {
        // Instantiate given value type
        if( prototypeInstance == null )
        {
            prototypeInstance = (T) valueModel.newValueInstance( moduleInstance, getState(), true ).proxy();
        }

        return prototypeInstance;
    }

    public T newInstance() throws ConstructionException
    {
        StateHolder instanceState;
        if( state == null )
        {
            instanceState = valueModel.newDefaultState();
        }
        else
        {
            instanceState = valueModel.newState(state);
        }

        ValueCompositeInstance valueCompositeInstance = valueModel.newValueInstance( moduleInstance, instanceState, false );
        return valueType.cast( valueCompositeInstance.proxy() );
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