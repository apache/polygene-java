/*
 * Copyright (c) 2010, Stanislav Muhametsin. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.qi4j.index.sql.support.postgresql.internal;

import java.util.List;

import org.qi4j.api.injection.scope.This;
import org.qi4j.api.query.grammar.BooleanExpression;
import org.qi4j.api.query.grammar.OrderBy;
import org.qi4j.api.service.Activatable;
import org.qi4j.index.sql.support.skeletons.AbstractSQLQuerying;
import org.qi4j.index.sql.support.skeletons.SQLDBState;
import org.sql.generation.api.grammar.builders.query.QuerySpecificationBuilder;
import org.sql.generation.api.grammar.builders.query.pgsql.PgSQLQuerySpecificationBuilder;
import org.sql.generation.api.grammar.factories.pgsql.PgSQLQueryFactory;
import org.sql.generation.api.grammar.query.QueryExpression;
import org.sql.generation.api.vendor.PostgreSQLVendor;
import org.sql.generation.api.vendor.SQLVendor;
import org.sql.generation.api.vendor.SQLVendorProvider;

/**
 * 
 * @author Stanislav Muhametsin
 */
public class PostgreSQLQuerying extends AbstractSQLQuerying
    implements Activatable
{

    @This
    private SQLDBState _state;

    public void activate()
        throws Exception
    {
        this._state.sqlVendor().set( SQLVendorProvider.createVendor( PostgreSQLVendor.class ) );
    }

    public void passivate()
        throws Exception
    {

    }

    @Override
    protected QueryExpression finalizeQuery( //
        SQLVendor sqlVendor, QuerySpecificationBuilder specBuilder, //
        Class<?> resultType, //
        BooleanExpression whereClause, //
        OrderBy[] orderBySegments, //
        Integer firstResult, //
        Integer maxResults, //
        List<Object> values, //
        List<Integer> valueSQLTypes, //
        Boolean countOnly )
    {
        PgSQLQuerySpecificationBuilder builder = (PgSQLQuerySpecificationBuilder) specBuilder;
        PostgreSQLVendor vendor = (PostgreSQLVendor) sqlVendor;
        Boolean needOffset = firstResult != null && firstResult > 0;
        Boolean needLimit = maxResults != null && maxResults > 0;

        PgSQLQueryFactory q = vendor.getQueryFactory();

        if( needOffset )
        {
            builder.offset( q.offset( firstResult ) );
        }
        if( needLimit )
        {
            builder.limit( q.limit( maxResults ) );
        }

        builder.setOrderByToFirstColumnIfOffsetOrLimit( q );

        return q.createQuery( builder.createExpression() );
    }

}
