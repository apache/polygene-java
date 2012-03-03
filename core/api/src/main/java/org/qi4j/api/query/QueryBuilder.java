/*
 * Copyright 2007 Rickard Öberg.
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

import org.qi4j.api.composite.Composite;
import org.qi4j.functional.Specification;

/**
 * QueryBuilders are used to create {@link Query} instances.
 * Iteratively add where() clauses to the query, and then use
 * {@link org.qi4j.api.unitofwork.UnitOfWork#newQuery(QueryBuilder)}  to instantiate the Query.
 * QueryBuilders are immutable, so when adding new where-clauses you get new instances. This
 *
 * DDD tip: Query objects are not executed immediately, so they
 * should be constructed in the domain model and handed over to
 * the UI, which can then further constrain it before actual
 * execution.
 */
public interface QueryBuilder<T>
{
    /**
     * Add a where-clause to the Query. Use {@link QueryExpressions}
     * to create the expression.
     *
     * @param specification the where clause
     *
     * @return a new builder with the added where-clause
     */
    QueryBuilder<T> where( Specification<Composite> specification );

    /**
     * Create a new query with the declared where-clauses that will be evaluated against the iterable entries.
     *
     * @param iterable collection of objects (composites?)
     *
     * @return a new Query instance
     */
    Query<T> newQuery( Iterable<T> iterable );
}