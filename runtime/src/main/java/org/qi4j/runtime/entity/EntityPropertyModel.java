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
import org.qi4j.composite.ConstraintViolationException;
import org.qi4j.property.Property;
import org.qi4j.runtime.composite.ValueConstraintsInstance;
import org.qi4j.runtime.property.PropertyModel;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.PropertyType;
import org.qi4j.spi.property.ImmutablePropertyInstance;
import org.qi4j.util.ClassUtil;
import org.qi4j.util.MetaInfo;

/**
 * TODO
 */
public final class EntityPropertyModel extends PropertyModel
{
    public EntityPropertyModel( Method anAccessor, ValueConstraintsInstance constraints, MetaInfo metaInfo, Object defaultValue )
    {
        super( anAccessor, constraints, metaInfo, defaultValue );
    }

    public Property newEntityInstance( EntityState state )
    {
        if( isImmutable() )
        {
            return new ImmutablePropertyInstance( this, state.getProperty( qualifiedName() ) );
        }
        else if( isComputed() )
        {
            return super.newInstance();
        }
        else
        {
            return new EntityPropertyInstance( this, state );
        }
    }

    public void setState( Property property, EntityState entityState )
        throws ConstraintViolationException
    {
        Object value;

        if( property == null || property.get() == ImmutablePropertyInstance.UNSET )
        {
            value = defaultValue();
        }
        else
        {
            value = property.get();
        }

        // Check constraints
        checkConstraints( value );

        entityState.setProperty( qualifiedName(), value );
    }

    public PropertyType propertyType()
    {
        return new PropertyType( qualifiedName(), ClassUtil.getRawClass( type() ).getName() );
    }
}
