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
package org.apache.polygene.library.sql.generator.implementation.transformation;

import java.util.Iterator;
import java.util.Objects;
import org.apache.polygene.library.sql.generator.grammar.booleans.BinaryPredicate;
import org.apache.polygene.library.sql.generator.grammar.booleans.BooleanExpression;
import org.apache.polygene.library.sql.generator.grammar.booleans.BooleanTest;
import org.apache.polygene.library.sql.generator.grammar.booleans.BooleanTest.TestType;
import org.apache.polygene.library.sql.generator.grammar.booleans.Conjunction;
import org.apache.polygene.library.sql.generator.grammar.booleans.Disjunction;
import org.apache.polygene.library.sql.generator.grammar.booleans.MultiPredicate;
import org.apache.polygene.library.sql.generator.grammar.booleans.Negation;
import org.apache.polygene.library.sql.generator.grammar.booleans.UnaryPredicate;
import org.apache.polygene.library.sql.generator.grammar.common.NonBooleanExpression;
import org.apache.polygene.library.sql.generator.grammar.common.SQLConstants;
import org.apache.polygene.library.sql.generator.grammar.query.QueryExpression;
import org.apache.polygene.library.sql.generator.implementation.grammar.booleans.BooleanUtils;
import org.apache.polygene.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;

/**
 * Currently not thread-safe.
 *
 * @author Stanislav Muhametsin
 */
public class BooleanExpressionProcessing
{
    public static class UnaryPredicateProcessor extends AbstractProcessor<UnaryPredicate>
    {
        public enum UnaryOperatorOrientation
        {
            BEFORE_EXPRESSION, // After expression
            AFTER_EXPRESSION
            // Before expression
        }

        public static final UnaryOperatorOrientation DEFAULT_ORIENTATION = UnaryOperatorOrientation.AFTER_EXPRESSION;

        private final UnaryOperatorOrientation _orientation;
        private final String _operator;

        public UnaryPredicateProcessor( String operator )
        {
            this( DEFAULT_ORIENTATION, operator );
        }

        public UnaryPredicateProcessor( UnaryOperatorOrientation unaryOrientation, String unaryOperator )
        {
            super( UnaryPredicate.class );
            Objects.requireNonNull( unaryOrientation, "unary operator orientation" );
            Objects.requireNonNull( unaryOperator, "unary operator" );
            this._orientation = unaryOrientation;
            this._operator = unaryOperator;
        }

        protected void doProcess( SQLProcessorAggregator processor, UnaryPredicate predicate, StringBuilder builder )
        {
            UnaryOperatorOrientation orientation = this._orientation;
            if( orientation == UnaryOperatorOrientation.BEFORE_EXPRESSION )
            {
                builder.append( this._operator ).append( SQLConstants.TOKEN_SEPARATOR );
            }

            NonBooleanExpression exp = predicate.getValueExpression();
            Boolean isQuery = exp instanceof QueryExpression;
            if( isQuery )
            {
                builder.append( SQLConstants.OPEN_PARENTHESIS );
            }
            processor.process( exp, builder );
            if( isQuery )
            {
                builder.append( SQLConstants.CLOSE_PARENTHESIS );
            }

            if( orientation == UnaryOperatorOrientation.AFTER_EXPRESSION )
            {
                builder.append( SQLConstants.TOKEN_SEPARATOR ).append( this._operator );
            }
        }
    }

    public static class BinaryPredicateProcessor extends AbstractProcessor<BinaryPredicate>
    {
        private final String _operator;

        public BinaryPredicateProcessor( String binaryOperator )
        {
            super( BinaryPredicate.class );
            Objects.requireNonNull( binaryOperator, "binary operator" );

            this._operator = binaryOperator;
        }

        @Override
        protected void doProcess( SQLProcessorAggregator processor, BinaryPredicate predicate, StringBuilder builder )
        {
            processor.process( predicate.getLeft(), builder );
            builder.append( SQLConstants.TOKEN_SEPARATOR ).append( this._operator )
                   .append( SQLConstants.TOKEN_SEPARATOR );
            processor.process( predicate.getRight(), builder );
        }
    }

    public static class MultiPredicateProcessor extends AbstractProcessor<MultiPredicate>
    {
        private final String _operator;
        private final String _separator;
        private final Boolean _needParenthesis;

        public MultiPredicateProcessor( String multiOperator, String multiSeparator, Boolean needParenthesis )
        {
            super( MultiPredicate.class );
            Objects.requireNonNull( multiOperator, "multi-operator" );
            Objects.requireNonNull( multiSeparator, "multi separator" );
            Objects.requireNonNull( needParenthesis, "need parenthesis" );

            this._operator = multiOperator;
            this._separator = multiSeparator;
            this._needParenthesis = needParenthesis;
        }

