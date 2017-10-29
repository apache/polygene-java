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
package org.apache.polygene.library.sql.generator.implementation.grammar.builders.booleans;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.apache.polygene.library.sql.generator.grammar.booleans.InPredicate;
import org.apache.polygene.library.sql.generator.grammar.builders.booleans.InBuilder;
import org.apache.polygene.library.sql.generator.grammar.common.NonBooleanExpression;
import org.apache.polygene.library.sql.generator.implementation.grammar.booleans.InPredicateImpl;
import org.apache.polygene.library.sql.generator.implementation.grammar.common.SQLBuilderBase;
import org.apache.polygene.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;

/**
 *
 */
public class InBuilderImpl extends SQLBuilderBase
    implements InBuilder
{

    private final NonBooleanExpression _left;

    private final List<NonBooleanExpression> _expressions;

    public InBuilderImpl( SQLProcessorAggregator processor, NonBooleanExpression left )
    {
        super( processor );
        Objects.requireNonNull( left, "left" );

        this._left = left;
        this._expressions = new ArrayList<NonBooleanExpression>();
    }

    public InBuilder addValues( NonBooleanExpression... expressions )
    {
        Objects.requireNonNull( expressions, "expressions" );
        for( NonBooleanExpression exp : expressions )
        {
            Objects.requireNonNull( exp, "expression" );
        }

        this._expressions.addAll( Arrays.asList( expressions ) );
        return this;
    }

    public InPredicate createExpression()
    {
        return new InPredicateImpl( this.getProcessor(), this._left, this._expressions );
    }
}
