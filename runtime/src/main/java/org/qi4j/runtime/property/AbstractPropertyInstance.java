/*
 * Copyright 2008 Niclas Hedhman.
 * Copyright 2008 Edward Yakop.
 * Copyright 2008 Rickard Öberg.
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
package org.qi4j.runtime.property;

import org.qi4j.api.property.Property;
import org.qi4j.api.property.PropertyDescriptor;

/**
 * {@code AbstractPropertyInstance} is the base implementation of {@link org.qi4j.api.property.Property}.
 */
public abstract class AbstractPropertyInstance<T>
    implements Property<T>
{
    protected final PropertyDescriptor propertyDescriptor;

    /**
     * Construct an instance
     *
     * @param descriptor The property descriptor. This argument must not be {@code null}.
     *
     * @throws IllegalArgumentException Thrown if the specified {@code aPropertyInfo} argument is {@code null}.
     */
    protected AbstractPropertyInstance( PropertyDescriptor descriptor )
    {
        propertyDescriptor = descriptor;
    }

    public PropertyDescriptor getPropertyDescriptor()
    {
        return propertyDescriptor;
    }

    /**
     * Perform equals with {@code o} argument.
     * <p/>
     * The definition of equals() for the property is that if the value and subclass are
     * equal, then the properties are equal
     *
     * @param o The other object to compare.
     *
     * @return Returns a {@code boolean} indicator whether this object is equals the other.
     */
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

        Property<?> that = (Property<?>) o;

        T value = get();
        if( value == null )
        {
            return that.get() == null;
        }
        return value.equals( that.get() );
    }

    /**
     * Calculate hash code.
     *
     * @return the hashcode of this instance.
     */
    public int hashCode()
    {
        int hash = getClass().hashCode();
        if( propertyDescriptor != null )
        {
            hash = propertyDescriptor.type().hashCode();
        }
        hash = hash * 19;
        T value = get();
        if( value != null )
        {
            hash = hash + value.hashCode() * 13;
        }
        return hash;
    }
}