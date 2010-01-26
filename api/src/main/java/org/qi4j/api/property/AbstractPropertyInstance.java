/*
 * Copyright 2008 Niclas Hedhman.
 * Copyright 2008 Edward Yakop.
 * Copyright 2008 Rickard Öberg.
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
package org.qi4j.api.property;

import java.lang.reflect.Type;
import org.qi4j.api.common.QualifiedName;

import static org.qi4j.api.util.NullArgumentException.*;

/**
 * {@code AbstractPropertyInstance} is the base implementation of {@link org.qi4j.api.property.Property}.
 */
public abstract class AbstractPropertyInstance<T>
    implements Property<T>
{
    protected final PropertyInfo propertyInfo;

    /**
     * Construct an instance of {@code ComputedPropertyInstance}.
     *
     * @param aPropertyInfo The property info. This argument must not be {@code null}.
     *
     * @throws IllegalArgumentException Thrown if the specified {@code aPropertyInfo} argument is {@code null}.
     */
    protected AbstractPropertyInstance( PropertyInfo aPropertyInfo )
        throws IllegalArgumentException
    {
        validateNotNull( "aPropertyInfo", aPropertyInfo );
        propertyInfo = aPropertyInfo;
    }

    /**
     * Returns the property info given {@code anInfoType} argument.
     *
     * @param anInfoType The info type.
     *
     * @return Property info given {@code anInfoType} argument.
     */
    // Was it a mistake to have another T here? (I think so...)
    public final <V> V metaInfo( Class<V> anInfoType )
    {
        return propertyInfo.metaInfo( anInfoType );
    }

    /**
     * Returns the qualified name of this {@code Property}. Must not return {@code null}.
     *
     * @return The qualified name of this {@code Property}.
     */
    public final QualifiedName qualifiedName()
    {
        return propertyInfo.qualifiedName();
    }

    public final Type type()
    {
        return propertyInfo.type();
    }

    public boolean isImmutable()
    {
        return propertyInfo.isImmutable();
    }

    public boolean isComputed()
    {
        return propertyInfo.isComputed();
    }

    /**
     * Perform equals with {@code o} argument.
     * <p/>
     * The definition of equals() for the ComputedProperty is that if the Value, subclass and all the metaInfo are
     * equal, then th
     *
     * @param o The other object to compare.
     *
     * @return Returns a {@code boolean} indicator whether this object is equals the other.
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

        Property<?> that = (Property<?>) o;

        if( !type().equals( that.type() ) )
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
     */
    public int hashCode()
    {
        int hash = getClass().hashCode();
        if( propertyInfo != null )
        {
            hash = propertyInfo.type().hashCode();
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