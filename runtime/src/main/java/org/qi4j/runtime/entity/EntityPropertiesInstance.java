/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.runtime.entity;

import org.qi4j.api.property.Property;
import org.qi4j.runtime.property.*;
import org.qi4j.runtime.unitofwork.BuilderEntityState;
import org.qi4j.spi.entity.EntityState;

import java.lang.reflect.AccessibleObject;
import java.util.HashMap;
import java.util.Iterator;

/**
 * JAVADOC
 */
public class EntityPropertiesInstance
    extends PropertiesInstance
{
    private PropertiesModel model;
    private EntityState entityState;

    public EntityPropertiesInstance( PropertiesModel model, EntityState entityState )
    {
        super( null );
        this.model = model;
        this.entityState = entityState;
    }

    public <T> Property<T> propertyFor( AccessibleObject accessor )
            throws IllegalArgumentException
    {
        if( properties == null )
        {
            properties = new HashMap<AccessibleObject, Property<?>>();
        }

        Property<T> property = (Property<T>) properties.get( accessor );

        if( property == null )
        {
            PersistentPropertyModel entityPropertyModel = (PersistentPropertyModel) model.getProperty( accessor );
            if (entityPropertyModel == null)
                throw new IllegalArgumentException("No such property:"+accessor);

            property = new EntityPropertyInstance<T>( entityState instanceof BuilderEntityState ? entityPropertyModel.getBuilderInfo() : entityPropertyModel, entityState);
            properties.put( accessor, property );
        }

        return property;
    }

    @Override
    public Iterable<Property<?>> properties()
    {
        return new Iterable<Property<?>>()
        {
            @Override
            public Iterator<Property<?>> iterator()
            {
                final Iterator<PropertyModel> propertyModels = model.properties().iterator();
                return new Iterator<Property<?>>()
                {
                    @Override
                    public boolean hasNext()
                    {
                        return propertyModels.hasNext();
                    }

                    @Override
                    public Property<?> next()
                    {
                        PropertyModel model = propertyModels.next();
                        return propertyFor( model.accessor() );
                    }

                    @Override
                    public void remove()
                    {
                    }
                };
            }
        };
    }

    public void checkConstraints()
    {
        model.checkConstraints( this );
    }
}
