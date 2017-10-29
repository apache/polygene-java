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
package org.apache.polygene.library.sql.generator.implementation.grammar.query;

import java.util.Objects;
import org.apache.polygene.library.sql.generator.grammar.common.ValueExpression;
import org.apache.polygene.library.sql.generator.grammar.query.Ordering;
import org.apache.polygene.library.sql.generator.grammar.query.SortSpecification;
import org.apache.polygene.library.sql.generator.implementation.grammar.common.SQLSyntaxElementBase;
import org.apache.polygene.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;

/**
 *
 */
public class SortSpecificationImpl extends SQLSyntaxElementBase<SortSpecification, SortSpecification>
    implements SortSpecification
{
    private final Ordering _ordering;

    private final ValueExpression _expression;

    public SortSpecificationImpl( SQLProcessorAggregator processor, ValueExpression expression, Ordering ordering )
    {
        this( processor, SortSpecification.class, expression, ordering );
    }

    protected SortSpecificationImpl( SQLProcessorAggregator processor, Class<? extends SortSpecification> implClass,
                                     ValueExpression expression, Ordering ordering )
    {
        super( processor, implClass );
        Objects.requireNonNull( expression, "expression" );
        Objects.requireNonNull( ordering, "ordering" );

        this._expression = expression;
        this._ordering = ordering;
    }

    public Ordering getOrderingSpecification()
    {
        return this._ordering;
    }

    public ValueExpression getValueExpression()
    {
        return this._expression;
    }

    @Override
    protected boolean doesEqual( SortSpecification another )
    {
        return this._ordering.equals( another.getOrderingSpecification() )
               && this._expression.equals( another.getOrderingSpecification() );
    }
}
