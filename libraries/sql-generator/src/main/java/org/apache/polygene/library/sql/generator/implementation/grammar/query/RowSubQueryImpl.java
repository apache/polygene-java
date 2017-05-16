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
import org.apache.polygene.library.sql.generator.grammar.query.RowSubQuery;
import org.apache.polygene.library.sql.generator.grammar.query.RowValueConstructor;
import org.apache.polygene.library.sql.generator.implementation.grammar.common.SQLSyntaxElementBase;
import org.apache.polygene.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;

/**
 * @author Stanislav Muhametsin
 */
public class RowSubQueryImpl extends SQLSyntaxElementBase<RowValueConstructor, RowSubQuery>
    implements RowSubQuery
{
    private final QueryExpression _queryExpression;

    public RowSubQueryImpl( SQLProcessorAggregator processor, QueryExpression queryExpression )
    {
        this( processor, RowSubQuery.class, queryExpression );
    }

    protected RowSubQueryImpl( SQLProcessorAggregator processor, Class<? extends RowSubQuery> realImplementingType,
                               QueryExpression queryExpression )
    {
        super( processor, realImplementingType );
        Objects.requireNonNull( queryExpression, "query expression" );
        this._queryExpression = queryExpression;
    }

    public QueryExpression getQueryExpression()
    {
        return this._queryExpression;
    }

    @Override
    protected boolean doesEqual( RowSubQuery another )
    {
        return this._queryExpression.equals( another.getQueryExpression() );
    }
}
