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

package org.qi4j.runtime.property;

import org.qi4j.api.property.Property;
import org.qi4j.api.property.StateHolder;

import java.lang.reflect.AccessibleObject;
import java.util.Map;

/**
 * Collection of Property instances.
 */
public class PropertiesInstance
    implements StateHolder
{
    protected Map<AccessibleObject, Property<?>> properties;

    public PropertiesInstance( Map<AccessibleObject, Property<?>> properties )
    {
        this.properties = properties;
    }

    public <T> Property<T> propertyFor( AccessibleObject accessor )
            throws IllegalArgumentException
    {
        Property<T> property = (Property<T>) properties.get( accessor );

        if (property == null)
            throw new IllegalArgumentException( "No such property:"+accessor );

        return property;
    }

    @Override
    public Iterable<Property<?>> properties()
    {
        return properties.values();
    }

    @Override
    public boolean equals( Object o )
    {
        if( this == o )
        {
            return true;
        }
        if( o == null || getClass() != o.getClass() )
        {
            return false;
        }

        // Check if all properties are the same
        PropertiesInstance that = (PropertiesInstance) o;
        for( Map.Entry<AccessibleObject, Property<?>> propertyEntry : properties.entrySet() )
        {
            Property<?> thatProperty = that.propertyFor( propertyEntry.getKey() );
            if (thatProperty == null)
                return false;
            else
                if (!propertyEntry.getValue().equals( thatProperty ))
                    return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        return properties.hashCode();
    }

    @Override
    public String toString()
    {
        return "properties=" + properties;
    }
}
