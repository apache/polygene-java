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
 * ied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.qi4j.api.query;

import java.util.Collection;
import org.qi4j.api.entity.association.Association;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.property.Property;
import org.qi4j.api.query.grammar.AssociationIsNotNullPredicate;
import org.qi4j.api.query.grammar.AssociationIsNullPredicate;
import org.qi4j.api.query.grammar.AssociationReference;
import org.qi4j.api.query.grammar.BooleanExpression;
import org.qi4j.api.query.grammar.Conjunction;
import org.qi4j.api.query.grammar.ContainsAllPredicate;
import org.qi4j.api.query.grammar.ContainsPredicate;
import org.qi4j.api.query.grammar.Disjunction;
import org.qi4j.api.query.grammar.EqualsPredicate;
import org.qi4j.api.query.grammar.GreaterOrEqualPredicate;
import org.qi4j.api.query.grammar.GreaterThanPredicate;
import org.qi4j.api.query.grammar.LessOrEqualPredicate;
import org.qi4j.api.query.grammar.LessThanPredicate;
import org.qi4j.api.query.grammar.ManyAssociationContainsPredicate;
import org.qi4j.api.query.grammar.ManyAssociationReference;
import org.qi4j.api.query.grammar.MatchesPredicate;
import org.qi4j.api.query.grammar.Negation;
import org.qi4j.api.query.grammar.NotEqualsPredicate;
import org.qi4j.api.query.grammar.OrderBy;
import org.qi4j.api.query.grammar.PropertyIsNotNullPredicate;
import org.qi4j.api.query.grammar.PropertyIsNullPredicate;
import org.qi4j.api.query.grammar.PropertyReference;
import org.qi4j.api.query.grammar.SingleValueExpression;
import org.qi4j.api.query.grammar.VariableValueExpression;

/**
 * Static factory methods for query expressions and operators.
 */
public final class QueryExpressions
{

    private static QueryExpressionsProvider provider;

    /**
     * Set the provider to be used. This is typically called by the runtime.
     *
     * @param provider the QueryExpressionsProvider
     */
    public static void setProvider( QueryExpressionsProvider provider )
    {
        QueryExpressions.provider = provider;
    }

    /**
     * When querying for a member of an association, use this to get
     * a template for a single element, which can then be used in expressions.
     *
     * @param association
     * @param <T>
     *
     * @return
     */
    public static <T> T oneOf( final ManyAssociation<T> association )
    {
        return provider.oneOf( association );
    }

    /**
     * Creates a template for the a mixin type to be used to access properties in type safe fashion.
     *
     * @param mixinType mixin type
     *
     * @return template instance
     */
    @SuppressWarnings( "unchecked" )
    public static <T> T templateFor( final Class<T> mixinType )
    {
        return provider.templateFor( mixinType );
    }

    public static <T> T templateFor( final Class<T> mixinType, Object associatedEntity )
    {
        return provider.templateFor( mixinType, associatedEntity );
    }

    /**
     * {@link org.qi4j.api.query.grammar.VariableValueExpression} factory method.
     *
     * @param name variable name; cannot be null
     *
     * @return an {@link org.qi4j.api.query.grammar.VariableValueExpression} expression
     *
     * @throws IllegalArgumentException - If name is null or empty
     */
    public static <T> VariableValueExpression<T> variable( final String name )
    {
        return provider.newVariableValueExpression( name );
    }

    /**
     * {@link org.qi4j.api.query.grammar.PropertyIsNullPredicate} factory method.
     *
     * @param property filtered property; cannot be null
     *
     * @return an {@link org.qi4j.api.query.grammar.PropertyIsNullPredicate} expression
     *
     * @throws IllegalArgumentException - If property is null
     */
    public static <T> PropertyIsNullPredicate<T> isNull( final Property<T> property )
    {
        return provider.newPropertyIsNullPredicate( asPropertyExpression( property ) );
    }

