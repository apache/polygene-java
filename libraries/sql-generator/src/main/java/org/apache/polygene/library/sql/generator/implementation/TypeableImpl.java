/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package org.apache.polygene.library.sql.generator.implementation;

import org.apache.polygene.library.sql.generator.Typeable;

/**
 * This is base class for all classes implementing sub-types of {@link Typeable}. It is meant to be extended by actual
 * domain-specific classes implementing the API interfaces/classes inherited from {@link Typeable}. For simple usecases,
 * typically {@code <ImplementedType>} and {@code <RealImplementedType>} are both the same.
 *
 * @param <ImplementedType>     The base type for API interface or class.
 * @param <RealImplementedType> The actual, possibly extended in other project, type of API interface or class. This is
 *                              useful when one has a complex and extendable type hierarchy and wants the implementation class to be
 *                              extendable as well.
 * @author Stanislav Muhametsin
 */
public abstract class TypeableImpl<ImplementedType extends Typeable<?>, RealImplementedType extends ImplementedType>
    implements Typeable<ImplementedType>
{

    /**
     * The implemented API type of this object.
     */
    private final Class<? extends RealImplementedType> _expressionClass;

    /**
     * Constructs new base implementation for {@link Typeable}.
     *
     * @param realImplementingType The API type, which this specific instance implements. Must not be null.
     * @throws IllegalArgumentException If the {@code realImplementingType} is {@code null}.
     */
    protected TypeableImpl( Class<? extends RealImplementedType> realImplementingType )
    {
        if( realImplementingType == null )
        {
            throw new IllegalArgumentException( "The implemented type can not be null." );
        }

        this._expressionClass = realImplementingType;
    }

    /**
     */
    public Class<? extends RealImplementedType> getImplementedType()
    {
        return this._expressionClass;
    }

    /**
     * This {@link Typeable} equals to {@code obj}, when {@code this} == {@code obj}, OR when {@code obj} is not null,
     * and {@code obj} is instance of {@link Typeable}, and this.{@link #getImplementedType()} returns SAME type as
     * {@code obj}. {@link #getImplementedType()}, and {@code this}.{@link #doesEqual(Typeable)} returns {@code true}
     * when given {@code obj}.
     */
    @Override
    public boolean equals( Object obj )
    {
        return this == obj
               || ( obj != null && obj instanceof Typeable<?>
                    && this.getImplementedType().equals( ( (Typeable<?>) obj ).getImplementedType() ) && this
                        .doesEqual( this._expressionClass.cast( obj ) ) );
    }

    /**
     * Override this method to test equaling to other objects implementing same API type as this.
     *
     * @param another Another object implementing same API type as {@code this}. Is guaranteed to be non-null and
     *                different reference than {@code this}, when called by {@link Typeable#equals(Object)}.
     * @return {@code true} if this object equals to {@code another}; false otherwise.
     */
    protected abstract boolean doesEqual( RealImplementedType another );

    /**
     * Helper method to check that either both arguments are {@code null}s, or one is non-null and equals to another.
     * Note that this method fails if {@code one.equals(another)} fails when {@code another} is null.
     *
     * @param <T>     The type of objects.
     * @param one     First object to check.
     * @param another Second object to check.
     * @return {@code (one == null && another == null) || (one != null && one.equals( another ))}.
     */
    public static final <T> boolean bothNullOrEquals( T one, T another )
    {
        return ( one == null && another == null ) || ( one != null && one.equals( another ) );
    }
}