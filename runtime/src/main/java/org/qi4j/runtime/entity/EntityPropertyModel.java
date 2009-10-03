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
import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.property.Property;
import org.qi4j.runtime.composite.ValueConstraintsInstance;
import org.qi4j.runtime.property.PersistentPropertyModel;
import org.qi4j.runtime.unitofwork.BuilderEntityState;
import org.qi4j.spi.entity.EntityState;

/**
 * JAVADOC
 */
public final class EntityPropertyModel
    extends PersistentPropertyModel
{
    public EntityPropertyModel( Method anAccessor,
                                Class compositeType,
                                boolean immutable,
                                ValueConstraintsInstance constraints,
                                MetaInfo metaInfo,
                                Object defaultValue
    )
    {
        super( anAccessor, compositeType, immutable, constraints, metaInfo, defaultValue );
    }

    public Property<?> newInstance( Object value )
    {
        // Unused
        return null;
    }

    public <T> Property<T> newInstance( EntityState state )
    {
        Property property;
        if( isComputed() )
        {
            property = new ComputedPropertyInfo<Object>( propertyInfo );
        }
        else
        {
            property = new EntityPropertyInstance( state instanceof BuilderEntityState ? builderInfo : this, state, this );
        }

        return wrapProperty( property );
    }
}
