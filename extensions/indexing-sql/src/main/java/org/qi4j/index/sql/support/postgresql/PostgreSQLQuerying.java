/*
 * Copyright (c) 2010, Stanislav Muhametsin. All Rights Reserved.
 * Copyright (c) 2012, Paul Merlin. All Rights Reserved.
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

import java.util.List;
import java.util.Map;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.query.grammar.OrderBy;
import org.qi4j.functional.Specification;
import org.qi4j.index.sql.support.skeletons.AbstractSQLQuerying;
import org.sql.generation.api.grammar.builders.query.QuerySpecificationBuilder;
import org.sql.generation.api.grammar.query.QueryExpression;
import org.sql.generation.api.vendor.SQLVendor;

public class PostgreSQLQuerying
        extends AbstractSQLQuerying
{

    @Override
    protected QueryExpression finalizeQuery(
            SQLVendor sqlVendor, QuerySpecificationBuilder specBuilder,
            Class<?> resultType,
            Specification<Composite> whereClause,
            OrderBy[] orderBySegments,
            Integer firstResult,
            Integer maxResults,
            Map<String, Object> variables,
            List<Object> values,
            List<Integer> valueSQLTypes,
            Boolean countOnly )
    {
        Boolean needOffset = firstResult != null && firstResult > 0;
        Boolean needLimit = maxResults != null && maxResults > 0;

        if ( needOffset ) {
            specBuilder.offset( firstResult );
        }
        if ( needLimit ) {
            specBuilder.limit( maxResults );
        }

        return sqlVendor.getQueryFactory().createQuery( specBuilder.createExpression() );
    }

}
