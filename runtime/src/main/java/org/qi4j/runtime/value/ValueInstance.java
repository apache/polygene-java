/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2007, Niclas Hedhman. All Rights Reserved.
 * Copyright (c) 2007, Alin Dreghiciu. All Rights Reserved.
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

import java.lang.reflect.Proxy;
import org.json.JSONException;
import org.json.JSONStringer;
import org.qi4j.api.property.StateHolder;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.runtime.composite.MixinsInstance;
import org.qi4j.runtime.composite.TransientInstance;
import org.qi4j.runtime.structure.ModuleInstance;
import org.qi4j.spi.composite.CompositeInstance;
import org.qi4j.spi.value.ValueDescriptor;

/**
 * ValueComposite instance
 */
public final class ValueInstance
    extends TransientInstance
    implements CompositeInstance, MixinsInstance
{
    public static ValueInstance getValueInstance( ValueComposite composite )
    {
        return (ValueInstance) Proxy.getInvocationHandler( composite );
    }

    public ValueInstance( ValueModel compositeModel, ModuleInstance moduleInstance, Object[] mixins, StateHolder state )
    {
        super( compositeModel, moduleInstance, mixins, state );
    }

    @Override
    public boolean equals( Object o )
    {
        if( this == o )
        {
            return true;
        }
        if( o == null || !Proxy.isProxyClass( o.getClass() ) )
        {
            return false;
        }

        try
        {
            ValueInstance that = (ValueInstance) Proxy.getInvocationHandler( o );
            return state.equals( that.state );
        }
        catch( ClassCastException e )
        {
            return false;
        }
    }

    @Override
    public ValueDescriptor descriptor()
    {
        return (ValueDescriptor) compositeModel;
    }

    @Override
    public int hashCode()
    {
        return state.hashCode();
    }

    @Override
    public String toString()
    {
        try
        {
            JSONStringer stringer = new JSONStringer();
            ( (ValueModel) compositeModel ).valueType().toJSON( proxy(), stringer );
            return stringer.toString();
        }
        catch( JSONException e )
        {
            return super.toString();
        }
    }
}