    /**
     * {@link org.qi4j.api.query.grammar.AssociationIsNullPredicate} factory method.
     *
     * @param association filtered association; cannot be null
     *
     * @return an {@link org.qi4j.api.query.grammar.AssociationIsNullPredicate} expression
     *
     * @throws IllegalArgumentException - If association is null
     */
    public static AssociationIsNullPredicate isNull( final Association<?> association )
    {
        return provider.newAssociationIsNullPredicate( asAssociationExpression( association ) );
    }

    /**
     * {@link org.qi4j.api.query.grammar.PropertyIsNotNullPredicate} factory method.
     *
     * @param property filtered property; cannot be null
     *
     * @return an {@link org.qi4j.api.query.grammar.PropertyIsNotNullPredicate} expression
     *
     * @throws IllegalArgumentException - If property is null
     */
    public static <T> PropertyIsNotNullPredicate<T> isNotNull( final Property<T> property )
    {
        return provider.newPropertyIsNotNullPredicate( asPropertyExpression( property ) );
    }

    /**
     * {@link org.qi4j.api.query.grammar.AssociationIsNotNullPredicate} factory method.
     *
     * @param association filtered association; cannot be null
     *
     * @return an {@link org.qi4j.api.query.grammar.AssociationIsNotNullPredicate} expression
     *
     * @throws IllegalArgumentException - If association is null
     */
    public static AssociationIsNotNullPredicate isNotNull( final Association<?> association )
    {
        return provider.newAssociationIsNotNullPredicate( asAssociationExpression( association ) );
    }

    /**
     * {@link org.qi4j.api.query.grammar.EqualsPredicate} factory method.
     *
     * @param property filtered property; cannot be null
     * @param value    expected value that property is equal to; cannot be null
     *
     * @return an {@link org.qi4j.api.query.grammar.EqualsPredicate}
     *
     * @throws IllegalArgumentException - If property or value are null
     */
    public static <T> EqualsPredicate<T> eq( final Property<T> property,
                                             final T value
    )
    {
        return provider.newEqualsPredicate( asPropertyExpression( property ), asTypedValueExpression( value ) );
    }

    /**
     * {@link org.qi4j.api.query.grammar.EqualsPredicate} factory method.
     *
     * @param property        filtered property; cannot be null
     * @param valueExpression expected value that property is equal to; cannot be null
     *
     * @return an {@link org.qi4j.api.query.grammar.EqualsPredicate}
     *
     * @throws IllegalArgumentException - If property or value are null
     */
    public static <T> EqualsPredicate<T> eq( final Property<T> property,
                                             final VariableValueExpression<T> valueExpression
    )
    {
        return provider.newEqualsPredicate( asPropertyExpression( property ), valueExpression );
    }

    /**
     * {@link org.qi4j.api.query.grammar.EqualsPredicate} factory method.
     *
     * @param property filtered property; cannot be null
     * @param value    expected value that property is equal to; cannot be null
     *
     * @return an {@link org.qi4j.api.query.grammar.EqualsPredicate}
     *
     * @throws IllegalArgumentException - If property or value are null
     */
    public static <T> EqualsPredicate<String> eq( final Association<T> property,
                                                  final T value
    )
    {
        return provider.newEqualsPredicate( asAssociationExpression( property ), asTypedValueExpression( value ) );
    }

    /**
     * {@link org.qi4j.api.query.grammar.EqualsPredicate} factory method.
     *
     * @param property        filtered property; cannot be null
     * @param valueExpression expected value that property is equal to; cannot be null
     *
     * @return an {@link org.qi4j.api.query.grammar.EqualsPredicate}
     *
     * @throws IllegalArgumentException - If property or value are null
     */
    public static <T> EqualsPredicate<T> eq( final Association<T> property,
                                             final VariableValueExpression<T> valueExpression
    )
    {
        return provider.newEqualsPredicate( asAssociationExpression( property ), valueExpression );
    }

    /**
     * {@link org.qi4j.api.query.grammar.NotEqualsPredicate} factory method.
     *
     * @param property filtered property; cannot be null
     * @param value    expected value that property is not equal to; cannot be null
     *
     * @return an {@link org.qi4j.api.query.grammar.NotEqualsPredicate} expression
     *
     * @throws IllegalArgumentException - If property or value are null
     */
    public static <T> NotEqualsPredicate<T> notEq( final Property<T> property,
                                                   final T value
    )
    {
        return provider.newNotEqualsPredicate( asPropertyExpression( property ), asTypedValueExpression( value ) );
    }

