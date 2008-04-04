/*
 * Copyright 2008 Niclas Hedhman.
 * Copyright 2008 Edward Yakop.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.qi4j.property;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import static org.qi4j.composite.NullArgumentException.*;

/**
 * {@code ComputedPropertyInstance} is the base implementation of {@link Property}.
 *
 * @author Niclas Hedhman
 * @since 0.1.0
 */
public abstract class ComputedPropertyInstance<T>
    implements Property<T>
{
    public static String getName( String qualifiedName )
    {
        int idx = qualifiedName.lastIndexOf( ":" );
        return qualifiedName.substring( idx + 1 );
    }

    public static String getDeclaringClassName( String qualifiedName )
    {
        int idx = qualifiedName.lastIndexOf( ":" );
        return qualifiedName.substring( 0, idx + 1 );
    }

    public static String getQualifiedName( Method accessor )
    {
        String className = accessor.getDeclaringClass().getName();
        className = className.replace( '$', '&' );
        return className + ":" + accessor.getName();
    }

    public static String getQualifiedName( Class declaringClass, String name )
    {
        String className = declaringClass.getName();
        className = className.replace( '$', '&' );
        return className + ":" + name;
    }

    public static Type getPropertyType( Method accessor )
    {
        return getPropertyType( accessor.getGenericReturnType() );
    }

    public static Type getPropertyType( Type methodReturnType )
    {
        if( methodReturnType instanceof ParameterizedType )
        {
            ParameterizedType parameterizedType = (ParameterizedType) methodReturnType;
            if( Property.class.isAssignableFrom( (Class<?>) parameterizedType.getRawType() ) )
            {
                return parameterizedType.getActualTypeArguments()[ 0 ];
            }
        }

        Type[] interfaces = ( (Class) methodReturnType ).getInterfaces();
        for( Type anInterface : interfaces )
        {
            Type propertyType = getPropertyType( anInterface );
            if( propertyType != null )
            {
                return propertyType;
            }
        }
        return null;
    }

    protected PropertyInfo propertyInfo;

    /**
     * Construct an instance of {@code ComputedPropertyInstance}.
     *
     * @param aPropertyInfo The property info. This argument must not be {@code null}.
     * @throws IllegalArgumentException Thrown if the specified {@code aPropertyInfo} argument is {@code null}.
     * @since 0.1.0
     */
    protected ComputedPropertyInstance( PropertyInfo aPropertyInfo )
        throws IllegalArgumentException
    {
        validateNotNull( "aPropertyInfo", aPropertyInfo );
        propertyInfo = aPropertyInfo;
    }

    /**
     * Returns {@code null} by default.
     *
     * @return Returns null by default.
     * @since 0.1.0
     */
    public T get()
    {
        return null;
    }

    /**
     * Throws {@link IllegalArgumentException} exception.
     *
     * @param anIgnoredValue This value is ignored.
     * @throws IllegalArgumentException Thrown by default.
     * @since 0.1.0
     */
    public void set( T anIgnoredValue )
        throws IllegalArgumentException
    {
        String qualifiedName = qualifiedName();
        throw new IllegalArgumentException( "Property [" + qualifiedName + "] is read-only" );
    }

    /**
     * Returns the property info given {@code anInfoType} argument.
     *
     * @param anInfoType The info type.
     * @return Property info given {@code anInfoType} argument.
     * @since 0.1.0
     */
    public final <T> T metaInfo( Class<T> anInfoType )
    {
        return propertyInfo.metaInfo( anInfoType );
    }

    /**
     * Returns the property name. Must not return {@code null}.
     *
     * @return The property name.
     * @since 0.1.0
     */
    public final String name()
    {
        return propertyInfo.name();
    }

    /**
     * Returns the qualified name of this {@code Property}. Must not return {@code null}.
     *
     * @return The qualified name of this {@code Property}.
     * @since 0.1.0
     */
    public final String qualifiedName()
    {
        return propertyInfo.qualifiedName();
    }

    public Type type()
    {
        return propertyInfo.type();
    }

    /**
     * Perform equals with {@code o} argument.
     *
     * The definition of equals() for the ComputedProperty is that if the Value, subclass and all the metaInfo are
     * equal, then th
     *
     * @param o The other object to compare.
     * @return Returns a {@code boolean} indicator whether this object is equals the other.
     * @since 0.1.0
     */
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

        ComputedPropertyInstance that = (ComputedPropertyInstance) o;

        if( !propertyInfo.equals( that.propertyInfo ) )
        {
            return false;
        }
        T value = get();
        if( value == null )
        {
            return that.get() == null;
        }
        return value.equals( that.get() );
    }

    /**
     * Calculate hash code.
     *
     * @return the hashcode of this {@code ComputedPropertyInstance} instance.
     * @since 0.1.0
     */
    public int hashCode()
    {
        int hash = getClass().hashCode();
        if( propertyInfo != null )
        {
            hash = propertyInfo.hashCode();
        }
        hash = hash * 19;
        T value = get();
        if( value != null )
        {
            hash = hash + value.hashCode() * 13;
        }
        return hash;
    }

}