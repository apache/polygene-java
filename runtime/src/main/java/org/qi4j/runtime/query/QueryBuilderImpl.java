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

import org.qi4j.api.query.MissingIndexingSystemException;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.query.grammar.BooleanExpression;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.spi.query.EntityFinder;

/**
 * Default implementation of {@link QueryBuilder}
 */
final class QueryBuilderImpl<T>
    implements QueryBuilder<T>
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
    private BooleanExpression whereClause;

    /**
     * Constructor.
     *
     * @param entityFinder entity finder to be used to locate entities; canot be null
     * @param resultType   type of queried entities; cannot be null
     * @param whereClause  current where-clause
     */
    public QueryBuilderImpl( final EntityFinder entityFinder,
                             final Class<T> resultType,
                             final BooleanExpression whereClause
    )
    {
        this.entityFinder = entityFinder;
        this.resultType = resultType;
        this.whereClause = whereClause;
    }

    /**
     * @see QueryBuilder#where(BooleanExpression)
     */
    public QueryBuilder<T> where( BooleanExpression whereClause )
    {
        if( whereClause == null )
        {
            throw new IllegalArgumentException( "Where clause cannot be null" );
        }
        if( this.whereClause != null )
        {
            whereClause = QueryExpressions.and( this.whereClause, whereClause );
        }
        return new QueryBuilderImpl<T>( entityFinder, resultType, whereClause );
    }

    /**
     * @see QueryBuilder#newQuery(org.qi4j.api.unitofwork.UnitOfWork)
     */
    public Query<T> newQuery( UnitOfWork unitOfWork )
    {
        if( unitOfWork == null )
        {
            throw new IllegalArgumentException( "UnitOfWork may not be null" );
        }

        if( entityFinder == null )
        {
            throw new MissingIndexingSystemException();
        }
        return new EntityQuery<T>( unitOfWork, entityFinder, resultType, whereClause );
    }

    /**
     * @see QueryBuilder#newQuery(Iterable)
     */
    public Query<T> newQuery( Iterable<T> iterable )
    {
        return new IterableQuery<T>( iterable, resultType, whereClause );
    }
}