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
package org.apache.polygene.library.sql.generator.implementation.grammar.booleans;

import java.util.Objects;
import org.apache.polygene.library.sql.generator.grammar.booleans.UnaryPredicate;
import org.apache.polygene.library.sql.generator.grammar.common.NonBooleanExpression;
import org.apache.polygene.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;

/**
 *
 */
public abstract class UnaryPredicateImpl<ExpressionType extends UnaryPredicate> extends
                                                                                AbstractBooleanExpression<ExpressionType>
    implements UnaryPredicate
{

    private final NonBooleanExpression _expression;

    public UnaryPredicateImpl( SQLProcessorAggregator processor, Class<? extends ExpressionType> expressionClass,
                               NonBooleanExpression expression )
    {
        super( processor, expressionClass );
        Objects.requireNonNull( expression, "expression" );

        this._expression = expression;
    }

    public NonBooleanExpression getValueExpression()
    {
        return this._expression;
    }

    @Override
    protected boolean doesEqual( ExpressionType another )
    {
        return this._expression.equals( another.getValueExpression() );
    }
}
