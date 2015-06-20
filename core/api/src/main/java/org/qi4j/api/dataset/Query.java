/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.qi4j.api.dataset;

import org.qi4j.api.property.Property;
import org.qi4j.api.query.QueryException;
import org.qi4j.api.query.QueryExecutionException;
import org.qi4j.functional.Specification;
import org.qi4j.functional.Visitor;

/**
 * TODO
 */
public interface Query<T>
{
    public enum Order
    {
        ASCENDING, DESCENDING
    }

    Query filter( Specification<T> filter );

    Query orderBy( final Property<?> property, final Order order );

    Query skip( int skipNrOfResults );

    Query limit( int maxNrOfResults );

    // Variables
    Query<T> setVariable( String name, Object value );

    Object getVariable( String name );

    long count()
        throws QueryExecutionException;

    T first()
        throws QueryExecutionException;

    T single()
        throws QueryException;

    <ThrowableType extends Throwable> boolean execute( Visitor<T, ThrowableType> resultVisitor )
        throws ThrowableType, QueryExecutionException;

    Iterable<T> toIterable()
        throws QueryExecutionException;
}
