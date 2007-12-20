/*
 * Copyright (c) 2007, Rickard …berg. All Rights Reserved.
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

package org.qi4j.runtime.entity.property;

import org.qi4j.entity.property.Property;
import org.qi4j.entity.property.PropertyAccessObserver;
import org.qi4j.entity.property.PropertyChangeObserver;
import org.qi4j.entity.property.PropertyChangeVeto;
import org.qi4j.entity.property.PropertyContainer;
import org.qi4j.entity.property.PropertyVetoException;

/**
 * TODO
 */
public class PropertyInstance<T>
    implements Property<T>
{
    private PropertyContainer<T> container;
    private T value;

    public PropertyInstance( PropertyContainer<T> container, T value )
    {
        this.container = container;
        this.value = value;
    }

    public T get()
    {
        container.newAccess( this, null, null );
        return value;
    }

    public void set( T newValue )
        throws PropertyVetoException
    {
        container.newChange( this, newValue, null, null );
        value = newValue;
    }

    public <T> T getPropertyInfo( Class<T> infoType )
    {
        return container.getPropertyInfo( infoType );
    }

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

    public void update( T value )
    {
        this.value = value;
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
