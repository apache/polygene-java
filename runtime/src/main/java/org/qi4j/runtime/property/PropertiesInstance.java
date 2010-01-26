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

import java.lang.reflect.Method;
import java.util.Map;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.property.Property;
import org.qi4j.api.property.StateHolder;

/**
 * Collection of Property instances.
 */
public class PropertiesInstance
    implements StateHolder
{
    protected Map<Method, Property<?>> properties;

    public PropertiesInstance( Map<Method, Property<?>> properties )
    {
        this.properties = properties;
    }

    public <T> Property<T> getProperty( Method propertyMethod )
    {
        return (Property<T>) properties.get( propertyMethod );
    }

    public <T> Property<T> getProperty( QualifiedName name )
    {
        for( Property property : properties.values() )
        {
            if( property.qualifiedName().equals( name ) )
            {
                return property;
            }
        }
        return null; // indicate with null that it has not been found.
    }

    public void visitProperties( StateVisitor visitor )
    {
        for( Property<?> property : properties.values() )
        {
            visitor.visitProperty( property.qualifiedName(), property.get() );
        }
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

        PropertiesInstance that = (PropertiesInstance) o;
        return properties.equals( that.properties );
    }

    @Override
    public int hashCode()
    {
        return properties.hashCode();
    }
}
