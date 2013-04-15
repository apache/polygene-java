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
import org.qi4j.api.Qi4j;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.util.Classes;

import static org.qi4j.functional.Iterables.first;

/**
 * A Qualified Identity is the combination of the Composite type name and the identity of a specific
 * Entity instance. When stringified, these two are separated by the ":" character. Example:
 * <pre>
 * com.mycompany.mydomain.SomeEntity:123456
 * </pre>
 * where "com.mycompany.mydomain.SomeEntity" is the Composite type, and "123456" is the identity.
 */
public final class QualifiedIdentity
    implements Serializable
{
    public static QualifiedIdentity parseURI( String uri )
    {
        String str = uri.substring( "urn:qi4j:entity:".length() );
        int idx = str.indexOf( "/" );
        String type = str.substring( 0, idx ).replace( "-", "$" );
        String identity = str.substring( idx + 1 );
        return new QualifiedIdentity( identity, type );
    }

    public static QualifiedIdentity parseQualifiedIdentity( String id )
    {
        return new QualifiedIdentity( id );
    }

    public static QualifiedIdentity qualifiedIdentityOf( Object o )
    {
        return new QualifiedIdentity( (EntityComposite) o );
    }

    private static final long serialVersionUID = 1L;

    // Associations who have been set to "null" should use this as the representation in the EntityState
    public static final QualifiedIdentity NULL = new QualifiedIdentity( "null", "none" );

    private String identity;
    private String compositeType;

    public QualifiedIdentity( EntityComposite entityComposite )
    {
        this( entityComposite.identity().get(), first( Qi4j.FUNCTION_DESCRIPTOR_FOR
                                                           .map( entityComposite )
                                                           .types() ).getName() );
    }

    public QualifiedIdentity( String identity, Class<?> clazz )
    {
        this.identity = identity;
        this.compositeType = clazz.getName();
    }

    public QualifiedIdentity( String identity, String clazz )
    {
        this.identity = identity;
        this.compositeType = clazz;
    }

    protected QualifiedIdentity( String qualifiedIdentity )
    {
        int separatorIndex = qualifiedIdentity.indexOf( ":" );
        if( separatorIndex == -1 )
        {
            throw new IllegalArgumentException( "Supplied string is not a qualified identity: " + qualifiedIdentity );
        }
        this.compositeType = qualifiedIdentity.substring( 0, separatorIndex );
        this.identity = qualifiedIdentity.substring( separatorIndex + 1 );
    }

    public final String identity()
    {
        return identity;
    }

    public final String type()
    {
        return compositeType;
    }

    public final String toURI()
    {
        return "urn:qi4j:entity:" + Classes.normalizeClassToURI( compositeType ) + "/" + identity;
    }

    @Override
    @SuppressWarnings( "AccessingNonPublicFieldOfAnotherObject" )
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
        return compositeType.equals( that.compositeType ) && identity.equals( that.identity );
    }

    @Override
    public int hashCode()
    {
        int result;
        result = identity.hashCode();
        result = 31 * result + compositeType.hashCode();
        return result;
    }

    @Override
    public String toString()
    {
        return compositeType + ":" + identity;
    }
}
