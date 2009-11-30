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

package org.qi4j.runtime.types;

import java.io.Serializable;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.spi.property.PropertyType;
import org.qi4j.spi.property.ValueType;

/**
 * JAVADOC
 */
public final class PropertyTypeImpl
    implements Serializable, Comparable<PropertyType>, PropertyType
{

    private final QualifiedName qualifiedName;
    private final ValueType type;
    private final boolean queryable;
    private final PropertyTypeEnum propertyType;

    public PropertyTypeImpl( final QualifiedName qualifiedName,
                             final ValueType type,
                             final boolean queryable,
                             final PropertyTypeEnum propertyType
    )
    {
        this.qualifiedName = qualifiedName;
        this.type = type;
        this.queryable = queryable;
        this.propertyType = propertyType;
    }

    public QualifiedName qualifiedName()
    {
        return qualifiedName;
    }

    public ValueType type()
    {
        return type;
    }

    public PropertyTypeEnum propertyType()
    {
        return propertyType;
    }

    public boolean queryable()
    {
        return queryable;
    }

    @Override
    public String toString()
    {
        return qualifiedName + "(" + type + ")";
    }

    public int compareTo( PropertyType pt )
    {
        return qualifiedName.compareTo( pt.qualifiedName() );
    }
}
