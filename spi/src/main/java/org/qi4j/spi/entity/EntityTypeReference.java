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

package org.qi4j.spi.entity;

import org.qi4j.api.common.TypeName;

import java.io.Serializable;

/**
 * JAVADOC
 */
public class EntityTypeReference
    implements Serializable
{
    private TypeName type;
    private String rdf;
    private String version;

    public EntityTypeReference( String ref )
    {
        String[] refs = ref.split( ":", 3 );
        version = refs[ 0 ];
        type = TypeName.nameOf( refs[ 1 ] );
        if( refs.length == 3 )
        {
            rdf = refs[ 2 ];
        }
    }

    public EntityTypeReference( TypeName type, String rdf, String version )
    {
        this.type = type;
        this.rdf = rdf;
        this.version = version;
    }

    public TypeName type()
    {
        return type;
    }

    public String rdf()
    {
        return rdf;
    }

    public String version()
    {
        return version;
    }

    @Override
    public String toString()
    {
        return version + ":" + type + ( rdf == null ? "" : ":" + rdf );
    }

    public String toUri()
    {
        return "urn:qi4j:type:" + version + ":" + type + ( rdf == null ? "" : ":" + rdf );
    }

    @Override
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

        EntityTypeReference that = (EntityTypeReference) o;

        if( !version.equals( that.version ) )
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        return version.hashCode();
    }
}
