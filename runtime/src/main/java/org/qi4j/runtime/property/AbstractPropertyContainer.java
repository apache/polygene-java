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
import org.qi4j.entity.EntitySession;
import org.qi4j.property.Property;
import org.qi4j.property.PropertyAccess;
import org.qi4j.property.PropertyAccessObserver;
import org.qi4j.property.PropertyChange;
import org.qi4j.property.PropertyChangeObserver;
import org.qi4j.property.PropertyChangeVeto;
import org.qi4j.property.PropertyContainer;
import org.qi4j.property.PropertyVetoException;
import org.qi4j.property.ReadableProperty;

/**
 * TODO
 */
public abstract class AbstractPropertyContainer<T>
    implements PropertyContainer<T>
{
    protected PropertyContainer<T> container;

    protected PropertyChangeObserver<T> changeObserver;
    protected PropertyAccessObserver<T> accessObserver;
    protected PropertyChangeVeto<T> changeVeto;

    protected boolean processingEvent;

    public AbstractPropertyContainer( PropertyContainer<T> container )
    {
        this.container = container;
    }

    public <T> T getPropertyInfo( Class<T> infoType )
    {
        return container.getPropertyInfo( infoType );
    }

    public void addChangeObserver( PropertyChangeObserver<T> changeObserver )
    {
        if( this.changeObserver == null )
        {
            this.changeObserver = changeObserver;
        }
        else
        {
            this.changeObserver = new ChainedChangeObserver<T>( this.changeObserver, changeObserver );
        }
    }

    public void addAccessObserver( PropertyAccessObserver<T> accessObserver )
    {
        if( this.accessObserver == null )
        {
            this.accessObserver = accessObserver;
        }
        else
        {
            this.accessObserver = new ChainedAccessObserver<T>( this.accessObserver, accessObserver );
        }
    }

    public void addChangeVeto( PropertyChangeVeto<T> propertyChangeVeto )
    {
        if( this.changeVeto == null )
        {
            this.changeVeto = propertyChangeVeto;
        }
        else
        {
            this.changeVeto = new ChainedChangeVeto<T>( this.changeVeto, propertyChangeVeto );
        }
    }

    public PropertyChange<T> newChange( Property<T> writableProperty, T newValue, Composite composite, EntitySession entitySession ) throws PropertyVetoException
    {
        try
        {
            processingEvent = true;
            PropertyChange<T> change = container.newChange( writableProperty, newValue, null, null );

            if( changeVeto != null )
            {
                changeVeto.onChange( change );
            }

            if( changeObserver != null )
            {
                changeObserver.onChange( change );
            }

            return change;
        }
        finally
        {
            processingEvent = false;
        }
    }

    public PropertyAccess<T> newAccess( ReadableProperty<T> readableProperty, Composite composite, EntitySession entitySession )
    {
        if( !processingEvent )
        {
            try
            {
                processingEvent = true;

                PropertyAccess<T> access = container.newAccess( readableProperty, null, null );

                if( accessObserver != null )
                {
                    accessObserver.onAccess( access );
                }
                return access;
            }
            finally
            {
                processingEvent = false;
            }
        }
        else
        {
            return null;
        }
    }

    public String getName()
    {
        // TODO: What is a name?
        return "name???";
    }

    public String getQualifiedName()
    {
        // TODO: What is a name?
        return "qualifiedName?";
    }
}