    /**
     * {@link org.qi4j.api.query.grammar.NotEqualsPredicate} factory method.
     *
     * @param property        filtered property; cannot be null
     * @param valueExpression expected value that property is not equal to; cannot be null
     *
     * @return an {@link org.qi4j.api.query.grammar.NotEqualsPredicate} expression
     *
     * @throws IllegalArgumentException - If property or value are null
     */
    public static <T> NotEqualsPredicate<T> notEq( final Property<T> property,
                                                   final VariableValueExpression<T> valueExpression
    )
    {
        return provider.newNotEqualsPredicate( asPropertyExpression( property ), valueExpression );
    }

    /**
     * {@link org.qi4j.api.query.grammar.LessThanPredicate} factory method.
     *
     * @param property filtered property; cannot be null
     * @param value    expected value that property is less than; cannot be null
     *
     * @return an {@link org.qi4j.api.query.grammar.LessThanPredicate} expression
     *
     * @throws IllegalArgumentException - If property or value are null
     */
    public static <T> LessThanPredicate<T> lt( final Property<T> property,
                                               final T value
    )
    {
        return provider.newLessThanPredicate( asPropertyExpression( property ), asTypedValueExpression( value ) );
    }

    /**
     * {@link org.qi4j.api.query.grammar.LessThanPredicate} factory method.
     *
     * @param property        filtered property; cannot be null
     * @param valueExpression expected value that property is less than; cannot be null
     *
     * @return an {@link org.qi4j.api.query.grammar.LessThanPredicate} expression
     *
     * @throws IllegalArgumentException - If property or value are null
     */
    public static <T> LessThanPredicate<T> lt( final Property<T> property,
                                               final VariableValueExpression<T> valueExpression
    )
    {
        return provider.newLessThanPredicate( asPropertyExpression( property ), valueExpression );
    }

    /**
     * {@link org.qi4j.api.query.grammar.LessOrEqualPredicate} factory method.
     *
     * @param property filtered property; cannot be null
     * @param value    expected value that property is less than or equal to; cannot be null
     *
     * @return an {@link org.qi4j.api.query.grammar.LessOrEqualPredicate} expression
     *
     * @throws IllegalArgumentException - If property or value are null
     */
    public static <T> LessOrEqualPredicate<T> le( final Property<T> property,
                                                  final T value
    )
    {
        return provider.newLessOrEqualPredicate( asPropertyExpression( property ), asTypedValueExpression( value ) );
    }

    /**
     * {@link org.qi4j.api.query.grammar.LessOrEqualPredicate} factory method.
     *
     * @param property        filtered property; cannot be null
     * @param valueExpression expected value that property is less than or equal to; cannot be null
     *
     * @return an {@link org.qi4j.api.query.grammar.LessOrEqualPredicate} expression
     *
     * @throws IllegalArgumentException - If property or value are null
     */
    public static <T> LessOrEqualPredicate<T> le( final Property<T> property,
                                                  final VariableValueExpression<T> valueExpression
    )
    {
        return provider.newLessOrEqualPredicate( asPropertyExpression( property ), valueExpression );
    }

    /**
     * {@link org.qi4j.api.query.grammar.GreaterThanPredicate} factory method.
     *
     * @param property filtered property; cannot be null
     * @param value    expected value that property is greater than; cannot be null
     *
     * @return an {@link org.qi4j.api.query.grammar.GreaterThanPredicate} expression
     *
     * @throws IllegalArgumentException - If property or value are null
     */
    public static <T> GreaterThanPredicate<T> gt( final Property<T> property,
                                                  final T value
    )
    {
        return provider.newGreaterThanPredicate( asPropertyExpression( property ), asTypedValueExpression( value ) );
    }

