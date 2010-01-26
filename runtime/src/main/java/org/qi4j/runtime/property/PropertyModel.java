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

import java.lang.reflect.Method;
import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.property.GenericPropertyInfo;
import org.qi4j.api.property.Property;
import org.qi4j.api.property.PropertyInfo;
import org.qi4j.runtime.composite.ValueConstraintsInstance;

/**
 * Implementation of Properties for Transient Composites
 */
public class PropertyModel
    extends AbstractPropertyModel
{
    private PropertyInfo propertyInfo;

    public PropertyModel( Method anAccessor, boolean immutable, ValueConstraintsInstance constraints,
                          MetaInfo metaInfo, Object anInitialValue
    )
    {
        super( anAccessor, immutable, constraints, metaInfo, anInitialValue );
        propertyInfo = new GenericPropertyInfo( metaInfo, isImmutable(), isComputed(), qualifiedName(), type() );
    }

    @SuppressWarnings( "unchecked" )
    public <T> Property<T> newInstance( Object value )
    {
        // Property was constructed using a builder

        Property property;
        if( isComputed() )
        {
            property = new ComputedPropertyInfo<Object>( propertyInfo );
        }
        else
        {
            property = new PropertyInstance<Object>( propertyInfo, value, this );
        }
        return wrapProperty( property );
    }
}
