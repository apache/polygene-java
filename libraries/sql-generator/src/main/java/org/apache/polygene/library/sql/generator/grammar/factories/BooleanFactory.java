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
package org.apache.polygene.library.sql.generator.grammar.factories;

import org.apache.polygene.library.sql.generator.grammar.booleans.BetweenPredicate;
import org.apache.polygene.library.sql.generator.grammar.booleans.BooleanExpression;
import org.apache.polygene.library.sql.generator.grammar.booleans.BooleanTest;
import org.apache.polygene.library.sql.generator.grammar.booleans.BooleanTest.TestType;
import org.apache.polygene.library.sql.generator.grammar.booleans.BooleanTest.TruthValue;
import org.apache.polygene.library.sql.generator.grammar.booleans.Conjunction;
import org.apache.polygene.library.sql.generator.grammar.booleans.Disjunction;
import org.apache.polygene.library.sql.generator.grammar.booleans.EqualsPredicate;
import org.apache.polygene.library.sql.generator.grammar.booleans.ExistsPredicate;
import org.apache.polygene.library.sql.generator.grammar.booleans.GreaterOrEqualPredicate;
import org.apache.polygene.library.sql.generator.grammar.booleans.GreaterThanPredicate;
import org.apache.polygene.library.sql.generator.grammar.booleans.InPredicate;
import org.apache.polygene.library.sql.generator.grammar.booleans.IsNotNullPredicate;
import org.apache.polygene.library.sql.generator.grammar.booleans.IsNullPredicate;
import org.apache.polygene.library.sql.generator.grammar.booleans.LessOrEqualPredicate;
import org.apache.polygene.library.sql.generator.grammar.booleans.LessThanPredicate;
import org.apache.polygene.library.sql.generator.grammar.booleans.LikePredicate;
import org.apache.polygene.library.sql.generator.grammar.booleans.Negation;
import org.apache.polygene.library.sql.generator.grammar.booleans.NotBetweenPredicate;
import org.apache.polygene.library.sql.generator.grammar.booleans.NotEqualsPredicate;
import org.apache.polygene.library.sql.generator.grammar.booleans.NotInPredicate;
import org.apache.polygene.library.sql.generator.grammar.booleans.NotLikePredicate;
import org.apache.polygene.library.sql.generator.grammar.booleans.NotRegexpPredicate;
import org.apache.polygene.library.sql.generator.grammar.booleans.Predicate;
import org.apache.polygene.library.sql.generator.grammar.booleans.RegexpPredicate;
import org.apache.polygene.library.sql.generator.grammar.booleans.UniquePredicate;
import org.apache.polygene.library.sql.generator.grammar.builders.booleans.BooleanBuilder;
import org.apache.polygene.library.sql.generator.grammar.builders.booleans.InBuilder;
import org.apache.polygene.library.sql.generator.grammar.common.NonBooleanExpression;
import org.apache.polygene.library.sql.generator.grammar.query.QueryExpression;
import org.apache.polygene.library.sql.generator.vendor.SQLVendor;

/**
 * A factory for creating various {@link BooleanExpression}s. This factory is obtainable from {@link SQLVendor}.
 *
 * @author Stanislav Muhametsin
 * @see SQLVendor
 */
public interface BooleanFactory
{

    /**
     * Creates new {@link EqualsPredicate}.
     *
     * @param left  The left-side expression.
     * @param right The right-side expression.
     * @return The new {@link EqualsPredicate}.
     */
    EqualsPredicate eq( NonBooleanExpression left, NonBooleanExpression right );

    /**
     * Creates new {@link NotEqualsPredicate}.
     *
     * @param left  The left-side expression.
     * @param right The right-side expression.
     * @return The new {@link NotEqualsPredicate}.
     */
    NotEqualsPredicate neq( NonBooleanExpression left, NonBooleanExpression right );

    /**
     * Creates new {@link LessThanPredicate}.
     *
     * @param left  The left-side expression.
     * @param right The right-side expression.
     * @return The new {@link LessThanPredicate}.
     */
    LessThanPredicate lt( NonBooleanExpression left, NonBooleanExpression right );

    /**
     * Creates new {@link LessOrEqualPredicate}.
     *
     * @param left  The left-side expression.
     * @param right The right-side expression.
     * @return The new {@link LessOrEqualPredicate}.
     */
    LessOrEqualPredicate leq( NonBooleanExpression left, NonBooleanExpression right );

    /**
     * Creates new {@link GreaterThanPredicate}.
     *
     * @param left  The left-side expression.
     * @param right The right-side expression.
     * @return The new {@link GreaterThanPredicate}.
     */
    GreaterThanPredicate gt( NonBooleanExpression left, NonBooleanExpression right );

    /**
     * Creates new {@link GreaterOrEqualPredicate}.
     *
     * @param left  The left-side expression.
     * @param right The right-side expression.
     * @return The new {@link GreaterOrEqualPredicate}.
     */
    GreaterOrEqualPredicate geq( NonBooleanExpression left, NonBooleanExpression right );