    /**
     * {@link org.qi4j.api.query.grammar.GreaterThanPredicate} factory method.
     *
     * @param property        filtered property; cannot be null
     * @param valueExpression expected value that property is greater than; cannot be null
     *
     * @return an {@link org.qi4j.api.query.grammar.GreaterThanPredicate} expression
     *
     * @throws IllegalArgumentException - If property or value are null
     */
    public static <T> GreaterThanPredicate<T> gt( final Property<T> property,
                                                  final VariableValueExpression<T> valueExpression
    )
    {
        return provider.newGreaterThanPredicate( asPropertyExpression( property ), valueExpression );
    }

    /**
     * {@link org.qi4j.api.query.grammar.GreaterOrEqualPredicate} factory method.
     *
     * @param property filtered property; cannot be null
     * @param value    expected value that property is greater than or equal; cannot be null
     *
     * @return an {@link org.qi4j.api.query.grammar.GreaterOrEqualPredicate} expression
     *
     * @throws IllegalArgumentException - If property or value are null
     */
    public static <T> GreaterOrEqualPredicate<T> ge( final Property<T> property,
                                                     final T value
    )
    {
        return provider.newGreaterOrEqualPredicate( asPropertyExpression( property ), asTypedValueExpression( value ) );
    }

    /**
     * {@link org.qi4j.api.query.grammar.GreaterOrEqualPredicate} factory method.
     *
     * @param property        filtered property; cannot be null
     * @param valueExpression expected value that property is greater than or equal; cannot be null
     *
     * @return an {@link org.qi4j.api.query.grammar.GreaterOrEqualPredicate} expression
     *
     * @throws IllegalArgumentException - If property or value are null
     */
    public static <T> GreaterOrEqualPredicate<T> ge( final Property<T> property,
                                                     final VariableValueExpression<T> valueExpression
    )
    {
        return provider.newGreaterOrEqualPredicate( asPropertyExpression( property ), valueExpression );
    }

    /**
     * {@link org.qi4j.api.query.grammar.MatchesPredicate} factory method.
     *
     * @param property filtered property; cannot be null
     * @param regexp   expected regexp that property should match to; cannot be null
     *
     * @return an {@link org.qi4j.api.query.grammar.MatchesPredicate}
     *
     * @throws IllegalArgumentException - If property or value are null
     */
    public static MatchesPredicate matches( final Property<String> property,
                                            final String regexp
    )
    {
        return provider.newMatchesPredicate( asPropertyExpression( property ), asTypedValueExpression( regexp ) );
    }

    /**
     * {@link org.qi4j.api.query.grammar.Conjunction} factory method. Apply a logical "AND" between two (or more) boolean expressions. Also known as "conjunction".
     *
     * @param left          left side boolean expression; cannot be null
     * @param right         right side boolean expression; cannot be null
     * @param optionalRight optional additional right side boolean expressions
     *
     * @return an {@link org.qi4j.api.query.grammar.Conjunction} operator
     *
     * @throws IllegalArgumentException - If left or right expressions are null
     */
    public static Conjunction and( final BooleanExpression left,
                                   final BooleanExpression right,
                                   final BooleanExpression... optionalRight
    )
    {
        BooleanExpression leftExpr = left;
        BooleanExpression rightExpr = right;
        Conjunction conjunction = provider.newConjunction( leftExpr, rightExpr );
        for( BooleanExpression anOptionalRight : optionalRight )
        {
            leftExpr = conjunction;
            rightExpr = anOptionalRight;
            conjunction = provider.newConjunction( leftExpr, rightExpr );
        }

        return conjunction;
    }

    /**
     * {@link org.qi4j.api.query.grammar.Disjunction} factory method. Apply a logical "OR" between two (or more) boolean expressions. Also known as disjunction.
     *
     * @param left          left side boolean expression; cannot be null
     * @param right         right side boolean expression; cannot be null
     * @param optionalRight optional additional right side boolean expressions
     *
     * @return an {@link org.qi4j.api.query.grammar.Disjunction} operator
     *
     * @throws IllegalArgumentException - If left or right expressions are null
     */
    public static Disjunction or( final BooleanExpression left,
                                  final BooleanExpression right,
                                  final BooleanExpression... optionalRight
    )
    {
        BooleanExpression leftExpr = left;
        BooleanExpression rightExpr = right;
        Disjunction disjunction = provider.newDisjunction( leftExpr, rightExpr );
        for( BooleanExpression anOptionalRight : optionalRight )
        {
            leftExpr = disjunction;
            rightExpr = anOptionalRight;
            disjunction = provider.newDisjunction( leftExpr, rightExpr );
        }

        return disjunction;
    }

