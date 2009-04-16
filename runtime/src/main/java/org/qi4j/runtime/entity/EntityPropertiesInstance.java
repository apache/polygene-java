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

import java.lang.reflect.Method;
import java.util.HashMap;
import org.qi4j.api.property.Property;
import org.qi4j.runtime.property.PropertiesInstance;
import org.qi4j.runtime.structure.ModuleUnitOfWork;
import org.qi4j.spi.entity.EntityState;

/**
 * JAVADOC
 */
public class EntityPropertiesInstance
    extends PropertiesInstance
{
    private EntityPropertiesModel model;
    private EntityState entityState;
    private ModuleUnitOfWork uow;

    public EntityPropertiesInstance( EntityPropertiesModel model, EntityState entityState, ModuleUnitOfWork uow )
    {
        super( null );
        this.model = model;
        this.entityState = entityState;
        this.uow = uow;
    }

    public <T> Property<T> getProperty( Method accessor )
    {
        if( properties == null )
        {
            properties = new HashMap<Method, Property<?>>();
        }

        Property<T> property = (Property<T>) properties.get( accessor );

        if( property == null )
        {
            property = model.newInstance( accessor, entityState, uow );
            properties.put( accessor, property );
        }

        return property;
    }

    public void refresh( EntityState entityState )
    {
        if( properties != null )
        {
            for( Property<?> property : properties.values() )
            {
                EntityPropertyInstance entityProperty = (EntityPropertyInstance) property;
                entityProperty.refresh( entityState );
            }
        }

        this.entityState = entityState;
    }

    public void checkConstraints()
    {
        model.checkConstraints( this );
    }
}
