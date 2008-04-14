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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.qi4j.query.Query;
import org.qi4j.query.grammar.BooleanExpression;
import org.qi4j.query.grammar.OrderBy;
import org.qi4j.query.grammar.SingleValueExpression;
import org.qi4j.runtime.entity.UnitOfWorkInstance;
import org.qi4j.spi.query.EntitySearcher;
import org.qi4j.spi.query.SearchException;
import org.qi4j.spi.serialization.EntityId;

/**
 * Default implementation of {@link Query}
 *
 * @author Alin Dreghiciu
 * @since 0.2.0, April 14, 2008
 */
final class QueryImpl<T>
    implements Query<T>
{

    /**
     * Parent unit of work.
     */
    private final UnitOfWorkInstance unitOfWorkInstance;
    /**
     * Entity searcher to be used to locate entities.
     */
    private final EntitySearcher entitySearcher;
    /**
     * Type of queried entities.
     */
    private final Class<T> resultType;
    /**
     * Where clause.
     */
    private final BooleanExpression whereClause;
    /**
     * Order by clause segments.
     */
    private OrderBy[] orderBySegments;
    /**
     * First result to be returned.
     */
    private Integer firstResult;
    /**
     * Maximum number of results to be returned.
     */
    private Integer maxResults;
    /**
     * Mapping between variable name and variable value expression.
     */
    private final Map<String, SingleValueExpression> variables;

    /**
     * Constructor.
     *
     * @param unitOfWorkInstance parent unit of work; cannot be null
     * @param entitySearcher     entity searcher to be used to locate entities; cannot be null
     * @param resultType         type of queried entities; cannot be null
     * @param whereClause        where clause
     */
    QueryImpl( final UnitOfWorkInstance unitOfWorkInstance,
               final EntitySearcher entitySearcher,
               final Class<T> resultType,
               final BooleanExpression whereClause )
    {
        this.unitOfWorkInstance = unitOfWorkInstance;
        this.entitySearcher = entitySearcher;
        this.resultType = resultType;
        this.whereClause = whereClause;
        this.variables = new HashMap<String, SingleValueExpression>();
        // TODO initialize variables map by finding them into the whereClause clause
    }

    /**
     * @see Query#orderBy(OrderBy[])
     */
    public Query<T> orderBy( final OrderBy... segments )
    {
        orderBySegments = segments;
        return this;
    }

    /**
     * @see Query#firstResult(int)
     */
    public Query<T> firstResult( int firstResult )
    {
        this.firstResult = firstResult;
        return this;
    }

    /**
     * @see Query#maxResults(int)
     */
    public Query<T> maxResults( int maxResults )
    {
        this.maxResults = maxResults;
        return this;
    }

    /**
     * @see Query#find()
     */
    public T find()
    {
        final Iterator<T> results = iterator();
        if( results.hasNext() )
        {
            return results.next();
        }
        return null;
    }

    /**
     * @see Query#setVariable(String, Object)
     */
    public void setVariable( final String name,
                             final Object value )
    {
        // TODO implement variable set
        throw new UnsupportedOperationException();
    }

    /**
     * @see Query#getVariable(String)
     */
    @SuppressWarnings( "unchecked" )
    public <V> V getVariable( final String name )
    {
        return (V) variables.get( name );
    }

    /**
     * @see Query#iterator()
     */
    public Iterator<T> iterator()
    {
        final List<T> entities = new ArrayList<T>();
        try
        {
            final Iterable<EntityId> foundEntities = entitySearcher.find(
                resultType, whereClause, orderBySegments, firstResult, maxResults
            );
            if( foundEntities != null )
            {
                for( final EntityId foundEntity : foundEntities )
                {
                    final Class<T> entityType = unitOfWorkInstance.getModuleInstance().getModuleContext().getModuleBinding().lookupClass( foundEntity.getCompositeType() );
                    // TODO shall we throw an exception if class cannot be found?
                    final T entity = unitOfWorkInstance.getReference( foundEntity.getIdentity(), entityType );
                    entities.add( entity );
                }
            }
        }
        catch( SearchException e )
        {
            // TODO what shall we do? return null / throw runtime exception?
            e.printStackTrace();
        }
        return entities.iterator();
    }

    @Override public String toString()
    {
        return whereClause.toString();
    }
}