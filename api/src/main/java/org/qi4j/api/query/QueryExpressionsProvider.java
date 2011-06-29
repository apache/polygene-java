/*
 * Copyright 2008 Niclas Hedhman.
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
package org.qi4j.api.query;

import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.query.grammar.*;

import java.util.Collection;

/**
 * SPI interface for QueryExpressionsProviders
 */
public interface QueryExpressionsProvider
{
    <T> T templateFor( Class<T> mixinType );

    <T> T templateFor( Class<T> mixinType, Object associatedEntity );

    <T> VariableValueExpression<T> newVariableValueExpression( String name );

    <T> PropertyIsNullPredicate<T> newPropertyIsNullPredicate( PropertyReference<T> propertyRef );

    AssociationIsNullPredicate newAssociationIsNullPredicate( AssociationReference associationReference );

    <T> PropertyIsNotNullPredicate<T> newPropertyIsNotNullPredicate( PropertyReference<T> propertyRef );

    AssociationIsNotNullPredicate newAssociationIsNotNullPredicate( AssociationReference associationRef );

    <T> EqualsPredicate<T> newEqualsPredicate( PropertyReference<T> propertyRef,
                                               SingleValueExpression<T> staticValueExpr
    );

    <T> EqualsPredicate<T> newEqualsPredicate( PropertyReference<T> propertyRef,
                                               VariableValueExpression<T> variableValueExpr
    );

    <T> EqualsPredicate<String> newEqualsPredicate( AssociationReference associationRef,
                                                    SingleValueExpression<T> staticValueExpr
    );

    <T> EqualsPredicate<String> newEqualsPredicate( AssociationReference associationRef,
                                                    VariableValueExpression<T> variableValueExpr
    );

    <T> NotEqualsPredicate<T> newNotEqualsPredicate( PropertyReference<T> propertyRef,
                                                     SingleValueExpression<T> staticValueExpr
    );

    <T> NotEqualsPredicate<T> newNotEqualsPredicate( PropertyReference<T> propertyRef,
                                                     VariableValueExpression<T> variableValueExpr
    );

    <T> LessThanPredicate<T> newLessThanPredicate( PropertyReference<T> propertyRef,
                                                   SingleValueExpression<T> staticValueExpr
    );

    <T> LessThanPredicate<T> newLessThanPredicate( PropertyReference<T> propertyRef,
                                                   VariableValueExpression<T> variableValueExpr
    );

    <T> LessOrEqualPredicate<T> newLessOrEqualPredicate( PropertyReference<T> propertyRef,
                                                         SingleValueExpression<T> staticValueExpr
    );

    <T> LessOrEqualPredicate<T> newLessOrEqualPredicate( PropertyReference<T> propertyRef,
                                                         VariableValueExpression<T> variableValueExpr
    );

    <T> GreaterThanPredicate<T> newGreaterThanPredicate( PropertyReference<T> propertyRef,
                                                         SingleValueExpression<T> staticValueExpr
    );

    <T> GreaterThanPredicate<T> newGreaterThanPredicate( PropertyReference<T> propertyRef,
                                                         VariableValueExpression<T> variableValueExpr
    );

    <T> GreaterOrEqualPredicate<T> newGreaterOrEqualPredicate( PropertyReference<T> propertyRef,
                                                               SingleValueExpression<T> staticValueExpr
    );

    <T> GreaterOrEqualPredicate<T> newGreaterOrEqualPredicate( PropertyReference<T> propertyRef,
                                                               VariableValueExpression<T> variableValueExpr
    );

    MatchesPredicate newMatchesPredicate( PropertyReference<String> stringPropertyReference,
                                          SingleValueExpression<String> stringSingleValueExpression
    );

    Conjunction newConjunction( BooleanExpression left, BooleanExpression right );

    Disjunction newDisjunction( BooleanExpression left, BooleanExpression right );

    Negation newNegation( BooleanExpression expression );

    OrderBy newOrderBy( PropertyReference<?> propertyRef, OrderBy.Order order );

    <T> SingleValueExpression<T> newSingleValueExpression( T value );

    <T> T oneOf( ManyAssociation<T> association );

    <T, C extends Collection<T>> ContainsAllPredicate<T, C> newContainsAllPredicate( PropertyReference<C> propertyRef,
                                                                                     SingleValueExpression<C> collectionValues
    );

    <T, C extends Collection<T>> ContainsPredicate<T, C> newContainsPredicate( PropertyReference<C> propertyRef,
                                                                               SingleValueExpression<T> singleValueExpression
    );

    <T> ManyAssociationContainsPredicate<T> newManyAssociationContainsPredicate(
        ManyAssociationReference associationRef, SingleValueExpression<T> singleValueExpression
    );
}
