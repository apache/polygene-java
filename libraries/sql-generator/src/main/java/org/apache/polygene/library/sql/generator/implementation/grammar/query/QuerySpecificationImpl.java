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
import org.apache.polygene.library.sql.generator.grammar.booleans.BooleanExpression;
import org.apache.polygene.library.sql.generator.grammar.query.FromClause;
import org.apache.polygene.library.sql.generator.grammar.query.GroupByClause;
import org.apache.polygene.library.sql.generator.grammar.query.LimitSpecification;
import org.apache.polygene.library.sql.generator.grammar.query.OffsetSpecification;
import org.apache.polygene.library.sql.generator.grammar.query.OrderByClause;
import org.apache.polygene.library.sql.generator.grammar.query.QuerySpecification;
import org.apache.polygene.library.sql.generator.grammar.query.SelectColumnClause;
import org.apache.polygene.library.sql.generator.implementation.TypeableImpl;
import org.apache.polygene.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;

/**
 * @author Stanislav Muhametsin
 */
public class QuerySpecificationImpl extends QueryExpressionBodyImpl<QuerySpecification>
    implements QuerySpecification
{

    private final SelectColumnClause _select;

    private final FromClause _from;

    private final BooleanExpression _where;

    private final GroupByClause _groupBy;

    private final BooleanExpression _having;

    private final OrderByClause _orderBy;

    private final OffsetSpecification _offset;

    private final LimitSpecification _limit;

    public QuerySpecificationImpl( SQLProcessorAggregator processor, SelectColumnClause select, FromClause from,
                                   BooleanExpression where, GroupByClause groupBy, BooleanExpression having, OrderByClause orderBy,
                                   OffsetSpecification offset, LimitSpecification limit )
    {
        this( processor, QuerySpecification.class, select, from, where, groupBy, having, orderBy, offset, limit );
    }

    protected QuerySpecificationImpl( SQLProcessorAggregator processor, Class<? extends QuerySpecification> queryClass,
                                      SelectColumnClause select, FromClause from, BooleanExpression where, GroupByClause groupBy,
                                      BooleanExpression having, OrderByClause orderBy, OffsetSpecification offset, LimitSpecification limit )
    {
        super( processor, queryClass );
        Objects.requireNonNull( select, "select" );
        this._select = select;
        this._from = from;
        this._where = where;
        this._groupBy = groupBy;
        this._having = having;
        this._orderBy = orderBy;
        this._offset = offset;
        this._limit = limit;
    }

    public SelectColumnClause getColumns()
    {
        return this._select;
    }

    public FromClause getFrom()
    {
        return this._from;
    }

    public BooleanExpression getWhere()
    {
        return this._where;
    }

    public GroupByClause getGroupBy()
    {
        return this._groupBy;
    }

    public BooleanExpression getHaving()
    {
        return this._having;
    }

    public OrderByClause getOrderBy()
    {
        return this._orderBy;
    }

    public LimitSpecification getLimitSpecification()
    {
        return this._limit;
    }

    public OffsetSpecification getOffsetSpecification()
    {
        return this._offset;
    }

    @Override
    protected boolean doesEqual( QuerySpecification another )
    {
        return this._select.equals( another.getColumns() )
               && TypeableImpl.bothNullOrEquals( this._from, another.getFrom() )
               && TypeableImpl.bothNullOrEquals( this._where, another.getWhere() )
               && TypeableImpl.bothNullOrEquals( this._groupBy, another.getGroupBy() )
               && TypeableImpl.bothNullOrEquals( this._having, another.getHaving() )
               && TypeableImpl.bothNullOrEquals( this._orderBy, another.getOrderBy() );
    }
}
