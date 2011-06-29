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

package org.qi4j.index.sql.support.postgresql;

import org.qi4j.api.composite.Composite;
import org.qi4j.api.query.grammar.OrderBy;
import org.qi4j.functional.Specification;
import org.qi4j.index.sql.support.skeletons.AbstractSQLQuerying;
import org.sql.generation.api.grammar.builders.query.QuerySpecificationBuilder;
import org.sql.generation.api.grammar.builders.query.pgsql.PgSQLQuerySpecificationBuilder;
import org.sql.generation.api.grammar.factories.pgsql.PgSQLQueryFactory;
import org.sql.generation.api.grammar.query.QueryExpression;
import org.sql.generation.api.vendor.PostgreSQLVendor;
import org.sql.generation.api.vendor.SQLVendor;

import java.util.List;

/**
 * 
 * @author Stanislav Muhametsin
 */
public class PostgreSQLQuerying extends AbstractSQLQuerying
{

    @Override
    protected QueryExpression finalizeQuery( //
        SQLVendor sqlVendor, QuerySpecificationBuilder specBuilder, //
        Class<?> resultType, //
        Specification<Composite> whereClause, //
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

        builder.setOrderByToFirstColumnIfOffsetOrLimit();

        return q.createQuery( builder.createExpression() );
    }

}
