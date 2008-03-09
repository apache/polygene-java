/*  Copyright 2008 Edward Yakop.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.entity.ibatis;

import org.qi4j.property.PropertyInfo;
import org.qi4j.spi.property.PropertyInstance;

/**
 * {@code IBatisMutablePropertyInstance} represents a mutable property.
 *
 * @author edward.yakop@gmail.com
 * @since 0.1.0
 */
final class IBatisMutablePropertyInstance<T> extends PropertyInstance<T>
{
    private boolean isDirty;

    /**
     * Construct an instance of {@code MutableProperty}.
     *
     * @param aPropertyInfo  The property info. This argument must not be {@code null}.
     * @param anInitialValue The initial value. This argument must not be {@code null}.
     * @throws IllegalArgumentException Thrown if the specified {@code aPropertyInfo} argument is {@code null}.
     * @since 0.1.0
     */
    IBatisMutablePropertyInstance( PropertyInfo aPropertyInfo, T anInitialValue )
        throws IllegalArgumentException
    {
        super( aPropertyInfo, anInitialValue );
        isDirty = false;
    }

    public void set( T aNewValue )
    {
        if( !isDirty )
        {
            isDirty = isNotEquals( value, aNewValue );
        }

        super.set( aNewValue );
    }

    /**
     * Returns {@code true} if both arguments are equals.
     *
     * @param aValue       A value.
     * @param anotherValue Another value.
     * @return A {@code boolean} indicator whether both arguments are equal.
     * @since 0.1.0
     */
    static boolean isNotEquals( Object aValue, Object anotherValue )
    {
        if( aValue == anotherValue )
        {
            return false;
        }

        return ( aValue != null ) ? !aValue.equals( anotherValue ) : !anotherValue.equals( aValue );
    }

    /**
     * Returns {@code true} if this {@code IBatisMutablePropertyInstance} instance is dirty, {@code false} otherwise.
     *
     * @return A {@code boolean} indicator whether this {@code IBatisMutablePropertyInstance} instance is dirty.
     * @since 0.1.0
     */
    final boolean isDirty()
    {
        return isDirty;
    }

    /**
     * Marks this {@code IBatisMutablePropertyInstance} clean.
     *
     * @since 0.1.0
     */
    final void markAsClean()
    {
        isDirty = false;
    }
}
