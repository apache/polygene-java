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

import java.lang.reflect.Proxy;
import org.qi4j.property.Property;
import org.qi4j.query.graph.And;
import org.qi4j.query.graph.BooleanExpression;
import org.qi4j.query.graph.Equals;
import org.qi4j.query.graph.EqualsProperty;
import org.qi4j.query.graph.GreaterOrEqual;
import org.qi4j.query.graph.GreaterThan;
import org.qi4j.query.graph.IsNotNull;
import org.qi4j.query.graph.IsNull;
import org.qi4j.query.graph.LessOrEqual;
import org.qi4j.query.graph.LessThan;
import org.qi4j.query.graph.Not;
import org.qi4j.query.graph.NotEquals;
import org.qi4j.query.graph.Or;
import org.qi4j.query.graph.PropertyExpression;
import org.qi4j.query.graph.MixinTypeProxyFactory;
import org.qi4j.query.graph.TypedValue;
import org.qi4j.query.graph.VariableValue;

/**
 * Static factory methods for query expressions and operators.
 */
public class QueryExpressions
{

    /**
     * Creates a template for the a mixin type to be used to access properties in type safe fashion.
     *
     * @param mixinType mixin type
     * @return template instance
     */
    @SuppressWarnings( "unchecked" )
    public static <T> T templateFor( Class<T> mixinType )
    {
        return (T) Proxy.newProxyInstance(
            QueryExpressions.class.getClassLoader(),
            new Class[]{ mixinType },
            new MixinTypeProxyFactory()
        );
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
     * {@link Equals} factory method.
     *
     * @param property filtered property; cannot be null
     * @param value    expected value that property is equal to; cannot be null
     * @return an {@link Equals} expression
     * @throws IllegalArgumentException - If property or value are null
     */
    public static <T> BooleanExpression eq( final Property<T> property,
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
    public static <T> BooleanExpression eq( final Property<T> property,
                                            final VariableValue<T> value )
    {
        return new Equals<T>( asPropertyExpression( property ), value );
    }

    /**
     * {@link EqualsProperty} factory method.
     *
     * @param property        filtered property; cannot be null
     * @param anotherProperty expected property that left side property is equal to; cannot be null
     * @return an {@link EqualsProperty} expression
     * @throws IllegalArgumentException - If property or value are null
     */
    public static <T> BooleanExpression eq( final Property<T> property,
                                            final Property<T> anotherProperty )
    {
        return new EqualsProperty<T>( asPropertyExpression( property ), asPropertyExpression( anotherProperty ) );
    }

    /**
     * {@link IsNull} factory method.
     *
     * @param property filtered property; cannot be null
     * @return an {@link IsNull} expression
     * @throws IllegalArgumentException - If property or value are null
     */
    public static <T> BooleanExpression isNull( final Property<T> property )
    {
        return new IsNull<T>( asPropertyExpression( property ) );
    }

    /**
     * {@link IsNotNull} factory method.
     *
     * @param property filtered property; cannot be null
     * @return an {@link IsNotNull} expression
     * @throws IllegalArgumentException - If propertyis null
     */
    public static <T> BooleanExpression isNotNull( final Property<T> property )
    {
        return new IsNotNull<T>( asPropertyExpression( property ) );
    }

    /**
     * {@link NotEquals} factory method.
     *
     * @param property filtered property; cannot be null
     * @param value    expected value that property is not equal to; cannot be null
     * @return an {@link NotEquals} expression
     * @throws IllegalArgumentException - If property or value are null
     */
    public static <T> BooleanExpression notEq( final Property<T> property,
                                               final T value )
    {
        return new NotEquals<T>( asPropertyExpression( property ), asTypedValueExpression( value ) );
    }

    /**
     * {@link NotEquals} factory method.
     *
     * @param property filtered property; cannot be null
     * @param value    expected value that property is not equal to; cannot be null
     * @return an {@link NotEquals} expression
     * @throws IllegalArgumentException - If property or value are null
     */
    public static <T> BooleanExpression notEq( final Property<T> property,
                                               final VariableValue<T> value )
    {
        return new NotEquals<T>( asPropertyExpression( property ), value );
    }


    /**
     * {@link LessThan} factory method.
     *
     * @param property filtered property; cannot be null
     * @param value    expected value that property is less than; cannot be null
     * @return an {@link LessThan} expression
     * @throws IllegalArgumentException - If property or value are null
     */
    public static <T> BooleanExpression lt( final Property<T> property,
                                            final T value )
    {
        return new LessThan<T>( asPropertyExpression( property ), asTypedValueExpression( value ) );
    }

    /**
     * {@link LessThan} factory method.
     *
     * @param property filtered property; cannot be null
     * @param value    expected value that property is less than; cannot be null
     * @return an {@link LessThan} expression
     * @throws IllegalArgumentException - If property or value are null
     */
    public static <T> BooleanExpression lt( final Property<T> property,
                                            final VariableValue<T> value )
    {
        return new LessThan<T>( asPropertyExpression( property ), value );
    }

    /**
     * {@link LessOrEqual} factory method.
     *
     * @param property filtered property; cannot be null
     * @param value    expected value that property is less than or equal to; cannot be null
     * @return an {@link LessOrEqual} expression
     * @throws IllegalArgumentException - If property or value are null
     */
    public static <T> BooleanExpression le( final Property<T> property,
                                            final T value )
    {
        return new LessOrEqual<T>( asPropertyExpression( property ), asTypedValueExpression( value ) );
    }

    /**
     * {@link LessOrEqual} factory method.
     *
     * @param property filtered property; cannot be null
     * @param value    expected value that property is less than or equal to; cannot be null
     * @return an {@link LessOrEqual} expression
     * @throws IllegalArgumentException - If property or value are null
     */
    public static <T> BooleanExpression le( final Property<T> property,
                                            final VariableValue<T> value )
    {
        return new LessOrEqual<T>( asPropertyExpression( property ), value );
    }

    /**
     * {@link GreaterThan} factory method.
     *
     * @param property filtered property; cannot be null
     * @param value    expected value that property is greater than; cannot be null
     * @return an {@link GreaterThan} expression
     * @throws IllegalArgumentException - If property or value are null
     */
    public static <T> BooleanExpression gt( final Property<T> property,
                                            final T value )
    {
        return new GreaterThan<T>( asPropertyExpression( property ), asTypedValueExpression( value ) );
    }

    /**
     * {@link GreaterThan} factory method.
     *
     * @param property filtered property; cannot be null
     * @param value    expected value that property is greater than; cannot be null
     * @return an {@link GreaterThan} expression
     * @throws IllegalArgumentException - If property or value are null
     */
    public static <T> BooleanExpression gt( final Property<T> property,
                                            final VariableValue<T> value )
    {
        return new GreaterThan<T>( asPropertyExpression( property ), value );
    }

    /**
     * {@link GreaterOrEqual} factory method.
     *
     * @param property filtered property; cannot be null
     * @param value    expected value that property is greater than or equal; cannot be null
     * @return an {@link GreaterOrEqual} expression
     * @throws IllegalArgumentException - If property or value are null
     */
    public static <T> BooleanExpression ge( final Property<T> property,
                                            final T value )
    {
        return new GreaterOrEqual<T>( asPropertyExpression( property ), asTypedValueExpression( value ) );
    }

    /**
     * {@link GreaterOrEqual} factory method.
     *
     * @param property filtered property; cannot be null
     * @param value    expected value that property is greater than or equal; cannot be null
     * @return an {@link GreaterOrEqual} expression
     * @throws IllegalArgumentException - If property or value are null
     */
    public static <T> BooleanExpression ge( final Property<T> property,
                                            final VariableValue<T> value )
    {
        return new GreaterOrEqual<T>( asPropertyExpression( property ), value );
    }

    /**
     * {@link And} factory method. Apply a logical "AND" between two boolean expressions. Also known as "conjunction".
     *
     * @param left  left side boolean expression; cannot be null
     * @param right right side boolean expression; cannot be null
     * @return an {@link And} operator
     * @throws IllegalArgumentException - If left or right expressions are null
     */
    public static BooleanExpression and( final BooleanExpression left,
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
    public static BooleanExpression or( final BooleanExpression left,
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
    public static BooleanExpression not( final BooleanExpression expression )
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
    private static <T> PropertyExpression<T> asPropertyExpression( final Property<T> property )
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
        return (PropertyExpression<T>) property;
    }

    /**
     * Creates a typed value expression from a value.
     *
     * @param value to create expression from; cannot be null
     * @return created expression
     * @throws IllegalArgumentException - If value is null
     */
    private static <T> TypedValue<T> asTypedValueExpression( final T value )
    {
        if( value == null )
        {
            throw new IllegalArgumentException( "Value cannot be null" );
        }
        return new TypedValue<T>( value );
    }


}