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

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.query.QueryExpressionsProvider;
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
import org.qi4j.runtime.query.grammar.impl.AssociationIsNotNullPredicateImpl;
import org.qi4j.runtime.query.grammar.impl.AssociationIsNullPredicateImpl;
import org.qi4j.runtime.query.grammar.impl.ConjunctionImpl;
import org.qi4j.runtime.query.grammar.impl.ContainsAllPredicateImpl;
import org.qi4j.runtime.query.grammar.impl.ContainsPredicateImpl;
import org.qi4j.runtime.query.grammar.impl.DisjunctionImpl;
import org.qi4j.runtime.query.grammar.impl.EqualsPredicateImpl;
import org.qi4j.runtime.query.grammar.impl.GreaterOrEqualPredicateImpl;
import org.qi4j.runtime.query.grammar.impl.GreaterThanPredicateImpl;
import org.qi4j.runtime.query.grammar.impl.LessOrEqualPredicateImpl;
import org.qi4j.runtime.query.grammar.impl.LessThanPredicateImpl;
import org.qi4j.runtime.query.grammar.impl.ManyAssociationContainsPredicateImpl;
import org.qi4j.runtime.query.grammar.impl.MatchesPredicateImpl;
import org.qi4j.runtime.query.grammar.impl.NegationImpl;
import org.qi4j.runtime.query.grammar.impl.NotEqualsPredicateImpl;
import org.qi4j.runtime.query.grammar.impl.OrderByImpl;
import org.qi4j.runtime.query.grammar.impl.PropertyIsNotNullPredicateImpl;
import org.qi4j.runtime.query.grammar.impl.PropertyIsNullPredicateImpl;
import org.qi4j.runtime.query.grammar.impl.PropertyReferenceImpl;
import org.qi4j.runtime.query.grammar.impl.SingleValueExpressionImpl;
import org.qi4j.runtime.query.grammar.impl.VariableValueExpressionImpl;
import org.qi4j.runtime.query.proxy.ManyAssociationReferenceProxy;
import org.qi4j.runtime.query.proxy.MixinTypeProxy;

import static java.lang.reflect.Proxy.*;
import static org.qi4j.api.util.NullArgumentException.*;

