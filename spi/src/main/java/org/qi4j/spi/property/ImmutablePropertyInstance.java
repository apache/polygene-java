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

import org.qi4j.property.ImmutableProperty;
import org.qi4j.property.PropertyInfo;

/**
 * TODO
 */
public final class ImmutablePropertyInstance<T> extends ComputedPropertyInstance<T>
    implements ImmutableProperty<T>
{
    protected T value;

    public ImmutablePropertyInstance( PropertyInfo info, T value )
    {
        super( info );
        this.value = value;
    }

    public T get()
    {
        return value;
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

        if( value != null ? !value.equals( that.value ) : that.value != null )
        {
            return false;
        }

        return true;
    }

    @Override public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + ( value != null ? value.hashCode() : 0 );
        return result;
    }
}
