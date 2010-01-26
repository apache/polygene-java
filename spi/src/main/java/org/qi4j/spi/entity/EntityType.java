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

package org.qi4j.spi.entity;

import java.io.Serializable;
import java.util.Set;
import org.qi4j.api.common.TypeName;
import org.qi4j.spi.entity.association.AssociationType;
import org.qi4j.spi.entity.association.ManyAssociationType;
import org.qi4j.spi.property.PropertyType;

/**
 * SPI-level description of an Entity type. This contains
 * all metainformation about an Entity, including its properties,
 * associations and many-associations. Also contains functions
 * for calculating the version, so that Entity types can evolve
 * safely.
 */
public final class EntityType
    implements Serializable
{
    private final TypeName type;
    private final String uri;
    private final boolean queryable;
    private final Set<PropertyType> properties;
    private final Set<AssociationType> associations;
    private final Set<ManyAssociationType> manyAssociations;
    private final Set<String> mixinTypes;

    public EntityType( final TypeName entityType,
                       final boolean queryable,
                       final Set<String> mixinTypes,
                       final Set<PropertyType> properties,
                       final Set<AssociationType> associations,
                       final Set<ManyAssociationType> manyAssociations
    )
    {
        this.type = entityType;
        this.queryable = queryable;
        this.mixinTypes = mixinTypes;
        this.properties = properties;
        this.associations = associations;
        this.manyAssociations = manyAssociations;
        this.uri = "urn:qi4j:type:" + entityType.normalized();
    }

    public TypeName type()
    {
        return type;
    }

    public Set<String> mixinTypes()
    {
        return mixinTypes;
    }

    public String uri()
    {
        return uri;
    }

    public boolean queryable()
    {
        return queryable;
    }

    public Set<PropertyType> properties()
    {
        return properties;
    }

    public Set<AssociationType> associations()
    {
        return associations;
    }

    public Set<ManyAssociationType> manyAssociations()
    {
        return manyAssociations;
    }

    @Override
    public String toString()
    {
        return type.toString();
    }

    public int hashCode()
    {
        return type.hashCode();
    }

    public boolean equals( Object o )
    {
        if( this == o )
        {
            return true;
        }
        if( o == null || getClass() != o.getClass() )
        {
            return false;
        }

        EntityType that = (EntityType) o;
        return type.equals( that.type );
    }
}
