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

package org.qi4j.api.common;

import java.lang.reflect.Method;
import java.io.Serializable;
import org.qi4j.api.util.Classes;

/**
 * A QualifiedName is created by combining the name of a method and the
 * name of the type that declares the method.
 */
public class QualifiedName
    implements Comparable<QualifiedName>, Serializable
{
    private String type;
    private String name;

    public QualifiedName( Method accessor)
    {
        this(accessor.getDeclaringClass(), accessor.getName());
    }

    public QualifiedName(Class declaringClass, String name)
    {
        this(declaringClass.getName(), name);
    }

    public QualifiedName( String type, String name )
    {
        this.type = type.replace( '$', '-' );

        this.type = type;
        this.name = name;
    }

    public QualifiedName(String qualifiedName)
    {
        int idx = qualifiedName.lastIndexOf( ":" );
        if (idx == -1)
        {
            throw new IllegalArgumentException("Name '"+qualifiedName+"' is not a qualified name");
        }
        else
        {
            name = qualifiedName.substring( idx + 1 );
            type = qualifiedName.substring( 0, idx );
        }
    }

    public String type()
    {
        return type;
    }

    public String name()
    {
        return name;
    }

    public String toURI()
    {
        return "urn:qi4j:entitytype:" + type+"#"+name;
    }

    public String toNamespace()
    {
        return "urn:qi4j:entitytype:"
               + Classes.normalizeClassToURI( type )
               + "#";
    }

    @Override public String toString()
    {
        return type+":"+name;
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

        QualifiedName that = (QualifiedName) o;

        if( !name.equals( that.name ) )
        {
            return false;
        }
        if( type != null ? !type.equals( that.type ) : that.type != null )
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + name.hashCode();
        return result;
    }

    public int compareTo( QualifiedName o )
    {
        return toString().compareTo( o.toString() );
    }
}
