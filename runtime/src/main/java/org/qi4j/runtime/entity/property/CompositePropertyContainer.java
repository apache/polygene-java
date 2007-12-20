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

import org.qi4j.composite.Composite;
import org.qi4j.entity.EntitySession;
import org.qi4j.entity.property.Property;
import org.qi4j.entity.property.PropertyAccess;
import org.qi4j.entity.property.PropertyChange;
import org.qi4j.entity.property.PropertyContainer;
import org.qi4j.entity.property.PropertyVetoException;
import org.qi4j.entity.property.ReadableProperty;

/**
 * TODO
 */
public class CompositePropertyContainer<T>
    extends AbstractPropertyContainer<T>
{
    private Composite composite;

    public CompositePropertyContainer( PropertyContainer<T> container, Composite composite )
    {
        super( container );
        this.composite = composite;
    }

    public PropertyChange<T> newChange( Property<T> property, T newValue, Composite composite, EntitySession entitySession )
        throws PropertyVetoException
    {
        return super.newChange( property, newValue, this.composite, entitySession );
    }

    public PropertyAccess<T> newAccess( ReadableProperty<T> property, Composite composite, EntitySession entitySession )
    {
        return super.newAccess( property, this.composite, entitySession );
    }
}
