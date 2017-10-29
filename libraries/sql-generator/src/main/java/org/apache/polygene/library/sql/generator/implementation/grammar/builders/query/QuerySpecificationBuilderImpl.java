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
package org.apache.polygene.library.sql.generator.implementation.grammar.builders.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apache.polygene.library.sql.generator.grammar.booleans.Predicate;
import org.apache.polygene.library.sql.generator.grammar.builders.booleans.BooleanBuilder;
import org.apache.polygene.library.sql.generator.grammar.builders.query.ColumnsBuilder;
import org.apache.polygene.library.sql.generator.grammar.builders.query.FromBuilder;
import org.apache.polygene.library.sql.generator.grammar.builders.query.GroupByBuilder;
import org.apache.polygene.library.sql.generator.grammar.builders.query.OrderByBuilder;
import org.apache.polygene.library.sql.generator.grammar.builders.query.QuerySpecificationBuilder;
import org.apache.polygene.library.sql.generator.grammar.common.NonBooleanExpression;
import org.apache.polygene.library.sql.generator.grammar.factories.QueryFactory;
import org.apache.polygene.library.sql.generator.grammar.query.ColumnReference;
import org.apache.polygene.library.sql.generator.grammar.query.ColumnReferences.ColumnReferenceInfo;
import org.apache.polygene.library.sql.generator.grammar.query.GroupingElement;
import org.apache.polygene.library.sql.generator.grammar.query.OrdinaryGroupingSet;
import org.apache.polygene.library.sql.generator.grammar.query.QuerySpecification;
import org.apache.polygene.library.sql.generator.implementation.grammar.query.QuerySpecificationImpl;
import org.apache.polygene.library.sql.generator.implementation.transformation.spi.SQLProcessorAggregator;

/**
 *
 */
public class QuerySpecificationBuilderImpl extends AbstractQueryFactoryImpl<QuerySpecification>
    implements QuerySpecificationBuilder
{

    private ColumnsBuilder _select;

    private FromBuilder _from;

    private BooleanBuilder _where;

    private GroupByBuilder _groupBy;

    private BooleanBuilder _having;

    private OrderByBuilder _orderBy;

    private final QueryFactory _queryFactory;

    public QuerySpecificationBuilderImpl( SQLProcessorAggregator processor, QueryFactory q, ColumnsBuilder select,
                                          FromBuilder from, BooleanBuilder where, GroupByBuilder groupBy, BooleanBuilder having, OrderByBuilder orderBy )
    {
        super( processor );

        Objects.requireNonNull( q, "Query factory" );
        Objects.requireNonNull( select, "select" );
        Objects.requireNonNull( from, "from" );
        Objects.requireNonNull( where, "where" );
        Objects.requireNonNull( groupBy, "group by" );
        Objects.requireNonNull( having, "having" );
        Objects.requireNonNull( orderBy, "order by" );

        this._queryFactory = q;
        this._select = select;
        this._from = from;
        this._where = where;
        this._groupBy = groupBy;
        this._having = having;
        this._orderBy = orderBy;
    }

    public FromBuilder getFrom()
    {
        return this._from;
    }

    public ColumnsBuilder getSelect()
    {
        return this._select;
    }

    public BooleanBuilder getWhere()
    {
        return this._where;
    }

    public GroupByBuilder getGroupBy()
    {
        return this._groupBy;
    }

    public BooleanBuilder getHaving()
    {
        return this._having;
    }

    public OrderByBuilder getOrderBy()
    {
        return this._orderBy;
    }

    public QuerySpecificationBuilder trimGroupBy()
    {
        if( this._having.createExpression() != Predicate.EmptyPredicate.INSTANCE )
        {
            List<ColumnReference> groupByColumns = new ArrayList<ColumnReference>();
            for( GroupingElement element : this._groupBy.getGroupingElements() )
            {
                if( element instanceof OrdinaryGroupingSet )
                {
                    for( NonBooleanExpression exp : ( (OrdinaryGroupingSet) element ).getColumns() )
                    {
                        if( exp instanceof ColumnReference )
                        {
                            groupByColumns.add( (ColumnReference) exp );
                        }
                    }
                }
            }
            for( ColumnReferenceInfo column : this._select.getColumns() )
            {
                Boolean noColumn = true;
                for( ColumnReference groupByColumn : groupByColumns )
                {
                    if( column.getReference().equals( groupByColumn ) )
                    {
                        noColumn = false;
                        break;
                    }
                }

                if( noColumn )
                {
                    this._groupBy.addGroupingElements( this._queryFactory.groupingElement( column.getReference() ) );
                }
            }
        }

        return this;
    }

    public QuerySpecification createExpression()
    {
        return new QuerySpecificationImpl( this.getProcessor(), this._select.createExpression(),
                                           this._from.createExpression(), this._where.createExpression(), this._groupBy.createExpression(),
                                           this._having.createExpression(), this._orderBy.createExpression(), this.getOffset(), this.getLimit() );
    }

    public QuerySpecificationBuilder setSelect( ColumnsBuilder builder )
    {
        Objects.requireNonNull( builder, "builder" );
        this._select = builder;
        return this;
    }

    public QuerySpecificationBuilder setFrom( FromBuilder builder )
    {
        Objects.requireNonNull( builder, "builder" );
        this._from = builder;
        return this;
    }

    public QuerySpecificationBuilder setWhere( BooleanBuilder builder )
    {
        Objects.requireNonNull( builder, "builder" );
        this._where = builder;
        return this;
    }

    public QuerySpecificationBuilder setGroupBy( GroupByBuilder builder )
    {
        Objects.requireNonNull( builder, "builder" );
        this._groupBy = builder;
        return this;
    }

    public QuerySpecificationBuilder setHaving( BooleanBuilder builder )
    {
        Objects.requireNonNull( builder, "builder" );
        this._having = builder;
        return this;
    }

    public QuerySpecificationBuilder setOrderBy( OrderByBuilder builder )
    {
        Objects.requireNonNull( builder, "builder" );
        this._orderBy = builder;
        return this;
    }

    protected QueryFactory getQueryFactory()
    {
        return this._queryFactory;
    }

    @Override
    public QuerySpecificationBuilder limit()
    {
        return (QuerySpecificationBuilder) super.limit();
    }

    @Override
    public QuerySpecificationBuilder limit( Integer max )
    {
        return (QuerySpecificationBuilder) super.limit( max );
    }

    @Override
    public QuerySpecificationBuilder limit( NonBooleanExpression max )
    {
        return (QuerySpecificationBuilder) super.limit( max );
    }

    @Override
    public QuerySpecificationBuilder offset( Integer skip )
    {
        return (QuerySpecificationBuilder) super.offset( skip );
    }

    @Override
    public QuerySpecificationBuilder offset( NonBooleanExpression skip )
    {
        return (QuerySpecificationBuilder) super.offset( skip );
    }
}