    /**
     * {@link org.qi4j.api.query.grammar.Negation} factory method. Apply a logical "NOT" to a boolean expression.
     *
     * @param expression boolean expression; cannot be null; cannot be null
     *
     * @return an {@link org.qi4j.api.query.grammar.Negation} operator
     *
     * @throws IllegalArgumentException - If expression is null
     */
    public static Negation not( final BooleanExpression expression )
    {
        return provider.newNegation( expression );
    }

    public static <T> ContainsPredicate<T> contains( Property<Collection<T>> property, T value )
    {
        return provider.newContainsPredicate( asPropertyExpression( property ), asTypedValueExpression( value ) );
    }

    public static <T> ManyAssociationContainsPredicate<T> contains( ManyAssociation<T> manyAssoc, T value )
    {
        return provider.newManyAssociationContainsPredicate( asManyAssociationExpression( manyAssoc ), asTypedValueExpression( value ) );
    }

    public static <T> ContainsAllPredicate<T> containsAll( Property<Collection<T>> property, Collection<T> value )
    {
        return provider.newContainsAllPredicate( asPropertyExpression( property ), asTypedValueExpression( value ) );
    }

    /**
     * {@link org.qi4j.api.query.grammar.OrderBy} factory method (ascending order).
     *
     * @param property sorting property; cannot be null
     *
     * @return an {@link org.qi4j.api.query.grammar.OrderBy}
     *
     * @throws IllegalArgumentException - If property is null
     */
    public static <T> OrderBy orderBy( final Property<T> property )
    {
        return orderBy( property, null );
    }

    /**
     * {@link org.qi4j.api.query.grammar.OrderBy} factory method.
     *
     * @param property sorting property; cannot be null
     * @param order    sorting direction
     *
     * @return an {@link org.qi4j.api.query.grammar.OrderBy}
     *
     * @throws IllegalArgumentException - If property is null
     */
    public static <T> OrderBy orderBy( final Property<T> property,
                                       final OrderBy.Order order
    )
    {
        return provider.newOrderBy( asPropertyExpression( property ), order );
    }

    /**
     * Adapts a {@link Property} to a {@link org.qi4j.api.query.grammar.PropertyReference}.
     *
     * @param property to be adapted; cannot be null
     *
     * @return adapted property expression
     *
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
     * Adapts an {@link Association} to a {@link org.qi4j.api.query.grammar.AssociationReference}.
     *
     * @param association to be adapted; cannot be null
     *
     * @return adapted association expression
     *
     * @throws IllegalArgumentException - If association is null or is not an association expression
     */
    private static AssociationReference asAssociationExpression( final Association<?> association )
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
     * Adapts an {@link ManyAssociation} to a {@link org.qi4j.api.query.grammar.ManyAssociationReference}.
     *
     * @param association to be adapted; cannot be null
     *
     * @return adapted association expression
     *
     * @throws IllegalArgumentException - If association is null or is not an association expression
     */
    private static ManyAssociationReference asManyAssociationExpression( final ManyAssociation<?> association )
    {
        if( association == null )
        {
            throw new IllegalArgumentException( "ManyAssociation cannot be null" );
        }
        if( !( association instanceof ManyAssociationReference ) )
        {
            throw new IllegalArgumentException(
                "Invalid property. Association used in queries must be a result of using QueryBuilder.templateFor(...)."
            );
        }
        return (ManyAssociationReference) association;
    }

    /**
     * Creates a typed value expression from a value.
     *
     * @param value to create expression from; cannot be null
     *
     * @return created expression
     *
     * @throws IllegalArgumentException - If value is null
     */
    private static <T> SingleValueExpression<T> asTypedValueExpression( final T value )
    {
        if( value == null )
        {
            throw new IllegalArgumentException( "Value cannot be null" );
        }
        return provider.newSingleValueExpression( value );
    }
}