/*
 * Copyright 2008 Niclas Hedhman.
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
package org.qi4j.runtime.query;

import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.query.QueryExpressionsProvider;
import org.qi4j.api.query.grammar.*;
import static org.qi4j.api.util.NullArgumentException.validateNotNull;
import org.qi4j.runtime.query.grammar.impl.*;
import org.qi4j.runtime.query.proxy.ManyAssociationReferenceProxy;
import org.qi4j.runtime.query.proxy.MixinTypeProxy;

import static java.lang.reflect.Proxy.*;

public class QueryExpressionsProviderImpl
    implements QueryExpressionsProvider
{

    /**
     * Creates a template for the a mixin type to be used to access properties in type safe fashion.
     *
     * @param mixinType mixin type
     * @return template instance
     */
    @SuppressWarnings( "unchecked" )
    public <T> T templateFor( final Class<T> mixinType )
    {
        return (T) newProxyInstance(
            QueryExpressions.class.getClassLoader(),
            new Class[]{ mixinType },
            new MixinTypeProxy( mixinType )
        );
    }

    public <T> VariableValueExpression<T> newVariableValueExpression( String name )
    {
        return new VariableValueExpressionImpl<T>( name );
    }

    public <T> PropertyIsNullPredicate<T> newPropertyIsNullPredicate( PropertyReference<T> tPropertyReference )
    {
        return new PropertyIsNullPredicateImpl<T>( tPropertyReference );
    }

    public AssociationIsNullPredicate newAssociationIsNullPredicate( AssociationReference associationReference )
    {
        return new AssociationIsNullPredicateImpl( associationReference );
    }

    public <T> PropertyIsNotNullPredicate<T> newPropertyIsNotNullPredicate( PropertyReference<T> tPropertyReference )
    {
        return new PropertyIsNotNullPredicateImpl<T>( tPropertyReference );
    }

    public AssociationIsNotNullPredicate newAssociationIsNotNullPredicate( AssociationReference associationReference )
    {
        return new AssociationIsNotNullPredicateImpl( associationReference );
    }

    public <T> EqualsPredicate<T> newEqualsPredicate( PropertyReference<T> tPropertyReference, SingleValueExpression<T> tStaticValueExpression )
    {
        return new EqualsPredicateImpl<T>( tPropertyReference, tStaticValueExpression );
    }

    public <T> EqualsPredicate<T> newEqualsPredicate( PropertyReference<T> tPropertyReference, VariableValueExpression<T> valueExpression )
    {
        return new EqualsPredicateImpl<T>( tPropertyReference, valueExpression );
    }

    public <T> EqualsPredicate<T> newEqualsPredicate( AssociationReference tAssociationReference, SingleValueExpression<T> tStaticValueExpression )
    {
        return null; // TODO
    }

    public <T> EqualsPredicate<T> newEqualsPredicate( AssociationReference tAssociationReference, VariableValueExpression<T> valueExpression )
    {
        return null; // TODO
    }

    public <T> NotEqualsPredicate<T> newNotEqualsPredicate( PropertyReference<T> tPropertyReference, SingleValueExpression<T> tStaticValueExpression )
    {
        return new NotEqualsPredicateImpl<T>( tPropertyReference, tStaticValueExpression );
    }

    public <T> NotEqualsPredicate<T> newNotEqualsPredicate( PropertyReference<T> tPropertyReference, VariableValueExpression<T> valueExpression )
    {
        return new NotEqualsPredicateImpl<T>( tPropertyReference, valueExpression );
    }

    public <T> LessThanPredicate<T> newLessThanPredicate( PropertyReference<T> tPropertyReference, SingleValueExpression<T> tStaticValueExpression )
    {
        return new LessThanPredicateImpl<T>( tPropertyReference, tStaticValueExpression );
    }

    public <T> LessThanPredicate<T> newLessThanPredicate( PropertyReference<T> tPropertyReference, VariableValueExpression<T> valueExpression )
    {
        return new LessThanPredicateImpl<T>( tPropertyReference, valueExpression );
    }

    public <T> LessOrEqualPredicate<T> newLessOrEqualPredicate( PropertyReference<T> tPropertyReference, SingleValueExpression<T> tStaticValueExpression )
    {
        return new LessOrEqualPredicateImpl<T>( tPropertyReference, tStaticValueExpression );
    }

    public <T> LessOrEqualPredicate<T> newLessOrEqualPredicate( PropertyReference<T> tPropertyReference, VariableValueExpression<T> valueExpression )
    {
        return new LessOrEqualPredicateImpl<T>( tPropertyReference, valueExpression );
    }

    public <T> GreaterThanPredicate<T> newGreaterThanPredicate( PropertyReference<T> tPropertyReference, SingleValueExpression<T> tStaticValueExpression )
    {
        return new GreaterThanPredicateImpl<T>( tPropertyReference, tStaticValueExpression );
    }

    public <T> GreaterThanPredicate<T> newGreaterThanPredicate( PropertyReference<T> tPropertyReference, VariableValueExpression<T> valueExpression )
    {
        return new GreaterThanPredicateImpl<T>( tPropertyReference, valueExpression );
    }

    public <T> GreaterOrEqualPredicate<T> newGreaterOrEqualPredicate( PropertyReference<T> tPropertyReference, SingleValueExpression<T> tStaticValueExpression )
    {
        return new GreaterOrEqualPredicateImpl<T>( tPropertyReference, tStaticValueExpression );
    }

    public <T> GreaterOrEqualPredicate<T> newGreaterOrEqualPredicate( PropertyReference<T> tPropertyReference, VariableValueExpression<T> valueExpression )
    {
        return new GreaterOrEqualPredicateImpl<T>( tPropertyReference, valueExpression );
    }

    public MatchesPredicate newMatchesPredicate( PropertyReference<String> stringPropertyReference, SingleValueExpression<String> stringSingleValueExpression )
    {
        return new MatchesPredicateImpl( stringPropertyReference, stringSingleValueExpression );
    }

    public Conjunction newConjunction( BooleanExpression left, BooleanExpression right )
    {
        return new ConjunctionImpl( left, right );
    }

    public Disjunction newDisjunction( BooleanExpression left, BooleanExpression right )
    {
        return new DisjunctionImpl( left, right );
    }

    public Negation newNegation( BooleanExpression expression )
    {
        return new NegationImpl( expression );
    }

    public OrderBy newOrderBy( PropertyReference<?> tPropertyReference, OrderBy.Order order )
    {
        return new OrderByImpl( tPropertyReference, order );
    }

    public <T> SingleValueExpression<T> newSingleValueExpression( T value )
    {
        return new SingleValueExpressionImpl<T>( value );
    }

    @SuppressWarnings( "unchecked" )
    public <T> T oneOf( ManyAssociation<T> association )
    {
        validateNotNull( "association", association );

        Class<? extends ManyAssociation> associationClass = association.getClass();
        if( !isProxyClass( associationClass ) )
        {
            throw new IllegalArgumentException( "Argument [association] is not a proxy." );
        }

        ManyAssociationReferenceProxy manyAssociationReferenceProxy =
            (ManyAssociationReferenceProxy) getInvocationHandler( association );

        return (T) manyAssociationReferenceProxy.getAnyProxy();
    }
}