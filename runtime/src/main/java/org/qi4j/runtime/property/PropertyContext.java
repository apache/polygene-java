/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.runtime.property;

import org.qi4j.composite.Composite;
import org.qi4j.composite.CompositeBuilder;
import org.qi4j.property.ImmutableProperty;
import org.qi4j.property.Property;
import org.qi4j.runtime.structure.ModuleInstance;
import org.qi4j.spi.property.ImmutablePropertyInstance;
import org.qi4j.spi.property.PropertyBinding;
import org.qi4j.spi.property.PropertyInstance;

/**
 * TODO
 */
public final class PropertyContext
{
    private PropertyBinding propertyBinding;

    public PropertyContext( PropertyBinding propertyBinding )
    {
        this.propertyBinding = propertyBinding;
    }

    public PropertyBinding getPropertyBinding()
    {
        return propertyBinding;
    }

    public Property newInstance( ModuleInstance moduleInstance, Object value )
    {
        try
        {
            Class propertyType = propertyBinding.getPropertyResolution().getPropertyModel().getAccessor().getReturnType();

            if( Composite.class.isAssignableFrom( propertyType ) )
            {
                Class<? extends Composite> propertyCompositeType = (Class<? extends Composite>) propertyType;
                CompositeBuilder<? extends Composite> cb = moduleInstance.getStructureContext().getCompositeBuilderFactory().newCompositeBuilder( propertyCompositeType );
                cb.use( value );
                cb.use( propertyBinding );
                return Property.class.cast( cb.newInstance() );
            }
            else
            {
                Property instance;
                if( ImmutableProperty.class.isAssignableFrom( propertyType ) )
                {
                    instance = new ImmutablePropertyInstance<Object>( propertyBinding, value );
                }
                else
                {
                    instance = new PropertyInstance<Object>( propertyBinding, value );
                }

                return instance;
            }
        }
        catch( Exception e )
        {
            throw new InvalidPropertyException( "Could not instantiate property", e );
        }
    }
}
