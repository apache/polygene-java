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

package org.qi4j.runtime.value;

import java.lang.reflect.Method;
import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.entity.Queryable;
import org.qi4j.api.property.GenericPropertyInfo;
import org.qi4j.api.property.Property;
import org.qi4j.runtime.composite.ValueConstraintsInstance;
import org.qi4j.runtime.property.PersistentPropertyModel;
import org.qi4j.runtime.types.PropertyTypeImpl;
import org.qi4j.runtime.types.ValueTypeFactory;
import org.qi4j.spi.property.PropertyTypeDescriptor;
import org.qi4j.spi.property.ValueType;

/**
 * Property model for values
 */
public final class ValuePropertyModel
    extends PersistentPropertyModel
    implements PropertyTypeDescriptor
{
    public ValuePropertyModel( Method anAccessor,
                               Class compositeType, ValueConstraintsInstance constraints,
                               MetaInfo metaInfo,
                               Object defaultValue
    )
    {
        super( createPropertyType( anAccessor, compositeType ), anAccessor, true, constraints, metaInfo, defaultValue );
    }

    private static PropertyTypeImpl createPropertyType( Method anAccessor, Class compositeType )
    {
        final Queryable queryable = anAccessor.getAnnotation( Queryable.class );
        boolean isQueryable = queryable == null || queryable.value();
        ValueType valueType = ValueTypeFactory.instance()
            .newValueType( GenericPropertyInfo.getPropertyType( anAccessor ),
                           anAccessor.getDeclaringClass(),
                           compositeType );
        return new PropertyTypeImpl( QualifiedName.fromMethod( anAccessor ),
                                     valueType,
                                     isQueryable,
                                     PropertyTypeImpl.PropertyTypeEnum.IMMUTABLE );
    }

    public Property<?> newInstance( Object value )
    {
        // Property was constructed using a builder

        Property property;
        if( isComputed() )
        {
            property = new ComputedPropertyInfo<Object>( propertyInfo );
        }
        else
        {
            property = new ValuePropertyInstance<Object>( propertyInfo, value );
        }
        return wrapProperty( property );
    }
}