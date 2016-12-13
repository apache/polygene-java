/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package org.apache.polygene.spi.query;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.apache.polygene.api.common.Optional;
import org.apache.polygene.api.composite.Composite;
import org.apache.polygene.api.entity.EntityReference;
import org.apache.polygene.api.query.grammar.OrderBy;

/**
 * Entity Finder.
 */
public interface EntityFinder
{
    /**
     * Find entities matching the query criterion.
     *
     * @param resultType        Type that the entities must have.
     * @param whereClause       Where clause specification.
     * @param orderBySegments   Ordering
     * @param firstResult       Index of the first returned entity.
     * @param maxResults        Maximum returned entities.
     * @param variables         Query variables
     * @return Entities matching the query criterion.
     * @throws EntityFinderException on error
     */
    Stream<EntityReference> findEntities( Class<?> resultType,
                                          @Optional Predicate<Composite> whereClause,
                                          @Optional List<OrderBy> orderBySegments,
                                          @Optional Integer firstResult,
                                          @Optional Integer maxResults,
                                          Map<String, Object> variables )
        throws EntityFinderException;

    /**
     * Find a single entity matching the query criterion.
     *
     * @param resultType    Type that the entity must have.
     * @param whereClause   Where clause specification.
     * @param variables     Query variables
     * @return Single entity matching the query criterion.
     * @throws EntityFinderException on error
     */
    EntityReference findEntity( Class<?> resultType,
                                @Optional Predicate<Composite> whereClause,
                                Map<String, Object> variables
    )
        throws EntityFinderException;

    /**
     * Count entities matching the query criterion.
     *
     * @param resultType    Type that the entities must have.
     * @param whereClause   Where clause specification.
     * @param variables     Query variables
     * @return Count entities matching the query criterion.
     * @throws EntityFinderException on error
     */
    long countEntities( Class<?> resultType,
                        @Optional Predicate<Composite> whereClause,
                        Map<String, Object> variables
    )
        throws EntityFinderException;
}