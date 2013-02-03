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

import java.io.Serializable;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Member;
import org.qi4j.api.util.NullArgumentException;

/**
 * QualifiedName is a representation of Property names to their full declaration.
 * <p>
 * A QualifiedName is created by combining the name of a method and the name of the type that declares the method.
 * This class also contains many static utility methods to manage QualifiedName instances.
 * </p>
 * <p>
 * <strong>NOTE: Unless you do very generic libraries, entity stores and other extensions that is deeply coupled into
 * the Qi4j runtime, it is very unlikely you will need to use this class directly.</strong>
 * </p>
 * <p>
 * It is also important to notice that the QualifiedName needs to be long-term stable, as the names are written
 * to persistent storage. So any changes in the formatting <strong>must be made in a backward-compatible manner
 * </strong>.
 * </p>
 * <p>
 * The QualifiedName has two intrinsic parts, one being the {@code type} and the other the {@code name}. The
 * {@code type} comes from the class where the QualifiedName originates from and internally kept as a {@link TypeName}
 * instance. The name is the name from the method name. When the QualifiedName instance is converted to an external
 * string representation, via the offical and formal {@link #toString()} method, the {@code type} is normalized, i.e.
 * any dollar characters ($) in the name are replaced by dashes (-), to make them URI friendly.
 * </p>
 * <p>
 * QualifiedName instances are immutable, implements {@link #hashCode()} and {@link #equals(Object)} as a value
 * object and can safely be used as keys in {@link java.util.Map}.
 */
public final class QualifiedName
    implements Comparable<QualifiedName>, Serializable
{
    private final TypeName typeName;
    private final String name;

    /**
     * Creates a QualifiedName from a method.
     * <p>
     * This factory method will create a QualifiedName from the Method itself.
     *
     * </p>
     *
     * @param method Type method that returns a Property, for which the QualifiedName will be representing.
     *
     * @return A QualifiedName representing this method.
     *
     * @throws NullArgumentException If the {@code method} argument passed is null.
     */
    public static QualifiedName fromAccessor( AccessibleObject method )
    {
        NullArgumentException.validateNotNull( "method", method );
        return fromClass( ( (Member) method ).getDeclaringClass(), ( (Member) method ).getName() );
    }

    /**
     * Creates a QualifiedName instance from the Class and a given name.
     * <p>
     * This factory method converts the {@code type} to a {@link TypeName} and appends the given {@code name}.
     *
     * @param type The Class that is the base of the QualifiedName.
     * @param name The qualifier name which will be appended to the base name derived from the {@code type} argument.
     *
     * @return A QualifiedName instance representing the {@code type} and {@code name} arguments.
     *
     * @throws NullArgumentException if any of the two arguments are {@code null}, or if the name string is empty.
     */
    public static QualifiedName fromClass( Class type, String name )
    {
        return new QualifiedName( TypeName.nameOf( type ), name );
    }

    /**
     * Creates a Qualified name from a type as string and a name qualifier.
     *
     * @param type The type name as a a string, which must be properly formatted. No checks for correctly formatted
     *             type name is performed.
     * @param name The qualifier name which will be appended to the base name derived from the {@code type} argument.
     *
     * @return A QualifiedName instance representing the {@code type} and {@code name} arguments.
     *
     * @throws NullArgumentException if any of the two arguments are {@code null} or either string is empty.
     */
    public static QualifiedName fromName( String type, String name )
    {
        return new QualifiedName( TypeName.nameOf( type ), name );
    }

    /**
     * Creates a QualifiedName from the external string format of QualifiedName.
     * <p>
     * This factory method is the reverse of {@link QualifiedName#toString() }  method, and creates a new QualifiedName
     * instance from the string representation of the QualifiedName.
     * </p>
     *
     * @param fullQualifiedName The QualifiedName external string representation to be converted back into a QualifiedName
     *                      instance.
     *
     * @return The QualifiedName instance represented by the {@code qualifiedName} argument.
     *
     * @throws IllegalArgumentException If the {@code qualifiedName} argument has wrong format.
     */
    public static QualifiedName fromFQN( String fullQualifiedName )
    {
        NullArgumentException.validateNotEmpty( "qualifiedName", fullQualifiedName );
        int idx = fullQualifiedName.lastIndexOf( ":" );
        if( idx == -1 )
        {
            throw new IllegalArgumentException( "Name '" + fullQualifiedName + "' is not a qualified name" );
        }
        final String type = fullQualifiedName.substring( 0, idx );
        final String name = fullQualifiedName.substring( idx + 1 );
        return new QualifiedName( TypeName.nameOf( type ), name );
    }

    QualifiedName( TypeName typeName, String name )
    {
        NullArgumentException.validateNotNull( "typeName", typeName );
        NullArgumentException.validateNotEmpty( "name", name );
        this.typeName = typeName;
        this.name = name;
    }

    /**
     * Returns the normalized string of the type part of the QualifiedName.
     *
     * <p>
     * The normalized type name means that all dollar ($) characters have been replaced by dashes (-).
     * </p>
     *
     * @return the normalized string of the type part of the QualifiedName.
     */
    public String type()
    {
        return typeName.normalized();
    }

    /**
     * Returns the name component of the QualifiedName.
     *
     * @return the name component of the QualifiedName.
     */
    public String name()
    {
        return name;
    }

    /**
     * Returns the URI of the QualifiedName.
     *
     * <p>
     * The URI is the {@link #toNamespace()} followed by the {@code name} component.
     * <p>
     *
     * @return the URI of the QualifiedName.
     *
     * @see #toNamespace()
     */
    public String toURI()
    {
        return toNamespace() + name;
    }

    /**
     * Return the URI of the {@link TypeName} component of the QualifiedName.
     * <p>
     * The URI of the {@link TypeName} component is in the form of;
     * </p>
     * <pre>
     * "urn:qi4j:type:" normalizedClassName
     * </pre>
     * <p>
     * where {@code normalizedClassName} is the fully-qualified class name having had any dollar ($) characters replaced
     * by URI friendly dashes (-), with a trailing hash (#). Examples;
     * </p>
     * <pre>
     * urn:qi4j:type:org.qi4j.api.common.QualifiedName#
     * urn:qi4j:type:org.qi4j.samples.MyClass-MyInnerClass#
     * </pre>
     *
     * @return the URI of the {@link TypeName} component of the QualifiedName.
     */
    public String toNamespace()
    {
        return typeName.toURI() + "#";
    }

    /**
     * Return the formal and official, long-term stable, external string representation of a QualifiedName.
     * <p>
     * This returns the {@link org.qi4j.api.common.TypeName#toString()} followed by the {@code name} component.
     * </p>
     *
     * @return the formal and official, long-term stable, external string representation of a QualifiedName.
     */
    @Override
    public String toString()
    {
        return typeName + ":" + name;
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

        return name.equals( that.name ) && typeName.equals( that.typeName );
    }

    @Override
    public int hashCode()
    {
        return 31 * typeName.hashCode() + name.hashCode();
    }

    @Override
    public int compareTo( QualifiedName other )
    {
        final int result = typeName.compareTo( other.typeName );
        if( result != 0 )
        {
            return result;
        }
        return name.compareTo( other.name );
    }
}
