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

package org.qi4j.spi.property;

import java.lang.reflect.Method;
import org.qi4j.property.ComputedPropertyInstance;
import org.qi4j.property.GenericPropertyInfo;
import org.qi4j.property.ImmutableProperty;
import org.qi4j.property.PropertyInfo;

/**
 * TODO
 */
public final class ImmutablePropertyInstance<T> extends ComputedPropertyInstance<T>
    implements ImmutableProperty<T>
{
    // During initialization of the property the value will be set to this
    // As long as this value is used the value can be changed.
    public static final Object UNSET = "UNSET";

    protected T value;

    public ImmutablePropertyInstance( Method accessor, T value )
    {
        this( new GenericPropertyInfo( accessor ), value );
    }

    public ImmutablePropertyInstance( Class declaringClass, String accessorName, T value )
    {
        this( new GenericPropertyInfo( declaringClass, accessorName ), value );
    }

    public ImmutablePropertyInstance( PropertyInfo info, T value )
    {
        super( info );
        this.value = value;
    }

    @SuppressWarnings( { "unchecked" } )
    public ImmutablePropertyInstance( PropertyInfo info )
    {
        super( info );
        this.value = (T) UNSET;
    }

    @SuppressWarnings( { "unchecked" } )
    public T get()
    {
        if( value == UNSET )
        {
            return (T) ( (PropertyDescriptor) propertyInfo ).defaultValue();
        }
        return value;
    }

    /**
     * Throws {@link IllegalArgumentException} exception.
     *
     * @param newValue This value is ignored, unless this is set during the initialization phase.
     * @throws IllegalArgumentException Thrown by default.
     * @since 0.1.0
     */
    public void set( T newValue )
        throws IllegalArgumentException
    {
        if( this.value != UNSET && newValue != UNSET )
        {
            super.set( newValue );
        }
        else
        {
            this.value = newValue;
        }
    }

    @Override public String toString()
    {
        if( value == null )
        {
            return "";
        }
        else
        {
            return "[" + value.toString() + "]";
        }
    }

    @Override public boolean equals( Object o )
    {
        if( this == o )
        {
            return true;
        }
        if( o == null || getClass() != o.getClass() )
        {
            return false;
        }
        if( !super.equals( o ) )
        {
            return false;
        }
        ImmutablePropertyInstance that = (ImmutablePropertyInstance) o;
        return !( value != null ? !value.equals( that.value ) : that.value != null );
    }

    @Override public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + ( value != null ? value.hashCode() : 0 );
        return result;
    }
}
