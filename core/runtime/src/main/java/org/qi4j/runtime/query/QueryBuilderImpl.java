/*
 * Copyright 2007-2009 Niclas Hedhman.
 * Copyright 2008 Alin Dreghiciu.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.runtime.query;

import org.qi4j.api.composite.Composite;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.functional.Specification;
import org.qi4j.spi.query.EntityFinder;
import org.qi4j.spi.query.QueryBuilderSPI;
import org.qi4j.spi.query.QuerySource;

/**
 * Default implementation of {@link QueryBuilder}
 */
final class QueryBuilderImpl<T>
    implements QueryBuilder<T>, QueryBuilderSPI<T>
{

    /**
     * Entity finder to be used to locate entities.
     */
    private final EntityFinder entityFinder;

    /**
     * Type of queried entities.
     */
    private final Class<T> resultType;
    /**
     * Where clause.
     */
    private final Specification<Composite> whereClause;

    /**
     * Constructor.
     *
     * @param entityFinder entity finder to be used to locate entities; canot be null
     * @param resultType   type of queried entities; cannot be null
     * @param whereClause  current where-clause
     */
    QueryBuilderImpl( final EntityFinder entityFinder,
                      final Class<T> resultType,
                      final Specification<Composite> whereClause
    )
    {
        this.entityFinder = entityFinder;
        this.resultType = resultType;
        this.whereClause = whereClause;
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public QueryBuilder<T> where( Specification<Composite> specification )
    {
        if( specification == null )
        {
            throw new IllegalArgumentException( "Where clause cannot be null" );
        }
        if( this.whereClause != null )
        {
            specification = QueryExpressions.and( this.whereClause, specification );
        }
        return new QueryBuilderImpl<>( entityFinder, resultType, specification );
    }

    @Override
    public Query<T> newQuery( Iterable<T> iterable )
    {
        return new QueryImpl<>( resultType, whereClause, new IterableQuerySource( iterable ) );
    }

    // SPI
    @Override
    public Query<T> newQuery( QuerySource querySource )
    {
        return new QueryImpl<>( resultType, whereClause, querySource );
    }
}