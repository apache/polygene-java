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
package org.qi4j.spi.property;

import java.lang.reflect.Type;
import static org.qi4j.composite.NullArgumentException.*;
import org.qi4j.property.Property;
import org.qi4j.property.PropertyInfo;
import org.qi4j.property.PropertyVetoException;

/**
 * {@code ComputedPropertyInstance} is the base implementation of {@link Property}.
 *
 * @author Niclas Hedhman
 * @since 0.1.0
 */
public abstract class ComputedPropertyInstance<T>
    implements Property<T>
{
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
     * Throws {@link PropertyVetoException} exception.
     *
     * @param anIgnoredValue This value is ignored.
     * @throws PropertyVetoException Thrown by default.
     * @since 0.1.0
     */
    public T set( T anIgnoredValue )
        throws PropertyVetoException
    {
        String qualifiedName = getQualifiedName();
        throw new PropertyVetoException( "Property [" + qualifiedName + "] is read-only" );
    }

    /**
     * Returns the property info given {@code anInfoType} argument.
     *
     * @param anInfoType The info type.
     * @return Property info given {@code anInfoType} argument.
     * @since 0.1.0
     */
    public final <T> T getPropertyInfo( Class<T> anInfoType )
    {
        return propertyInfo.getPropertyInfo( anInfoType );
    }

    /**
     * Returns the property name. Must not return {@code null}.
     *
     * @return The property name.
     * @since 0.1.0
     */
    public final String getName()
    {
        return propertyInfo.getName();
    }

    /**
     * Returns the qualified name of this {@code Property}. Must not return {@code null}.
     *
     * @return The qualified name of this {@code Property}.
     * @since 0.1.0
     */
    public final String getQualifiedName()
    {
        return propertyInfo.getQualifiedName();
    }

    public Type getPropertyType()
    {
        return propertyInfo.getPropertyType();
    }

    /**
     * Perform equals with {@code o} argument.
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

        return propertyInfo.equals( that.propertyInfo );
    }

    /**
     * Calculate hash code.
     *
     * @return the hashcode of this {@code ComputedPropertyInstance} instance.
     * @since 0.1.0
     */
    public int hashCode()
    {
        return ( propertyInfo != null ? propertyInfo.hashCode() : 0 );
    }

    /**
     * Get the property info implementation
     *
     * @return the property implementation
     */
    public PropertyInfo getPropertyInfo()
    {
        return propertyInfo;
    }
}