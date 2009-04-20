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

/**
 * SPI interface for QueryExpressionsProviders
 */
public interface QueryExpressionsProvider
{
    <T> T templateFor( Class<T> mixinType );

    <T> VariableValueExpression<T> newVariableValueExpression( String name );

    <T> PropertyIsNullPredicate<T> newPropertyIsNullPredicate( PropertyReference<T> tPropertyReference );

    AssociationIsNullPredicate newAssociationIsNullPredicate( AssociationReference associationReference );

    <T> PropertyIsNotNullPredicate<T> newPropertyIsNotNullPredicate( PropertyReference<T> tPropertyReference );

    AssociationIsNotNullPredicate newAssociationIsNotNullPredicate( AssociationReference associationReference );

    <T> EqualsPredicate<T> newEqualsPredicate( PropertyReference<T> tPropertyReference, SingleValueExpression<T> tStaticValueExpression );

    <T> EqualsPredicate<T> newEqualsPredicate( PropertyReference<T> tPropertyReference, VariableValueExpression<T> valueExpression );

    <T> EqualsPredicate<T> newEqualsPredicate( AssociationReference tAssociationReference, SingleValueExpression<T> tStaticValueExpression );

    <T> EqualsPredicate<T> newEqualsPredicate( AssociationReference tAssociationReference, VariableValueExpression<T> valueExpression );

    <T> NotEqualsPredicate<T> newNotEqualsPredicate( PropertyReference<T> tPropertyReference, SingleValueExpression<T> tStaticValueExpression );

    <T> NotEqualsPredicate<T> newNotEqualsPredicate( PropertyReference<T> tPropertyReference, VariableValueExpression<T> valueExpression );

    <T> LessThanPredicate<T> newLessThanPredicate( PropertyReference<T> tPropertyReference, SingleValueExpression<T> tStaticValueExpression );

    <T> LessThanPredicate<T> newLessThanPredicate( PropertyReference<T> tPropertyReference, VariableValueExpression<T> valueExpression );

    <T> LessOrEqualPredicate<T> newLessOrEqualPredicate( PropertyReference<T> tPropertyReference, SingleValueExpression<T> tStaticValueExpression );

    <T> LessOrEqualPredicate<T> newLessOrEqualPredicate( PropertyReference<T> tPropertyReference, VariableValueExpression<T> valueExpression );

    <T> GreaterThanPredicate<T> newGreaterThanPredicate( PropertyReference<T> tPropertyReference, SingleValueExpression<T> tStaticValueExpression );

    <T> GreaterThanPredicate<T> newGreaterThanPredicate( PropertyReference<T> tPropertyReference, VariableValueExpression<T> valueExpression );

    <T> GreaterOrEqualPredicate<T> newGreaterOrEqualPredicate( PropertyReference<T> tPropertyReference, SingleValueExpression<T> tStaticValueExpression );

    <T> GreaterOrEqualPredicate<T> newGreaterOrEqualPredicate( PropertyReference<T> tPropertyReference, VariableValueExpression<T> valueExpression );

    MatchesPredicate newMatchesPredicate( PropertyReference<String> stringPropertyReference, SingleValueExpression<String> stringSingleValueExpression );

    Conjunction newConjunction( BooleanExpression left, BooleanExpression right );

    Disjunction newDisjunction( BooleanExpression left, BooleanExpression right );

    Negation newNegation( BooleanExpression expression );

    OrderBy newOrderBy( PropertyReference<?> tPropertyReference, OrderBy.Order order );

    <T> SingleValueExpression<T> newSingleValueExpression( T value );

    <T> T oneOf( ManyAssociation<T> association );
}