    /**
     * Creates new {@link IsNullPredicate}.
     *
     * @param what The expression for the predicate.
     * @return The new {@link IsNullPredicate}.
     */
    IsNullPredicate isNull( NonBooleanExpression what );

    /**
     * Creates new {@link IsNotNullPredicate}.
     *
     * @param what The expression for the predicate.
     * @return The new {@link IsNotNullPredicate}.
     */
    IsNotNullPredicate isNotNull( NonBooleanExpression what );

    /**
     * Creates new {@link Negation}.
     *
     * @param what The expression to be negated.
     * @return The new {@link Negation}.
     */
    Negation not( BooleanExpression what );

    /**
     * Creates new {@link Conjunction}.
     *
     * @param left  The left-side expression.
     * @param right The right-side expression.
     * @return The new {@link Conjunction}.
     */
    Conjunction and( BooleanExpression left, BooleanExpression right );

    /**
     * Creates new {@link Disjunction}.
     *
     * @param left  The left-side expression.
     * @param right The right-side expression.
     * @return The new {@link Disjunction}.
     */
    Disjunction or( BooleanExpression left, BooleanExpression right );

    /**
     * Creates new {@link BetweenPredicate}.
     *
     * @param left    What to be between.
     * @param minimum The minimum value.
     * @param maximum The maximum value.
     * @return The new {@link BetweenPredicate}.
     */
    BetweenPredicate between( NonBooleanExpression left, NonBooleanExpression minimum,
                              NonBooleanExpression maximum );

    /**
     * Creates new {@link NotBetweenPredicate}.
     *
     * @param left    What not to be between.
     * @param minimum The minimum value
     * @param maximum The maximum value.
     * @return The new {@link NotBetweenPredicate}.
     */
    NotBetweenPredicate notBetween( NonBooleanExpression left, NonBooleanExpression minimum,
                                    NonBooleanExpression maximum );

    /**
     * Creates new {@link InPredicate}.
     *
     * @param what   What to be in accepted values.
     * @param values The accepted values.
     * @return The new {@link InPredicate}.
     */
    InPredicate in( NonBooleanExpression what, NonBooleanExpression... values );

    /**
     * Returns a builder for {@link InPredicate}.
     *
     * @param what What to be in accepted values.
     * @return The builder for {@link InPredicate}.
     */
    InBuilder inBuilder( NonBooleanExpression what );

    /**
     * Creates new {@link NotInPredicate}.
     *
     * @param what   What not to be in values.
     * @param values The values.
     * @return The new {@link NotInPredicate}.
     */
    NotInPredicate notIn( NonBooleanExpression what, NonBooleanExpression... values );

    /**
     * Creates new {@link LikePredicate}.
     *
     * @param what    What to be like something.
     * @param pattern The pattern to match.
     * @return The new {@link LikePredicate}
     */
    LikePredicate like( NonBooleanExpression what, NonBooleanExpression pattern );

    /**
     * Creates new {@link NotLikePredicate}.
     *
     * @param what    What not to be like something.
     * @param pattern The pattern.
     * @return The new {@link NotLikePredicate}.
     */
    NotLikePredicate notLike( NonBooleanExpression what, NonBooleanExpression pattern );

    /**
     * Creates new {@link RegexpPredicate}.
     *
     * @param what    What to match.
     * @param pattern The pattern to match.
     * @return The new {@link NotRegexpPredicate}.
     */
    RegexpPredicate regexp( NonBooleanExpression what, NonBooleanExpression pattern );

    /**
     * Creates new {@link NotRegexpPredicate}.
     *
     * @param what    What would be not matching the pattern.
     * @param pattern The pattern to use.
     * @return The new {@link NotRegexpPredicate}.
     */
    NotRegexpPredicate notRegexp( NonBooleanExpression what, NonBooleanExpression pattern );

    /**
     * Creates new {@link ExistsPredicate}.
     *
     * @param query A query to use.
     * @return The new {@link ExistsPredicate}.
     */
    ExistsPredicate exists( QueryExpression query );

    /**
     * Creates new {@link UniquePredicate}.
     *
     * @param query A query to use.
     * @return The new {@link UniquePredicate}.
     */
    UniquePredicate unique( QueryExpression query );

    /**
     * Creates new {@link BooleanTest}.
     *
     * @param expression The expresssion to test.
     * @param testType   The test type to use.
     * @param truthValue The truth value to use.
     * @return The new {@link BooleanTest}.
     */
    BooleanTest test( BooleanExpression expression, TestType testType, TruthValue truthValue );

    /**
     * Returns new {@link BooleanBuilder} with {@link Predicate.EmptyPredicate} as initial value.
     *
     * @return The new {@link BooleanBuilder}.
     */
    BooleanBuilder booleanBuilder();

    /**
     * Returns new {@link BooleanBuilder} with given boolean expression as initial value.
     *
     * @param first The initial value for boolean expression.
     * @return The new {@link BooleanBuilder}.
     */
    BooleanBuilder booleanBuilder( BooleanExpression first );
}
