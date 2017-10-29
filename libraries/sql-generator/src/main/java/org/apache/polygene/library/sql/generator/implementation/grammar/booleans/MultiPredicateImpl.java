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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.apache.polygene.library.sql.generator.grammar.booleans.MultiPredicate;
import org.apache.polygene.library.sql.generator.grammar.common.NonBooleanExpression;
import org.apache.polygene.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;

/**
 *
 */
public abstract class MultiPredicateImpl<ExpressionType extends MultiPredicate> extends
                                                                                AbstractBooleanExpression<ExpressionType>
    implements MultiPredicate
{

    private final NonBooleanExpression _left;

    private final List<NonBooleanExpression> _rights;

    protected MultiPredicateImpl( SQLProcessorAggregator processor, Class<? extends ExpressionType> expressionClass,
                                  NonBooleanExpression left, NonBooleanExpression... rights )
    {
        this( processor, expressionClass, left, Arrays.asList( rights ) );
    }

    protected MultiPredicateImpl( SQLProcessorAggregator processor, Class<? extends ExpressionType> expressionClass,
                                  NonBooleanExpression left, List<NonBooleanExpression> rights )
    {
        super( processor, expressionClass );
        Objects.requireNonNull( left, "left" );
        Objects.requireNonNull( rights, "rights" );

        for( NonBooleanExpression exp : rights )
        {
            Objects.requireNonNull( exp, "right" );
        }

        this._left = left;
        this._rights = Collections.unmodifiableList( rights );
    }

    public NonBooleanExpression getLeft()
    {
        return this._left;
    }

    public List<NonBooleanExpression> getRights()
    {
        return this._rights;
    }

    @Override
    protected boolean doesEqual( ExpressionType another )
    {
        return this._left.equals( another.getLeft() ) && this._rights.equals( another.getRights() );
    }
}