public class QueryExpressionsProviderImpl
    implements QueryExpressionsProvider
{
    private static Method identity;

    static
    {
        try
        {
            identity = Identity.class.getMethod( "identity" );
        }
        catch( NoSuchMethodException e )
        {
            e.printStackTrace();
        }
    }

    /**
     * Creates a template for the a mixin type to be used to access properties in type safe fashion.
     *
     * @param mixinType mixin type
     *
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

    @SuppressWarnings( "unchecked" )
    public <T> T templateFor( Class<T> mixinType, Object associatedEntity )
    {
        MixinTypeProxy proxy = (MixinTypeProxy) Proxy.getInvocationHandler( associatedEntity );

        return (T) newProxyInstance(
            QueryExpressions.class.getClassLoader(),
            new Class[]{ mixinType },
            new MixinTypeProxy( mixinType, proxy.traversedAssociation() ) );
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

    public <T> EqualsPredicate<T> newEqualsPredicate( PropertyReference<T> tPropertyReference,
                                                      SingleValueExpression<T> tStaticValueExpression
    )
    {
        return new EqualsPredicateImpl<T>( tPropertyReference, tStaticValueExpression );
    }

    public <T> EqualsPredicate<T> newEqualsPredicate( PropertyReference<T> tPropertyReference,
                                                      VariableValueExpression<T> valueExpression
    )
    {
        return new EqualsPredicateImpl<T>( tPropertyReference, valueExpression );
    }

    public <T> EqualsPredicate<String> newEqualsPredicate( AssociationReference tAssociationReference,
                                                           SingleValueExpression<T> tStaticValueExpression
    )
    {
        Identity id = (Identity) tStaticValueExpression.value();
        SingleValueExpression<String> idExpression = new SingleValueExpressionImpl<String>( id.identity().get() );
        return new EqualsPredicateImpl<String>( new PropertyReferenceImpl<String>( identity, tAssociationReference, null ), idExpression );
    }

    public <T> EqualsPredicate<T> newEqualsPredicate( AssociationReference tAssociationReference,
                                                      VariableValueExpression<T> valueExpression
    )
    {
        return null; // TODO
    }

    public <T> NotEqualsPredicate<T> newNotEqualsPredicate( PropertyReference<T> tPropertyReference,
                                                            SingleValueExpression<T> tStaticValueExpression
    )
    {
        return new NotEqualsPredicateImpl<T>( tPropertyReference, tStaticValueExpression );
    }

    public <T> NotEqualsPredicate<T> newNotEqualsPredicate( PropertyReference<T> tPropertyReference,
                                                            VariableValueExpression<T> valueExpression
    )
    {
        return new NotEqualsPredicateImpl<T>( tPropertyReference, valueExpression );
    }

    public <T> LessThanPredicate<T> newLessThanPredicate( PropertyReference<T> tPropertyReference,
                                                          SingleValueExpression<T> tStaticValueExpression
    )
    {
        return new LessThanPredicateImpl<T>( tPropertyReference, tStaticValueExpression );
    }

    public <T> LessThanPredicate<T> newLessThanPredicate( PropertyReference<T> tPropertyReference,
                                                          VariableValueExpression<T> valueExpression
    )
    {
        return new LessThanPredicateImpl<T>( tPropertyReference, valueExpression );
    }

    public <T> LessOrEqualPredicate<T> newLessOrEqualPredicate( PropertyReference<T> tPropertyReference,
                                                                SingleValueExpression<T> tStaticValueExpression
    )
    {
        return new LessOrEqualPredicateImpl<T>( tPropertyReference, tStaticValueExpression );
    }

    public <T> LessOrEqualPredicate<T> newLessOrEqualPredicate( PropertyReference<T> tPropertyReference,
                                                                VariableValueExpression<T> valueExpression
    )
    {
        return new LessOrEqualPredicateImpl<T>( tPropertyReference, valueExpression );
    }

    public <T> GreaterThanPredicate<T> newGreaterThanPredicate( PropertyReference<T> tPropertyReference,
                                                                SingleValueExpression<T> tStaticValueExpression
    )
    {
        return new GreaterThanPredicateImpl<T>( tPropertyReference, tStaticValueExpression );
    }

    public <T> GreaterThanPredicate<T> newGreaterThanPredicate( PropertyReference<T> tPropertyReference,
                                                                VariableValueExpression<T> valueExpression
    )
    {
        return new GreaterThanPredicateImpl<T>( tPropertyReference, valueExpression );
    }

    public <T> GreaterOrEqualPredicate<T> newGreaterOrEqualPredicate( PropertyReference<T> tPropertyReference,
                                                                      SingleValueExpression<T> tStaticValueExpression
    )
    {
        return new GreaterOrEqualPredicateImpl<T>( tPropertyReference, tStaticValueExpression );
    }

    public <T> GreaterOrEqualPredicate<T> newGreaterOrEqualPredicate( PropertyReference<T> tPropertyReference,
                                                                      VariableValueExpression<T> valueExpression
    )
    {
        return new GreaterOrEqualPredicateImpl<T>( tPropertyReference, valueExpression );
    }

    public MatchesPredicate newMatchesPredicate( PropertyReference<String> stringPropertyReference,
                                                 SingleValueExpression<String> stringSingleValueExpression
    )
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

    public <T> ContainsAllPredicate<T> newContainsAllPredicate( PropertyReference<Collection<T>> propertyRef,
                                                                SingleValueExpression<Collection<T>> collectionValues
    )
    {
        return new ContainsAllPredicateImpl<T>( propertyRef, collectionValues );
    }

    public <T> ContainsPredicate<T> newContainsPredicate( PropertyReference<Collection<T>> propertyRef,
                                                          SingleValueExpression<T> singleValueExpression
    )
    {
        return new ContainsPredicateImpl<T>( propertyRef, singleValueExpression );
    }

    public <T> ManyAssociationContainsPredicate<T> newManyAssociationContainsPredicate( ManyAssociationReference associationReference,
                                                                                        SingleValueExpression<T> singleValueExpression
    )
    {
        return new ManyAssociationContainsPredicateImpl<T>( associationReference, singleValueExpression );
    }
}