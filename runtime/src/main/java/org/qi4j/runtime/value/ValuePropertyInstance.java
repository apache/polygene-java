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
package org.qi4j.runtime.value;

import org.qi4j.api.property.AbstractPropertyInstance;
import org.qi4j.api.property.PropertyInfo;

/**
 * {@code ValuePropertyInstance} represents a ValueComposite property. It is always immutable.
 */
public final class ValuePropertyInstance<T>
    extends AbstractPropertyInstance<T>
{
    private T value;

    /**
     * Construct an instance of {@code PropertyInstance} with the specified arguments.
     *
     * @param aPropertyInfo The property info. This argument must not be {@code null}.
     * @param aValue        The property value.
     *
     * @throws IllegalArgumentException Thrown if the specified {@code aPropertyInfo} is {@code null}.
     */
    public ValuePropertyInstance( PropertyInfo aPropertyInfo, T aValue )
        throws IllegalArgumentException
    {
        super( aPropertyInfo );
        value = aValue;
    }

    /**
     * Returns this property value.
     *
     * @return This property value.
     */
    public T get()
    {
        return value;
    }

    /**
     * Sets this property value.
     *
     * @param aNewValue The new value.
     */
    public void set( T aNewValue )
    {
        throw new IllegalStateException( "Property [" + qualifiedName() + "] is immutable." );
    }

    /**
     * Returns the value as string.
     *
     * @return The value as string.
     */
    @Override
    public String toString()
    {
        return value == null ? "" : value.toString();
    }
}