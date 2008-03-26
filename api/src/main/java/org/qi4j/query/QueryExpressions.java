/*
 * Copyright 2007 Rickard Öberg.
 * Copyright 2007 Niclas Hedhman.
 * Copyright 2008 Alin Dreghiciu.
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
 *
 */
package org.qi4j.query;

import org.qi4j.property.Property;
import org.qi4j.query.el.And;
import org.qi4j.query.el.BooleanExpression;
import org.qi4j.query.el.Equals;
import org.qi4j.query.el.GreaterOrEqual;
import org.qi4j.query.el.GreaterThan;
import org.qi4j.query.el.IsNotNull;
import org.qi4j.query.el.IsNull;
import org.qi4j.query.el.LessOrEqual;
import org.qi4j.query.el.LessThan;
import org.qi4j.query.el.Not;
import org.qi4j.query.el.NotEquals;
import org.qi4j.query.el.Or;
import org.qi4j.query.el.PropertyExpression;
import org.qi4j.query.el.TypedValueExpression;
import org.qi4j.query.el.VariableValue;

/**
 * Static factory methods for query expressions and operators.
 */
public class QueryExpressions
{

    /**
     * {@link Equals} factory method.
     *
     * @param property filtered property; cannot be null
     * @param value    expected value that property is equal to; cannot be null
     * @return an {@link Equals} expression
     * @throws IllegalArgumentException - If property or value are null
     */
    public static <T> Equals<T> equals( final Property<T> property,
                                        final T value )
    {
        return new Equals<T>( asPropertyExpression( property ), asTypedValueExpression( value ) );
    }

    /**
     * {@link Equals} factory method.
     *
     * @param property filtered property; cannot be null
     * @param value    expected value that property is equal to; cannot be null
     * @return an {@link Equals} expression
     * @throws IllegalArgumentException - If property or value are null
     */
    public static <T> Equals<T> equals( final Property<T> property,
                                        final VariableValue<T> value )
    {
        return new Equals<T>( asPropertyExpression( property ), value );
    }

    /**
     * {@link VariableValue} factory method.
     *
     * @param name variable name; cannot be null
     * @return an {@link VariableValue} expression
     * @throws IllegalArgumentException - If name is null or empty
     */
    public static <T> VariableValue<T> variable( final String name )
    {
        return new VariableValue<T>( name );
    }

    /**
     * {@link IsNull} factory method.
     *
     * @param property filtered property; cannot be null
     * @return an {@link IsNull} expression
     * @throws IllegalArgumentException - If property or value are null
     */
    public static IsNull isNull( final Property property )
    {
        return new IsNull( asPropertyExpression( property ) );
    }

    /**
     * {@link IsNotNull} factory method.
     *
     * @param property filtered property; cannot be null
     * @return an {@link IsNotNull} expression
     * @throws IllegalArgumentException - If propertyis null
     */
    public static IsNotNull isNotNull( final Property property )
    {
        return new IsNotNull( asPropertyExpression( property ) );
    }

    /**
     * {@link NotEquals} factory method.
     *
     * @param property filtered property; cannot be null
     * @param value    expected value that property is not equal to; cannot be null
     * @return an {@link NotEquals} expression
     * @throws IllegalArgumentException - If property or value are null
     */
    public static <T> NotEquals<T> notEquals( final Property<T> property,
                                              final T value )
    {
        return new NotEquals<T>( asPropertyExpression( property ), asTypedValueExpression( value ) );
    }


    /**
     * {@link LessThan} factory method.
     *
     * @param property filtered property; cannot be null
     * @param value    expected value that property is less than; cannot be null
     * @return an {@link LessThan} expression
     * @throws IllegalArgumentException - If property or value are null
     */
    public static <T> LessThan<T> lt( final Property<T> property,
                                      final T value )
    {
        return new LessThan<T>( asPropertyExpression( property ), asTypedValueExpression( value ) );
    }

    /**
     * {@link LessOrEqual} factory method.
     *
     * @param property filtered property; cannot be null
     * @param value    expected value that property is less than or equal to; cannot be null
     * @return an {@link LessOrEqual} expression
     * @throws IllegalArgumentException - If property or value are null
     */
    public static <T> LessOrEqual<T> le( final Property<T> property,
                                         final T value )
    {
        return new LessOrEqual<T>( asPropertyExpression( property ), asTypedValueExpression( value ) );
    }

    /**
     * {@link GreaterThan} factory method.
     *
     * @param property filtered property; cannot be null
     * @param value    expected value that property is greater than; cannot be null
     * @return an {@link GreaterThan} expression
     * @throws IllegalArgumentException - If property or value are null
     */
    public static <T> GreaterThan<T> gt( final Property<T> property,
                                         final T value )
    {
        return new GreaterThan<T>( asPropertyExpression( property ), asTypedValueExpression( value ) );
    }

    /**
     * {@link GreaterOrEqual} factory method.
     *
     * @param property filtered property; cannot be null
     * @param value    expected value that property is greater than or equal; cannot be null
     * @return an {@link GreaterOrEqual} expression
     * @throws IllegalArgumentException - If property or value are null
     */
    public static <T> GreaterOrEqual<T> ge( final Property<T> property,
                                            final T value )
    {
        return new GreaterOrEqual<T>( asPropertyExpression( property ), asTypedValueExpression( value ) );
    }

    /**
     * {@link And} factory method. Apply a logical "AND" between two boolean expressions. Also known as "conjunction".
     *
     * @param left  left side boolean expression; cannot be null
     * @param right right side boolean expression; cannot be null
     * @return an {@link And} operator
     * @throws IllegalArgumentException - If left or right expressions are null
     */
    public static And and( final BooleanExpression left,
                           final BooleanExpression right )
    {
        return new And( left, right );
    }

    /**
     * {@link Or} factory method. Apply a logical "OR" between two boolean expressions. Also known as disjunction.
     *
     * @param left  left side boolean expression; cannot be null
     * @param right right side boolean expression; cannot be null
     * @return an {@link Or} operator
     * @throws IllegalArgumentException - If left or right expressions are null
     */
    public static Or or( final BooleanExpression left,
                         final BooleanExpression right )
    {
        return new Or( left, right );
    }

    /**
     * {@link Not} factory method. Apply a logical "NOT" to a boolean expression.
     *
     * @param expression boolean expression; cannot be null; cannot be null
     * @return an {@link Not} operator
     * @throws IllegalArgumentException - If expression is null
     */
    public static Not not( final BooleanExpression expression )
    {
        return new Not( expression );
    }

    /**
     * Adapts a {@link Property} to a {@link PropertyExpression}.
     *
     * @param property to be adapted; cannot be null
     * @return adapted property expression
     * @throws IllegalArgumentException - If property is null or is not an property expression
     */
    private static PropertyExpression asPropertyExpression( final Property property )
    {
        if( property == null )
        {
            throw new IllegalArgumentException( "Property cannot be null" );
        }
        if( !( property instanceof PropertyExpression ) )
        {
            throw new IllegalArgumentException(
                "Invalid property. Properties used in queries must be a result of using QueryBuilder.parameter(...)."
            );
        }
        return (PropertyExpression) property;
    }

    /**
     * Creates a typed value expression from a value.
     *
     * @param value to create expression from; cannot be null
     * @return created expression
     * @throws IllegalArgumentException - If value is null
     */
    private static <T> TypedValueExpression<T> asTypedValueExpression( final T value )
    {
        if( value == null )
        {
            throw new IllegalArgumentException( "Value cannot be null" );
        }
        return new TypedValueExpression<T>( value );
    }

}