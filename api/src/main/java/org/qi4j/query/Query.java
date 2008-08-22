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
package org.qi4j.query;

import java.io.Serializable;
import org.qi4j.query.grammar.OrderBy;

/**
 *  This represents a Query in an indexing system. It is created from a
 * {@link QueryBuilder}, which decides the "where" clause in the query.
 * Additional limitations, such as paging, ordering, and variables, can be set on
 * a Query before it is executed by calling one of find(), iterator(),
 * or count().
 * <p/>
 * DDD tip: typically Queries are created in the Domain Model and passed to the UI,
 * which sets the order and paging before executing it.
 */
public interface Query<T>
    extends Iterable<T>, Serializable
{
    Query<T> orderBy( OrderBy... segments );

    Query<T> firstResult( int firstResult );

    Query<T> maxResults( int maxResults );

    T find()
        throws QueryExecutionException;

    void setVariable( String name, Object value );

    <V> V getVariable( String name );

    Class<T> resultType();

    long count()
        throws QueryExecutionException;
}