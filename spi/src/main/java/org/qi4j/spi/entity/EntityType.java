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

import static org.qi4j.api.common.TypeName.nameOf;
import org.qi4j.spi.entity.association.AssociationType;
import org.qi4j.spi.entity.association.ManyAssociationType;
import org.qi4j.spi.property.PropertyType;

import java.io.Serializable;

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
    private final String type;
    private final String version;
    private final String uri;
    private final boolean queryable;
    private final Iterable<PropertyType> properties;
    private final Iterable<AssociationType> associations;
    private final Iterable<ManyAssociationType> manyAssociations;
    private final Iterable<String> mixinTypes;

    public EntityType( final String entityType,
                       final String uri,
                       final boolean queryable,
                       final Iterable<String> mixinTypes,
                       final Iterable<PropertyType> properties,
                       final Iterable<AssociationType> associations,
                       final Iterable<ManyAssociationType> manyAssociations )
    {
        this.type = entityType;
        this.uri = uri;
        this.queryable = queryable;
        this.mixinTypes = mixinTypes;
        this.properties = properties;
        this.associations = associations;
        this.manyAssociations = manyAssociations;
        this.version = calculateSchemaVersion();

    }

    public String type()
    {
        return type;
    }

    public Iterable<String> mixinTypes()
    {
        return mixinTypes;
    }

    public String version()
    {
        return version;
    }

    public boolean isSameVersion( EntityType type )
    {
        return version.equals( type.version );
    }

    public String toURI()
    {
        return uri;
    }

    public boolean queryable()
    {
        return queryable;
    }

    public Iterable<PropertyType> properties()
    {
        return properties;
    }

    public Iterable<AssociationType> associations()
    {
        return associations;
    }

    public Iterable<ManyAssociationType> manyAssociations()
    {
        return manyAssociations;
    }

    @Override public String toString()
    {
        return type + "(" + version + ")";
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
        return version.equals( that.version ) && type.equals( that.type );
    }

    private String calculateSchemaVersion()
    {
        try
        {
            final SchemaVersion schemaVersion = new SchemaVersion();

            // Entity type
            schemaVersion.versionize( nameOf( type ) );

            // Properties
            for( PropertyType property : properties )
            {
                property.versionize( schemaVersion );
            }

            // Associations
            for( AssociationType association : associations )
            {
                association.versionize( schemaVersion );
            }

            // ManyAssociations
            for( ManyAssociationType manyAssociation : manyAssociations )
            {
                manyAssociation.versionize( schemaVersion );
            }


            return schemaVersion.base64();
        }
        catch( Exception e )
        {
            e.printStackTrace();
            return "";
        }
    }

}
