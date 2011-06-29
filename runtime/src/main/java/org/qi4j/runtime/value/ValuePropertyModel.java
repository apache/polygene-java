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

import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.property.Property;
import org.qi4j.runtime.composite.ValueConstraintsInstance;
import org.qi4j.runtime.property.PersistentPropertyModel;
import org.qi4j.spi.property.PersistentPropertyDescriptor;

import java.lang.reflect.AccessibleObject;

/**
 * Property model for values
 */
public final class ValuePropertyModel
    extends PersistentPropertyModel
    implements PersistentPropertyDescriptor
{
    public ValuePropertyModel( AccessibleObject anAccessor,
                               ValueConstraintsInstance constraints,
                               MetaInfo metaInfo,
                               Object defaultValue
    )
    {
        super( anAccessor, true, constraints, metaInfo, defaultValue );
    }

    public <T> Property<T> newInstance( Object value )
    {
        // Property was constructed using a builder

        Property property;
        property = new ValuePropertyInstance<Object>( this, value );
        return wrapProperty( property );
    }
}