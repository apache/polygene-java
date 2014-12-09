/*
 * Copyright 2007 Rickard Öberg.
 * Copyright 2007 Niclas Hedhman.
 * Copyright 2008 Alin Dreghiciu.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.qi4j.api.query;

import java.io.Serializable;

import org.qi4j.api.geometry.TPoint;
import org.qi4j.api.property.Property;
import org.qi4j.api.query.grammar.OrderBy;

/**
 * This represents a Query in an indexing system. It is created from a
 * {@link QueryBuilder}, which decides the "where" clause in the query.
 * <p>
 * Additional limitations, such as paging, ordering, and variables, can be set on
 * a Query before it is executed by calling one of find(), iterator(),
 * or count().
 * </p>
 * <p>
 * DDD tip: typically Queries are created in the Domain Model and passed to the UI,
 * which sets the order and paging before executing it.
 * </p>
 */
public interface Query<T>
    extends Iterable<T>, Serializable
{
    /**
     * Set the ordering rules. If many segments are used for ordering
     * then they will be applied in order.
     *
     * @param segments the segments to order by
     *
     * @return the Query
     */
    Query<T> orderBy( OrderBy... segments );

    /**
     * Append an ordering rule to the existing segments.
     *
     * @param property the property to order by
     * @param order the order to apply
     *
     * @return the Query
     */
    Query<T> orderBy( final Property<?> property, final OrderBy.Order order );


    /**
     * Append an ordering rule to the existing segments.
     *
     * @param property the property to order by
     * @param centre order from a spatial centre
     * @param order the order to apply
     *
     * @return the Query
     */
    Query<T> orderBy( final Property<?> property, TPoint centre, final OrderBy.Order order );

    /**
     * Append an ascending ordering rule to the existing segments.
     *
     * @param property the property to order by
     *
     * @return the Query
     */
    Query<T> orderBy( Property<?> property );

    /**
     * Set the index of the first result. Default is 0 (zero).
     *
     * @param firstResult which index to use as the first one
     *
     * @return the Query
     */
    Query<T> firstResult( int firstResult );

    /**
     * Set how many results should be returned. Default is that
     * there is no limit set.
     *
     * @param maxResults that shouldbe returned
     *
     * @return the query
     */
    Query<T> maxResults( int maxResults );

    /**
     * Get the first Entity that matches the criteria. This
     * executes the Query.
     *
     * @return the first found Entity or null if none were found
     *
     * @throws QueryExecutionException if the query fails
     */
    T find()
        throws QueryExecutionException;

    /**
     * Set the value of a named variable.
     *
     * @param name  of the variable
     * @param value of the variable
     *
     * @return the query
     */
    Query<T> setVariable( String name, Object value );

    /**
     * Get the value of a named variable.
     *
     * @param name of the variable
     *
     * @return value of the variable
     */
    <V> V getVariable( String name );

    /**
     * Get the result type of this Query
     *
     * @return the result type
     */
    Class<T> resultType();

    /**
     * Count how many results would be returned by this Query.
     * This executes the Query.
     *
     * @return result count
     *
     * @throws QueryExecutionException if the query fails
     */
    long count()
        throws QueryExecutionException;
}