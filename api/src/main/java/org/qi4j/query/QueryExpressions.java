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
import org.qi4j.entity.association.Association;
import org.qi4j.property.Property;
import org.qi4j.query.grammar.AssociationIsNotNullPredicate;
import org.qi4j.query.grammar.AssociationIsNullPredicate;
import org.qi4j.query.grammar.AssociationReference;
import org.qi4j.query.grammar.BooleanExpression;
import org.qi4j.query.grammar.Conjunction;
import org.qi4j.query.grammar.Disjunction;
import org.qi4j.query.grammar.EqualsPredicate;
import org.qi4j.query.grammar.GreaterOrEqualPredicate;
import org.qi4j.query.grammar.GreaterThanPredicate;
import org.qi4j.query.grammar.LessOrEqualPredicate;
import org.qi4j.query.grammar.LessThanPredicate;
import org.qi4j.query.grammar.Negation;
import org.qi4j.query.grammar.NotEqualsPredicate;
import org.qi4j.query.grammar.OrderBy;
import org.qi4j.query.grammar.PropertyIsNotNullPredicate;
import org.qi4j.query.grammar.PropertyIsNullPredicate;
import org.qi4j.query.grammar.PropertyReference;
import org.qi4j.query.grammar.impl.AssociationIsNotNullPredicateImpl;
import org.qi4j.query.grammar.impl.AssociationIsNullPredicateImpl;
import org.qi4j.query.grammar.impl.ConjunctionImpl;
import org.qi4j.query.grammar.impl.DisjunctionImpl;
import org.qi4j.query.grammar.impl.EqualsPredicateImpl;
import org.qi4j.query.grammar.impl.GreaterOrEqualPredicateImpl;
import org.qi4j.query.grammar.impl.GreaterThanPredicateImpl;
import org.qi4j.query.grammar.impl.LessOrEqualPredicateImpl;
import org.qi4j.query.grammar.impl.LessThanPredicateImpl;
import org.qi4j.query.grammar.impl.NegationImpl;
import org.qi4j.query.grammar.impl.NotEqualsPredicateImpl;
import org.qi4j.query.grammar.impl.OrderByImpl;
import org.qi4j.query.grammar.impl.PropertyIsNotNullPredicateImpl;
import org.qi4j.query.grammar.impl.PropertyIsNullPredicateImpl;
import org.qi4j.query.grammar.impl.StaticValueExpression;
import org.qi4j.query.grammar.impl.VariableValueExpression;
import org.qi4j.query.proxy.MixinTypeProxy;

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
    public static <T> T templateFor( final Class<T> mixinType )
    {
        return (T) Proxy.newProxyInstance(
            QueryExpressions.class.getClassLoader(),
            new Class[]{ mixinType },
            new MixinTypeProxy( mixinType )
        );
    }

    /**
     * {@link org.qi4j.query.grammar.impl.VariableValueExpression} factory method.
     *
     * @param name variable name; cannot be null
     * @return an {@link org.qi4j.query.grammar.impl.VariableValueExpression} expression
     * @throws IllegalArgumentException - If name is null or empty
     */
    public static <T> VariableValueExpression<T> variable( final String name )
    {
        return new VariableValueExpression<T>( name );
    }

    /**
     * {@link org.qi4j.query.grammar.EqualsPredicate} factory method.
     *
     * @param property filtered property; cannot be null
     * @param value    expected value that property is equal to; cannot be null
     * @return an {@link org.qi4j.query.grammar.EqualsPredicate}
     * @throws IllegalArgumentException - If property or value are null
     */
    public static <T> EqualsPredicate<T> eq( final Property<T> property,
                                             final T value )
    {
        return new EqualsPredicateImpl<T>( asPropertyExpression( property ), asTypedValueExpression( value ) );
    }

    /**
     * {@link org.qi4j.query.grammar.EqualsPredicate} factory method.
     *
     * @param property        filtered property; cannot be null
     * @param valueExpression expected value that property is equal to; cannot be null
     * @return an {@link org.qi4j.query.grammar.EqualsPredicate}
     * @throws IllegalArgumentException - If property or value are null
     */
    public static <T> EqualsPredicate<T> eq( final Property<T> property,
                                             final VariableValueExpression<T> valueExpression )
    {
        return new EqualsPredicateImpl<T>( asPropertyExpression( property ), valueExpression );
    }

    /**
     * {@link org.qi4j.query.grammar.impl.PropertyIsNullPredicateImpl} factory method.
     *
     * @param property filtered property; cannot be null
     * @return an {@link org.qi4j.query.grammar.impl.PropertyIsNullPredicateImpl} expression
     * @throws IllegalArgumentException - If property is null
     */
    public static <T> PropertyIsNullPredicate<T> isNull( final Property<T> property )
    {
        return new PropertyIsNullPredicateImpl<T>( asPropertyExpression( property ) );
    }

    /**
     * {@link org.qi4j.query.grammar.impl.AssociationIsNullPredicateImpl} factory method.
     *
     * @param association filtered association; cannot be null
     * @return an {@link org.qi4j.query.grammar.impl.AssociationIsNullPredicateImpl} expression
     * @throws IllegalArgumentException - If association is null
     */
    public static AssociationIsNullPredicate isNull( final Association association )
    {
        return new AssociationIsNullPredicateImpl( asAssociationExpression( association ) );
    }

    /**
     * {@link org.qi4j.query.grammar.impl.PropertyIsNotNullPredicateImpl} factory method.
     *
     * @param property filtered property; cannot be null
     * @return an {@link org.qi4j.query.grammar.impl.PropertyIsNotNullPredicateImpl} expression
     * @throws IllegalArgumentException - If property is null
     */
    public static <T> PropertyIsNotNullPredicate<T> isNotNull( final Property<T> property )
    {
        return new PropertyIsNotNullPredicateImpl<T>( asPropertyExpression( property ) );
    }

    /**
     * {@link org.qi4j.query.grammar.impl.AssociationIsNotNullPredicateImpl} factory method.
     *
     * @param association filtered association; cannot be null
     * @return an {@link org.qi4j.query.grammar.impl.AssociationIsNotNullPredicateImpl} expression
     * @throws IllegalArgumentException - If association is null
     */
    public static AssociationIsNotNullPredicate isNotNull( final Association association )
    {
        return new AssociationIsNotNullPredicateImpl( asAssociationExpression( association ) );
    }

    /**
     * {@link org.qi4j.query.grammar.impl.NotEqualsPredicateImpl} factory method.
     *
     * @param property filtered property; cannot be null
     * @param value    expected value that property is not equal to; cannot be null
     * @return an {@link org.qi4j.query.grammar.impl.NotEqualsPredicateImpl} expression
     * @throws IllegalArgumentException - If property or value are null
     */
    public static <T> NotEqualsPredicate<T> notEq( final Property<T> property,
                                                   final T value )
    {
        return new NotEqualsPredicateImpl<T>( asPropertyExpression( property ), asTypedValueExpression( value ) );
    }

    /**
     * {@link org.qi4j.query.grammar.impl.NotEqualsPredicateImpl} factory method.
     *
     * @param property        filtered property; cannot be null
     * @param valueExpression expected value that property is not equal to; cannot be null
     * @return an {@link org.qi4j.query.grammar.impl.NotEqualsPredicateImpl} expression
     * @throws IllegalArgumentException - If property or value are null
     */
    public static <T> NotEqualsPredicate<T> notEq( final Property<T> property,
                                                   final VariableValueExpression<T> valueExpression )
    {
        return new NotEqualsPredicateImpl<T>( asPropertyExpression( property ), valueExpression );
    }


    /**
     * {@link org.qi4j.query.grammar.impl.LessThanPredicateImpl} factory method.
     *
     * @param property filtered property; cannot be null
     * @param value    expected value that property is less than; cannot be null
     * @return an {@link org.qi4j.query.grammar.impl.LessThanPredicateImpl} expression
     * @throws IllegalArgumentException - If property or value are null
     */
    public static <T> LessThanPredicate<T> lt( final Property<T> property,
                                               final T value )
    {
        return new LessThanPredicateImpl<T>( asPropertyExpression( property ), asTypedValueExpression( value ) );
    }

    /**
     * {@link org.qi4j.query.grammar.impl.LessThanPredicateImpl} factory method.
     *
     * @param property        filtered property; cannot be null
     * @param valueExpression expected value that property is less than; cannot be null
     * @return an {@link org.qi4j.query.grammar.impl.LessThanPredicateImpl} expression
     * @throws IllegalArgumentException - If property or value are null
     */
    public static <T> LessThanPredicate<T> lt( final Property<T> property,
                                               final VariableValueExpression<T> valueExpression )
    {
        return new LessThanPredicateImpl<T>( asPropertyExpression( property ), valueExpression );
    }

    /**
     * {@link org.qi4j.query.grammar.impl.LessOrEqualPredicateImpl} factory method.
     *
     * @param property filtered property; cannot be null
     * @param value    expected value that property is less than or equal to; cannot be null
     * @return an {@link org.qi4j.query.grammar.impl.LessOrEqualPredicateImpl} expression
     * @throws IllegalArgumentException - If property or value are null
     */
    public static <T> LessOrEqualPredicate<T> le( final Property<T> property,
                                                  final T value )
    {
        return new LessOrEqualPredicateImpl<T>( asPropertyExpression( property ), asTypedValueExpression( value ) );
    }

    /**
     * {@link org.qi4j.query.grammar.impl.LessOrEqualPredicateImpl} factory method.
     *
     * @param property        filtered property; cannot be null
     * @param valueExpression expected value that property is less than or equal to; cannot be null
     * @return an {@link org.qi4j.query.grammar.impl.LessOrEqualPredicateImpl} expression
     * @throws IllegalArgumentException - If property or value are null
     */
    public static <T> LessOrEqualPredicate<T> le( final Property<T> property,
                                                  final VariableValueExpression<T> valueExpression )
    {
        return new LessOrEqualPredicateImpl<T>( asPropertyExpression( property ), valueExpression );
    }

    /**
     * {@link org.qi4j.query.grammar.impl.GreaterThanPredicateImpl} factory method.
     *
     * @param property filtered property; cannot be null
     * @param value    expected value that property is greater than; cannot be null
     * @return an {@link org.qi4j.query.grammar.impl.GreaterThanPredicateImpl} expression
     * @throws IllegalArgumentException - If property or value are null
     */
    public static <T> GreaterThanPredicate<T> gt( final Property<T> property,
                                                  final T value )
    {
        return new GreaterThanPredicateImpl<T>( asPropertyExpression( property ), asTypedValueExpression( value ) );
    }

    /**
     * {@link org.qi4j.query.grammar.impl.GreaterThanPredicateImpl} factory method.
     *
     * @param property        filtered property; cannot be null
     * @param valueExpression expected value that property is greater than; cannot be null
     * @return an {@link org.qi4j.query.grammar.impl.GreaterThanPredicateImpl} expression
     * @throws IllegalArgumentException - If property or value are null
     */
    public static <T> GreaterThanPredicate<T> gt( final Property<T> property,
                                                  final VariableValueExpression<T> valueExpression )
    {
        return new GreaterThanPredicateImpl<T>( asPropertyExpression( property ), valueExpression );
    }

    /**
     * {@link org.qi4j.query.grammar.impl.GreaterOrEqualPredicateImpl} factory method.
     *
     * @param property filtered property; cannot be null
     * @param value    expected value that property is greater than or equal; cannot be null
     * @return an {@link org.qi4j.query.grammar.impl.GreaterOrEqualPredicateImpl} expression
     * @throws IllegalArgumentException - If property or value are null
     */
    public static <T> GreaterOrEqualPredicate<T> ge( final Property<T> property,
                                                     final T value )
    {
        return new GreaterOrEqualPredicateImpl<T>( asPropertyExpression( property ), asTypedValueExpression( value ) );
    }

    /**
     * {@link org.qi4j.query.grammar.impl.GreaterOrEqualPredicateImpl} factory method.
     *
     * @param property        filtered property; cannot be null
     * @param valueExpression expected value that property is greater than or equal; cannot be null
     * @return an {@link org.qi4j.query.grammar.impl.GreaterOrEqualPredicateImpl} expression
     * @throws IllegalArgumentException - If property or value are null
     */
    public static <T> GreaterOrEqualPredicate<T> ge( final Property<T> property,
                                                     final VariableValueExpression<T> valueExpression )
    {
        return new GreaterOrEqualPredicateImpl<T>( asPropertyExpression( property ), valueExpression );
    }

    /**
     * {@link org.qi4j.query.grammar.impl.ConjunctionImpl} factory method. Apply a logical "AND" between two boolean expressions. Also known as "conjunction".
     *
     * @param left  left side boolean expression; cannot be null
     * @param right right side boolean expression; cannot be null
     * @return an {@link org.qi4j.query.grammar.impl.ConjunctionImpl} operator
     * @throws IllegalArgumentException - If left or right expressions are null
     */
    public static Conjunction and( final BooleanExpression left,
                                   final BooleanExpression right )
    {
        return new ConjunctionImpl( left, right );
    }

    /**
     * {@link org.qi4j.query.grammar.impl.DisjunctionImpl} factory method. Apply a logical "OR" between two boolean expressions. Also known as disjunction.
     *
     * @param left  left side boolean expression; cannot be null
     * @param right right side boolean expression; cannot be null
     * @return an {@link org.qi4j.query.grammar.impl.DisjunctionImpl} operator
     * @throws IllegalArgumentException - If left or right expressions are null
     */
    public static Disjunction or( final BooleanExpression left,
                                  final BooleanExpression right )
    {
        return new DisjunctionImpl( left, right );
    }

    /**
     * {@link org.qi4j.query.grammar.impl.NegationImpl} factory method. Apply a logical "NOT" to a boolean expression.
     *
     * @param expression boolean expression; cannot be null; cannot be null
     * @return an {@link org.qi4j.query.grammar.impl.NegationImpl} operator
     * @throws IllegalArgumentException - If expression is null
     */
    public static Negation not( final BooleanExpression expression )
    {
        return new NegationImpl( expression );
    }

    /**
     * {@link org.qi4j.query.grammar.OrderBy} factory method (ascending order).
     *
     * @param property sorting property; cannot be null
     * @return an {@link org.qi4j.query.grammar.OrderBy}
     * @throws IllegalArgumentException - If property is null
     */
    public static <T> OrderBy orderBy( final Property<T> property )
    {
        return orderBy( property, null );
    }

    /**
     * {@link org.qi4j.query.grammar.OrderBy} factory method.
     *
     * @param property sorting property; cannot be null
     * @param order    sorting direction
     * @return an {@link org.qi4j.query.grammar.OrderBy}
     * @throws IllegalArgumentException - If property is null
     */
    public static <T> OrderBy orderBy( final Property<T> property,
                                       final OrderBy.Order order )
    {
        return new OrderByImpl( asPropertyExpression( property ), order );
    }

    /**
     * Adapts a {@link Property} to a {@link org.qi4j.query.grammar.PropertyReference}.
     *
     * @param property to be adapted; cannot be null
     * @return adapted property expression
     * @throws IllegalArgumentException - If property is null or is not an property expression
     */
    @SuppressWarnings( "unchecked" )
    private static <T> PropertyReference<T> asPropertyExpression( final Property<T> property )
    {
        if( property == null )
        {
            throw new IllegalArgumentException( "Property cannot be null" );
        }
        if( !( property instanceof PropertyReference ) )
        {
            throw new IllegalArgumentException(
                "Invalid property. Properties used in queries must be a result of using QueryBuilder.templateFor(...)."
            );
        }
        return (PropertyReference<T>) property;
    }

    /**
     * Adapts an {@link Association} to a {@link org.qi4j.query.grammar.AssociationReference}.
     *
     * @param association to be adapted; cannot be null
     * @return adapted association expression
     * @throws IllegalArgumentException - If association is null or is not an association expression
     */
    private static AssociationReference asAssociationExpression( final Association association )
    {
        if( association == null )
        {
            throw new IllegalArgumentException( "Association cannot be null" );
        }
        if( !( association instanceof AssociationReference ) )
        {
            throw new IllegalArgumentException(
                "Invalid property. Association used in queries must be a result of using QueryBuilder.templateFor(...)."
            );
        }
        return (AssociationReference) association;
    }

    /**
     * Creates a typed value expression from a value.
     *
     * @param value to create expression from; cannot be null
     * @return created expression
     * @throws IllegalArgumentException - If value is null
     */
    private static <T> StaticValueExpression<T> asTypedValueExpression( final T value )
    {
        if( value == null )
        {
            throw new IllegalArgumentException( "Value cannot be null" );
        }
        return new StaticValueExpression<T>( value );
    }


}