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

import org.qi4j.api.composite.Composite;
import org.qi4j.api.property.Property;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryExecutionException;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.query.grammar.OrderBy;
import org.qi4j.functional.Iterables;
import org.qi4j.functional.Specification;
import org.qi4j.spi.query.QuerySource;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Default implementation of {@link org.qi4j.api.query.Query}
 */
class QueryImpl<T>
    implements Query<T>
{
    private static final long serialVersionUID = 1L;

    /**
     * Type of queried entities.
     */
    protected final Class<T> resultType;
    /**
     * Where clause.
     */
    protected final Specification<Composite> whereClause;
    private QuerySource querySource;
    /**
     * Order by clause segments.
     */
    protected Iterable<OrderBy> orderBySegments;
    /**
     * First result to be returned.
     */
    protected Integer firstResult;
    /**
     * Maximum number of results to be returned.
     */
    protected Integer maxResults;
    /**
     * Mapping between variable name and variable values.
     */
    protected Map<String, Object> variables;

    /**
     * Constructor.
     *
     * @param resultType  type of queried entities; cannot be null
     * @param whereClause where clause
     */
    QueryImpl( final Class<T> resultType,
               final Specification<Composite> whereClause,
               final QuerySource querySource
    )
    {
        this.resultType = resultType;
        this.whereClause = whereClause;
        this.querySource = querySource;
    }

    /**
     * @see org.qi4j.api.query.Query#orderBy(org.qi4j.api.query.grammar.OrderBy[])
     */
    public Query<T> orderBy( final OrderBy... segments )
    {
        orderBySegments = Iterables.iterable(segments);
        return this;
    }

    @Override
    public Query<T> orderBy( Property<?> property, OrderBy.Order order )
    {
        if (orderBySegments == null)
            orderBySegments = Iterables.iterable( new OrderBy( QueryExpressions.property( property ), order ) );
        else
            orderBySegments = Iterables.append( new OrderBy( QueryExpressions.property( property ), order ), orderBySegments );
        return this;
    }

    @Override
    public Query<T> orderBy( Property<?> property)
    {
        orderBy( property, OrderBy.Order.ASCENDING );
        return this;
    }

    /**
     * @see org.qi4j.api.query.Query#firstResult(int)
     */
    public Query<T> firstResult( int firstResult )
    {
        this.firstResult = firstResult;
        return this;
    }

    /**
     * @see org.qi4j.api.query.Query#maxResults(int)
     */
    public Query<T> maxResults( int maxResults )
    {
        this.maxResults = maxResults;
        return this;
    }

    /**
     * @see org.qi4j.api.query.Query#setVariable(String, Object)
     */
    @SuppressWarnings( "unchecked" )
    public Query<T> setVariable( final String name, final Object value )
    {
        if (variables == null)
            variables = new HashMap<String, Object>(  );
        variables.put( name, value );

        return this;
    }

    /**
     * @see org.qi4j.api.query.Query#getVariable(String)
     */
    @SuppressWarnings( "unchecked" )
    public <V> V getVariable( final String name )
    {
        if (variables == null)
            return null;
        else
            return (V) variables.get( name );
    }

    public Class<T> resultType()
    {
        return resultType;
    }

    @Override
    public T find() throws QueryExecutionException
    {
        return querySource.find(resultType, whereClause, orderBySegments, firstResult, maxResults, variables);
    }

    @Override
    public long count() throws QueryExecutionException
    {
        return querySource.count(resultType,  whereClause, orderBySegments, firstResult, maxResults, variables);
    }

    @Override
    public Iterator<T> iterator()
    {
        return querySource.iterator(resultType, whereClause, orderBySegments, firstResult, maxResults, variables);
    }
}