/*
 * Copyright 2007 Niclas Hedhman.
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
 */
package org.qi4j.api.query;

import org.qi4j.api.unitofwork.UnitOfWork;

/**
 * This is used to create QueryBuilders.
 *
 * @see QueryBuilder
 */
public interface QueryBuilderFactory
{
    /**
     * Create a new QueryBuilder.
     *
     * @param resultType the type of the result that you want
     *
     * @return a QueryBuilder
     *
     * @throws MissingIndexingSystemException if there is no EntityFinder service available
     */
    <T> QueryBuilder<T> newQueryBuilder( Class<T> resultType )
        throws MissingIndexingSystemException;

    <T> Query<T> newNamedQuery( Class<T> resultType, UnitOfWork unitOfWork, String name );
}