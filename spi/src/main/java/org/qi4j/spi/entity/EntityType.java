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

/**
 * TODO
 */
public class EntityType
    implements Serializable
{
    private String type;
    private long version = 0L; // TODO How to compute this?
    private String uri;
    private Iterable<PropertyType> properties;
    private Iterable<AssociationType> associations;
    private Iterable<ManyAssociationType> manyAssociations;
    private Iterable<String> mixinTypes;

    public EntityType( String entityType, String uri, Iterable<String> mixinTypes,
                       Iterable<PropertyType> properties, Iterable<AssociationType> associations, Iterable<ManyAssociationType> manyAssociations )
    {
        this.uri = uri;
        this.mixinTypes = mixinTypes;
        this.properties = properties;
        this.associations = associations;
        this.manyAssociations = manyAssociations;
        type = entityType;
    }

    public String type()
    {
        return type;
    }

    public Iterable<String> mixinTypes()
    {
        return mixinTypes;
    }

    public long version()
    {
        return version;
    }

    public String toURI()
    {
        return uri;
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
        int result;
        result = type.hashCode();
        result = 31 * result + (int) ( version ^ ( version >>> 32 ) );
        return result;
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

        if( version != that.version )
        {
            return false;
        }
        if( !type.equals( that.type ) )
        {
            return false;
        }

        return true;
    }
}
