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

package org.qi4j.api.property;

import java.math.BigDecimal;

/**
 * Convenience class for mathematical operations on numerical properties.
 * <pre>import static org.qi4j.api.property.Numbers.*;
 * ...
 * add( object.numberProperty(), 5 );</pre>
 */
public final class Numbers
{
    // Integer operations

    public static Property<Integer> add( Property<Integer> property, int amount )
    {
        property.set( property.get() + amount );
        return property;
    }

    public static Property<Integer> mult( Property<Integer> property, int amount )
    {
        property.set( property.get() * amount );
        return property;
    }

    public static Property<Integer> sub( Property<Integer> property, int amount )
    {
        property.set( property.get() - amount );
        return property;
    }

    public static Property<Integer> div( Property<Integer> property, int amount )
    {
        property.set( property.get() / amount );
        return property;
    }

    // Long operations

    public static Property<Long> add( Property<Long> property, long amount )
    {
        property.set( property.get() + amount );
        return property;
    }

    public static Property<Long> mult( Property<Long> property, long amount )
    {
        property.set( property.get() * amount );
        return property;
    }

    public static Property<Long> sub( Property<Long> property, long amount )
    {
        property.set( property.get() - amount );
        return property;
    }

    public static Property<Long> div( Property<Long> property, long amount )
    {
        property.set( property.get() / amount );
        return property;
    }

    // Double operations

    public static Property<Double> add( Property<Double> property, double amount )
    {
        property.set( property.get() + amount );
        return property;
    }

    public static Property<Double> mult( Property<Double> property, double amount )
    {
        property.set( property.get() * amount );
        return property;
    }

    public static Property<Double> sub( Property<Double> property, double amount )
    {
        property.set( property.get() - amount );
        return property;
    }

    public static Property<Double> div( Property<Double> property, double amount )
    {
        property.set( property.get() / amount );
        return property;
    }

    // Float operations

    public static Property<Float> add( Property<Float> property, float amount )
    {
        property.set( property.get() + amount );
        return property;
    }

    public static Property<Float> mult( Property<Float> property, float amount )
    {
        property.set( property.get() * amount );
        return property;
    }

    public static Property<Float> sub( Property<Float> property, float amount )
    {
        property.set( property.get() - amount );
        return property;
    }

    public static Property<Float> div( Property<Float> property, float amount )
    {
        property.set( property.get() / amount );
        return property;
    }

    // BigDecimal operations

    public static Property<BigDecimal> add( Property<BigDecimal> property, BigDecimal amount )
    {
        property.set( property.get().add( amount ) );
        return property;
    }

    public static Property<BigDecimal> mult( Property<BigDecimal> property, BigDecimal amount )
    {
        property.set( property.get().multiply( amount ) );
        return property;
    }

    public static Property<BigDecimal> sub( Property<BigDecimal> property, BigDecimal amount )
    {
        property.set( property.get().subtract( amount ) );
        return property;
    }

    public static Property<BigDecimal> div( Property<BigDecimal> property, BigDecimal amount )
    {
        property.set( property.get().divide( amount ) );
        return property;
    }
}
