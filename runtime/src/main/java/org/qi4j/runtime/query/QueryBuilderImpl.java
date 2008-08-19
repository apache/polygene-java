/*
 * Copyright 2007 Niclas Hedhman.
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

import org.qi4j.query.Query;
import org.qi4j.query.QueryBuilder;
import org.qi4j.query.QueryExpressions;
import org.qi4j.query.grammar.BooleanExpression;
import org.qi4j.runtime.entity.UnitOfWorkInstance;
import org.qi4j.spi.query.EntityFinder;

/**
 * Default implementation of {@link QueryBuilder}
 *
 * @author Alin Dreghiciu
 * @since March 25, 2008
 */
final class QueryBuilderImpl<T>
    implements QueryBuilder<T>
{

    /**
     * Parent unit of work.
     */
    private final UnitOfWorkInstance unitOfWorkInstance;
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
     * @param unitOfWorkInstance parent unit of work; cannot be null
     * @param entityFinder       entity finder to be used to locate entities; canot be null
     * @param resultType         type of queried entities; cannot be null
     */
    public QueryBuilderImpl( final UnitOfWorkInstance unitOfWorkInstance,
                             final EntityFinder entityFinder,
                             final Class<T> resultType )
    {
        this.unitOfWorkInstance = unitOfWorkInstance;
        this.entityFinder = entityFinder;
        this.resultType = resultType;
        this.whereClause = null;
    }

    /**
     * @see QueryBuilder#where(BooleanExpression)
     */
    public QueryBuilder<T> where( final BooleanExpression whereClause )
    {
        if( whereClause == null )
        {
            throw new IllegalArgumentException( "Where clause cannot be null" );
        }
        if( this.whereClause == null )
        {
            this.whereClause = whereClause;
        }
        else
        {
            this.whereClause = QueryExpressions.and( this.whereClause, whereClause );
        }
        return this;
    }

    /**
     * @see QueryBuilder#newQuery()
     */
    public Query<T> newQuery()
    {
        return new QueryImpl<T>( unitOfWorkInstance, entityFinder, resultType, whereClause );
    }

}