/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
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
import java.util.Map;
import org.qi4j.api.property.Property;
import org.qi4j.spi.entity.EntityState;

/**
 * TODO
 */
public final class EntityPropertiesInstance
{
    private final EntityPropertiesModel entityPropertiesModel;
    private final EntityState entityState;

    private Map<Method, Property> properties;

    public EntityPropertiesInstance( EntityPropertiesModel entityPropertiesModel, EntityState entityState )
    {
        this.entityPropertiesModel = entityPropertiesModel;
        this.entityState = entityState;
    }

    public Property<?> propertyFor( Method accessor )
    {
        if( properties == null )
        {
            properties = new HashMap<Method, Property>();
        }

        Property<?> property = properties.get( accessor );

        if( property == null )
        {
            property = entityPropertiesModel.newInstance( accessor, entityState );
            properties.put( accessor, property );
        }

        return property;
    }
}
