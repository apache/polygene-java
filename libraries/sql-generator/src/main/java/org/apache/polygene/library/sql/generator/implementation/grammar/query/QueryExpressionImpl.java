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
import org.apache.polygene.library.sql.generator.grammar.query.QueryExpression;
import org.apache.polygene.library.sql.generator.grammar.query.QueryExpressionBody;
import org.apache.polygene.library.sql.generator.implementation.grammar.common.NonBooleanExpressionImpl;
import org.apache.polygene.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;

/**
 *
 */
public class QueryExpressionImpl extends NonBooleanExpressionImpl<QueryExpression>
    implements QueryExpression
{

    private final QueryExpressionBody _body;

    public QueryExpressionImpl( SQLProcessorAggregator processor, QueryExpressionBody body )
    {
        this( processor, QueryExpression.class, body );
    }

    protected QueryExpressionImpl( SQLProcessorAggregator processor, Class<? extends QueryExpression> implClass,
                                   QueryExpressionBody body )
    {
        super( processor, implClass );
        Objects.requireNonNull( body, "query expression body" );
        this._body = body;
    }

    public QueryExpressionBody getQueryExpressionBody()
    {
        return this._body;
    }

    @Override
    protected boolean doesEqual( QueryExpression another )
    {
        return this._body.equals( another.getQueryExpressionBody() );
    }
}
