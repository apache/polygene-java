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

package org.qi4j.spi.entity;

import java.io.Serializable;
import org.qi4j.entity.EntityComposite;
import org.qi4j.util.ClassUtil;

/**
 * TODO
 */
public final class QualifiedIdentity
    implements Serializable
{
    private static final long serialVersionUID = 1L;

    // Associations who have been set to "null" should use this as the representation in the EntityState
    public static final QualifiedIdentity NULL = new QualifiedIdentity( "null", "none" );

    private String identity;
    private String compositeType;

    public QualifiedIdentity( EntityComposite entityComposite )
    {
        this( entityComposite.identity().get(), entityComposite.type().getName() );
    }

    public QualifiedIdentity( String identity, Class clazz )
    {
        this.identity = identity;
        this.compositeType = clazz.getName();
    }

    public QualifiedIdentity( String identity, String clazz )
    {
        this.identity = identity;
        this.compositeType = clazz;
    }

    public QualifiedIdentity( String qualifiedIdentity )
    {
        int separatorIndex = qualifiedIdentity.indexOf( ":" );
        if( separatorIndex == -1 )
        {
            throw new IllegalArgumentException( "Supplied string is not a qualified identity: " + qualifiedIdentity );
        }
        this.compositeType = qualifiedIdentity.substring( 0, separatorIndex );
        this.identity = qualifiedIdentity.substring( separatorIndex + 1 );
    }

    public String identity()
    {
        return identity;
    }

    public String type()
    {
        return compositeType;
    }

    public String toURI()
    {
        return ClassUtil.toURI( compositeType ) + "/" + identity;
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

        QualifiedIdentity that = (QualifiedIdentity) o;

        if( !compositeType.equals( that.compositeType ) )
        {
            return false;
        }
        if( !identity.equals( that.identity ) )
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        int result;
        result = identity.hashCode();
        result = 31 * result + compositeType.hashCode();
        return result;
    }

    @Override public String toString()
    {
        return compositeType + ":" + identity;
    }
}
