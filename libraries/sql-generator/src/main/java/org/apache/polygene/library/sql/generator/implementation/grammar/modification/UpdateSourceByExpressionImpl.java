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
package org.apache.polygene.library.sql.generator.implementation.grammar.modification;

import java.util.Objects;
import org.apache.polygene.library.sql.generator.grammar.common.ValueExpression;
import org.apache.polygene.library.sql.generator.grammar.modification.UpdateSource;
import org.apache.polygene.library.sql.generator.grammar.modification.UpdateSourceByExpression;
import org.apache.polygene.library.sql.generator.implementation.grammar.common.SQLSyntaxElementBase;
import org.apache.polygene.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;

/**
 *
 */
public class UpdateSourceByExpressionImpl extends SQLSyntaxElementBase<UpdateSource, UpdateSourceByExpression>
    implements UpdateSourceByExpression
{

    private final ValueExpression _valueExpression;

    public UpdateSourceByExpressionImpl( SQLProcessorAggregator processor, ValueExpression valueExpression )
    {
        this( processor, UpdateSourceByExpression.class, valueExpression );
    }

    protected UpdateSourceByExpressionImpl( SQLProcessorAggregator processor,
                                            Class<? extends UpdateSourceByExpression> expressionClass, ValueExpression valueExpression )
    {
        super( processor, expressionClass );
        Objects.requireNonNull( valueExpression, "expression" );
        this._valueExpression = valueExpression;
    }

    public ValueExpression getValueExpression()
    {
        return this._valueExpression;
    }

    @Override
    protected boolean doesEqual( UpdateSourceByExpression another )
    {
        return this._valueExpression.equals( another.getValueExpression() );
    }
}