        @Override
        protected void doProcess( SQLProcessorAggregator processor, MultiPredicate predicate, StringBuilder builder )
        {
            processor.process( predicate.getLeft(), builder );
            builder.append( SQLConstants.TOKEN_SEPARATOR ).append( this._operator )
                   .append( SQLConstants.TOKEN_SEPARATOR );
            if( this._needParenthesis )
            {
                builder.append( SQLConstants.OPEN_PARENTHESIS );
            }

            Iterator<NonBooleanExpression> iter = predicate.getRights().iterator();
            while( iter.hasNext() )
            {
                NonBooleanExpression next = iter.next();
                Boolean isQuery = next instanceof QueryExpression;

                if( isQuery )
                {
                    builder.append( SQLConstants.OPEN_PARENTHESIS );
                }

                processor.process( next, builder );

                if( isQuery )
                {
                    builder.append( SQLConstants.CLOSE_PARENTHESIS );
                }

                if( iter.hasNext() )
                {
                    builder.append( this._separator );
                }
            }

            if( this._needParenthesis )
            {
                builder.append( SQLConstants.CLOSE_PARENTHESIS );
            }
        }
    }

    public static class NegationProcessor extends AbstractProcessor<Negation>
    {
        public NegationProcessor()
        {
            super( Negation.class );
        }

        @Override
        protected void doProcess( SQLProcessorAggregator aggregator, Negation object, StringBuilder builder )
        {
            BooleanExpression negated = object.getNegated();
            if( !BooleanUtils.isEmpty( negated ) )
            {
                builder.append( SQLConstants.NOT ).append( SQLConstants.TOKEN_SEPARATOR );
                aggregator.process( negated, builder );
            }
        }
    }

    public static void processBinaryComposedObject( SQLProcessorAggregator aggregator, BooleanExpression left,
                                                    BooleanExpression right, StringBuilder builder, String operator )
    {
        Boolean leftEmpty = BooleanUtils.isEmpty( left );
        Boolean rightEmpty = BooleanUtils.isEmpty( right );
        if( !leftEmpty || !rightEmpty )
        {
            Boolean oneEmpty = leftEmpty || rightEmpty;
            if( !oneEmpty )
            {
                builder.append( SQLConstants.OPEN_PARENTHESIS );
            }
            aggregator.process( left, builder );

            if( !oneEmpty )
            {
                if( !leftEmpty )
                {
                    builder.append( SQLConstants.TOKEN_SEPARATOR );
                }
                builder.append( operator );
                if( !rightEmpty )
                {
                    builder.append( SQLConstants.TOKEN_SEPARATOR );
                }
            }

            aggregator.process( right, builder );
            if( !oneEmpty )
            {
                builder.append( SQLConstants.CLOSE_PARENTHESIS );
            }
        }
    }

    public static class ConjunctionProcessor extends AbstractProcessor<Conjunction>
    {

        public ConjunctionProcessor()
        {
            super( Conjunction.class );
        }

        @Override
        protected void doProcess( SQLProcessorAggregator aggregator, Conjunction object, StringBuilder builder )
        {
            processBinaryComposedObject( aggregator, object.getLeft(), object.getRight(), builder, SQLConstants.AND );
        }
    }

    public static class DisjunctionProcessor extends AbstractProcessor<Disjunction>
    {
        public DisjunctionProcessor()
        {
            super( Disjunction.class );
        }

        @Override
        protected void doProcess( SQLProcessorAggregator aggregator, Disjunction object, StringBuilder builder )
        {
            processBinaryComposedObject( aggregator, object.getLeft(), object.getRight(), builder, SQLConstants.OR );
        }
    }

    public static class BooleanTestProcessor extends AbstractProcessor<BooleanTest>
    {
        public BooleanTestProcessor()
        {
            super( BooleanTest.class );
        }

        @Override
        protected void doProcess( SQLProcessorAggregator aggregator, BooleanTest object, StringBuilder builder )
        {
            BooleanExpression testable = object.getBooleanExpression();
            builder.append( SQLConstants.OPEN_PARENTHESIS );
            aggregator.process( testable, builder );
            builder.append( SQLConstants.CLOSE_PARENTHESIS ).append( SQLConstants.IS )
                   .append( SQLConstants.TOKEN_SEPARATOR );
            if( object.getTestType() == TestType.IS_NOT )
            {
                builder.append( SQLConstants.NOT ).append( SQLConstants.TOKEN_SEPARATOR );
            }

            builder.append( object.getTruthValue().toString() );
        }
    }
}
