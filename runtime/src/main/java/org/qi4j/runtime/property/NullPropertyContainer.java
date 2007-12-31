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
public final class NullPropertyContainer<T>
    implements PropertyContainer<T>
{
    PropertyChange<T> changeInstance = new PropertyChange<T>();
    PropertyAccess<T> accessInstance = new PropertyAccess<T>();

    public PropertyChange<T> newChange( Property<T> writableProperty, T newValue, Composite composite, EntitySession entitySession ) throws PropertyVetoException
    {
        changeInstance.set( writableProperty, newValue, composite, entitySession );
        return changeInstance;
    }

    public PropertyAccess<T> newAccess( ReadableProperty<T> readableProperty, Composite composite, EntitySession entitySession )
    {
        accessInstance.set( readableProperty, composite, entitySession );
        return accessInstance;
    }

    public <T> T getPropertyInfo( Class<T> infoType )
    {
        return null;
    }

    public String getName()
    {
        // TODO: What is a name?
        return null;
    }

    public String getQualifiedName()
    {
        // TODO: What is a qualified name?
        return null;
    }

    public void addChangeObserver( PropertyChangeObserver<T> propertyChangeObserver )
    {
        // Ignore
    }

    public void addChangeVeto( PropertyChangeVeto<T> propertyChangeVeto )
    {
        // Ignore
    }

    public void addAccessObserver( PropertyAccessObserver<T> propertyAccessObserver )
    {
        // Ignore
    }
}
