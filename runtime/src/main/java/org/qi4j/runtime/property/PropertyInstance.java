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

import org.qi4j.property.Property;
import org.qi4j.property.PropertyAccessObserver;
import org.qi4j.property.PropertyChangeObserver;
import org.qi4j.property.PropertyChangeVeto;
import org.qi4j.property.PropertyContainer;
import org.qi4j.property.PropertyVetoException;

/**
 * TODO
 */
public final class PropertyInstance<T>
    implements Property<T>
{
    private PropertyContainer<T> container;
    private PropertyContext propertyContext;
    private T value;

    public PropertyInstance( PropertyContainer<T> container, PropertyContext propertyContext, T value )
    {
        this.container = container;
        this.propertyContext = propertyContext;
        this.value = value;
    }

    // ReadableProperty
    public T get()
    {
        container.newAccess( this, null, null );
        return propertyContext.getReadableProperty( this ).get();
    }

    // WritableProperty
    public void set( T newValue )
        throws PropertyVetoException
    {
        container.newChange( this, newValue, null, null );
        propertyContext.getWritableProperty( this ).set( newValue );
    }

    // PropertyInfo
    public <T> T getPropertyInfo( Class<T> infoType )
    {
        return container.getPropertyInfo( infoType );
    }

    public String getName()
    {
        return propertyContext.getPropertyBinding().getPropertyResolution().getPropertyModel().getName();
    }

    public String getQualifiedName()
    {
        return propertyContext.getPropertyBinding().getPropertyResolution().getPropertyModel().getQualifiedName();
    }

    // ObservableProperty
    public void addChangeObserver( PropertyChangeObserver<T> changeObserver )
    {
        if( !( container instanceof InstancePropertyContainer ) )
        {
            container = new InstancePropertyContainer<T>( container );
        }
        container.addChangeObserver( changeObserver );
    }

    public void addAccessObserver( PropertyAccessObserver<T> accessObserver )
    {
        if( !( container instanceof InstancePropertyContainer ) )
        {
            container = new InstancePropertyContainer<T>( container );
        }
        container.addAccessObserver( accessObserver );
    }

    public void addChangeVeto( PropertyChangeVeto<T> propertyChangeVeto )
    {
        if( !( container instanceof InstancePropertyContainer ) )
        {
            container = new InstancePropertyContainer<T>( container );
        }
        container.addChangeVeto( propertyChangeVeto );
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
