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
package org.apache.polygene.library.sql.generator.implementation.grammar.factories;

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
import org.apache.polygene.library.sql.generator.implementation.grammar.booleans.BetweenPredicateImpl;
import org.apache.polygene.library.sql.generator.implementation.grammar.booleans.BooleanTestImpl;
import org.apache.polygene.library.sql.generator.implementation.grammar.booleans.ConjunctionImpl;
import org.apache.polygene.library.sql.generator.implementation.grammar.booleans.DisjunctionImpl;
import org.apache.polygene.library.sql.generator.implementation.grammar.booleans.EqualsPredicateImpl;
import org.apache.polygene.library.sql.generator.implementation.grammar.booleans.ExistsPredicateImpl;
import org.apache.polygene.library.sql.generator.implementation.grammar.booleans.GreaterOrEqualPredicateImpl;
import org.apache.polygene.library.sql.generator.implementation.grammar.booleans.GreaterThanPredicateImpl;
import org.apache.polygene.library.sql.generator.implementation.grammar.booleans.IsNotNullPredicateImpl;
import org.apache.polygene.library.sql.generator.implementation.grammar.booleans.IsNullPredicateImpl;
import org.apache.polygene.library.sql.generator.implementation.grammar.booleans.LessOrEqualPredicateImpl;
import org.apache.polygene.library.sql.generator.implementation.grammar.booleans.LessThanPredicateImpl;
import org.apache.polygene.library.sql.generator.implementation.grammar.booleans.LikePredicateImpl;
import org.apache.polygene.library.sql.generator.implementation.grammar.booleans.NegationImpl;
import org.apache.polygene.library.sql.generator.implementation.grammar.booleans.NotBetweenPredicateImpl;
import org.apache.polygene.library.sql.generator.implementation.grammar.booleans.NotEqualsPredicateImpl;
import org.apache.polygene.library.sql.generator.implementation.grammar.booleans.NotInPredicateImpl;
import org.apache.polygene.library.sql.generator.implementation.grammar.booleans.NotLikePredicateImpl;
import org.apache.polygene.library.sql.generator.implementation.grammar.booleans.NotRegexpPredicateImpl;
import org.apache.polygene.library.sql.generator.implementation.grammar.booleans.RegexpPredicateImpl;
import org.apache.polygene.library.sql.generator.implementation.grammar.booleans.UniquePredicateImpl;
import org.apache.polygene.library.sql.generator.implementation.grammar.builders.booleans.BooleanBuilderImpl;
import org.apache.polygene.library.sql.generator.implementation.grammar.builders.booleans.InBuilderImpl;
import org.apache.polygene.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;
import org.apache.polygene.library.sql.generator.vendor.SQLVendor;

/**
 *
 */
public class DefaultBooleanFactory extends AbstractBooleanFactory
{

    public DefaultBooleanFactory( SQLVendor vendor, SQLProcessorAggregator processor )
    {
        super( vendor, processor );
    }

    public EqualsPredicate eq( NonBooleanExpression left, NonBooleanExpression right )
    {
        return new EqualsPredicateImpl( this.getProcessor(), left, right );
    }

    public NotEqualsPredicate neq( NonBooleanExpression left, NonBooleanExpression right )
    {
        return new NotEqualsPredicateImpl( this.getProcessor(), left, right );
    }

    public LessThanPredicate lt( NonBooleanExpression left, NonBooleanExpression right )
    {
        return new LessThanPredicateImpl( this.getProcessor(), left, right );
    }

    public LessOrEqualPredicate leq( NonBooleanExpression left, NonBooleanExpression right )
    {
        return new LessOrEqualPredicateImpl( this.getProcessor(), left, right );
    }

    public GreaterThanPredicate gt( NonBooleanExpression left, NonBooleanExpression right )
    {
        return new GreaterThanPredicateImpl( this.getProcessor(), left, right );
    }

    public GreaterOrEqualPredicate geq( NonBooleanExpression left, NonBooleanExpression right )
    {
        return new GreaterOrEqualPredicateImpl( this.getProcessor(), left, right );
    }

    public IsNullPredicate isNull( NonBooleanExpression what )
    {
        return new IsNullPredicateImpl( this.getProcessor(), what );
    }

    public IsNotNullPredicate isNotNull( NonBooleanExpression what )
    {
        return new IsNotNullPredicateImpl( this.getProcessor(), what );
    }

    public Negation not( BooleanExpression what )
    {
        return new NegationImpl( this.getProcessor(), what );
    }

    public Conjunction and( BooleanExpression left, BooleanExpression right )
    {
        return new ConjunctionImpl( this.getProcessor(), left, right );
    }

    public Disjunction or( BooleanExpression left, BooleanExpression right )
    {
        return new DisjunctionImpl( this.getProcessor(), left, right );
    }

    public BetweenPredicate between( NonBooleanExpression left, NonBooleanExpression minimum,
                                     NonBooleanExpression maximum )
    {
        return new BetweenPredicateImpl( this.getProcessor(), left, minimum, maximum );
    }

    public NotBetweenPredicate notBetween( NonBooleanExpression left, NonBooleanExpression minimum,
                                           NonBooleanExpression maximum )
    {
        return new NotBetweenPredicateImpl( this.getProcessor(), left, minimum, maximum );
    }

    public InBuilder inBuilder( NonBooleanExpression what )
    {
        return new InBuilderImpl( this.getProcessor(), what );
    }

    public NotInPredicate notIn( NonBooleanExpression what, NonBooleanExpression... values )
    {
        return new NotInPredicateImpl( this.getProcessor(), what, values );
    }

    public LikePredicate like( NonBooleanExpression what, NonBooleanExpression pattern )
    {
        return new LikePredicateImpl( this.getProcessor(), what, pattern );
    }

    public NotLikePredicate notLike( NonBooleanExpression what, NonBooleanExpression pattern )
    {
        return new NotLikePredicateImpl( this.getProcessor(), what, pattern );
    }

    public RegexpPredicate regexp( NonBooleanExpression what, NonBooleanExpression pattern )
    {
        return new RegexpPredicateImpl( this.getProcessor(), what, pattern );
    }

    public NotRegexpPredicate notRegexp( NonBooleanExpression what, NonBooleanExpression pattern )
    {
        return new NotRegexpPredicateImpl( this.getProcessor(), what, pattern );
    }

    public ExistsPredicate exists( QueryExpression query )
    {
        return new ExistsPredicateImpl( this.getProcessor(), query );
    }

    public UniquePredicate unique( QueryExpression query )
    {
        return new UniquePredicateImpl( this.getProcessor(), query );
    }

    public BooleanTest test( BooleanExpression expression, TestType testType, TruthValue truthValue )
    {
        return new BooleanTestImpl( this.getProcessor(), expression, testType, truthValue );
    }

    public BooleanBuilder booleanBuilder( BooleanExpression first )
    {
        return new BooleanBuilderImpl( this.getProcessor(), this, this.transformNullToEmpty( first ) );
    }

    private final BooleanExpression transformNullToEmpty( BooleanExpression expression )
    {
        return expression == null ? Predicate.EmptyPredicate.INSTANCE : expression;
    }
}
