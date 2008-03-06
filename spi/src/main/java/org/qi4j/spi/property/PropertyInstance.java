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

import org.qi4j.property.Property;
import org.qi4j.property.PropertyVetoException;
import org.qi4j.property.PropertyInfo;
import org.qi4j.property.ReadableProperty;

/**
 * TODO
 */
public class PropertyInstance<T>
    implements ReadableProperty<T>
{
    private PropertyInfo propertyInfo;
    protected T value;
    protected boolean readonly;

    public PropertyInstance( PropertyInfo propertyInfo, T value )
    {
        this.propertyInfo = propertyInfo;
        this.value = value;
        readonly = false;
    }

    public T get()
    {
        return value;
    }

    public void set( T newValue )
        throws PropertyVetoException
    {
        String message = "Immutable Property. Unable to set Property '" + getName() + "' to value " + newValue;
        throw new PropertyVetoException( message );
    }

    // PropertyInfo
    public <T> T getPropertyInfo( Class<T> infoType )
    {
        return propertyInfo.getPropertyInfo( infoType );
    }

    public String getName()
    {
        return propertyInfo.getName();
    }

    public String getQualifiedName()
    {
        return propertyInfo.getQualifiedName();
    }

    public void write( T value )
    {
        this.value = value;
    }

    public T read()
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
            return value.toString();
        }
    }

    @Override public int hashCode()
    {
        if( value == null )
        {
            return 0;
        }
        else
        {
            return value.hashCode();
        }
    }
}
