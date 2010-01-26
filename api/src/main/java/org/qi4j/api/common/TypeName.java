package org.qi4j.api.common;

import java.io.Serializable;
import java.lang.reflect.Type;
import org.qi4j.api.util.Classes;
import org.qi4j.api.util.NullArgumentException;

public final class TypeName
    implements Serializable, Comparable<TypeName>
{
    private final String name;

    public static TypeName nameOf( Class type )
    {
        NullArgumentException.validateNotNull( "type", type );
        return new TypeName( type.getName() );
    }

    public static TypeName nameOf( Type type )
    {
        return nameOf( Classes.getRawClass( type ) );
    }

    public static TypeName nameOf( String typeName )
    {
        return new TypeName( typeName );
    }

    private TypeName( String name )
    {
        NullArgumentException.validateNotEmpty( "name", name );
        this.name = name;
    }

    public String normalized()
    {
        return Classes.normalizeClassToURI( name );
    }

    public String toURI()
    {
        return Classes.toURI( name );
    }

    public String name()
    {
        return name;
    }

    @Override
    public String toString()
    {
        return name;
    }

    public boolean isClass( final Class<?> type )
    {
        return type.getName().equals( name );
    }

    public boolean equals( final Object o )
    {
        if( this == o )
        {
            return true;
        }
        if( o == null || getClass() != o.getClass() )
        {
            return false;
        }

        final TypeName other = (TypeName) o;

        return name.equals( other.name );
    }

    public int hashCode()
    {
        return name.hashCode();
    }

    public int compareTo( final TypeName typeName )
    {
        return this.name.compareTo( typeName.name );
    }
}

