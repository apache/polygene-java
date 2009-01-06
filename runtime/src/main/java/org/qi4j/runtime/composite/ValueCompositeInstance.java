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

package org.qi4j.runtime.composite;

import java.lang.reflect.Proxy;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.property.StateHolder;
import org.qi4j.runtime.structure.ModuleInstance;
import org.qi4j.runtime.composite.DefaultCompositeInstance;
import org.qi4j.runtime.composite.MixinsInstance;
import org.qi4j.runtime.composite.CompositeModel;
import org.qi4j.spi.composite.CompositeInstance;

/**
 * InvocationHandler for proxy objects.
 */
public final class ValueCompositeInstance extends DefaultCompositeInstance
    implements CompositeInstance, MixinsInstance
{
    public static ValueCompositeInstance getCompositeInstance( Composite composite )
    {
        return (ValueCompositeInstance) Proxy.getInvocationHandler( composite );
    }

    public ValueCompositeInstance( CompositeModel compositeModel, ModuleInstance moduleInstance, Object[] mixins, StateHolder state )
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
        if( o == null || ! Proxy.isProxyClass( o.getClass() ))
        {
            return false;
        }

        try
        {
            ValueCompositeInstance that = (ValueCompositeInstance) Proxy.getInvocationHandler( o );
            return state.equals( that.state );
        }
        catch( ClassCastException e )
        {
            return false;
        }
    }

    @Override
    public int hashCode()
    {
        return state.hashCode();
    }
}