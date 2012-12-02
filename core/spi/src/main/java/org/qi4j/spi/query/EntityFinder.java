/*
 * Copyright 2008 Alin Dreghiciu.
 * Copyright 2009 Niclas Hedhman.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *count( INTERFACES_OF.map( A.class ) ), equalTo( 1L )
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.spi.query;

import java.util.Map;
import org.qi4j.api.common.Optional;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.query.grammar.OrderBy;
import org.qi4j.functional.Specification;

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
     * @return Single entity matching the query criterion.
     */
    Iterable<EntityReference> findEntities( Class<?> resultType,
                                            @Optional Specification<Composite> whereClause,
                                            @Optional OrderBy[] orderBySegments,
                                            @Optional Integer firstResult,
                                            @Optional Integer maxResults,
                                            Map<String, Object> variables
    )
        throws EntityFinderException;

    /**
     * Find a single entity matching the query criterion.
     *
     * @param resultType    Type that the entity must have.
     * @param whereClause   Where clause specification.
     * @param variables     Query variables
     * @return Single entity matching the query criterion.
     */
    EntityReference findEntity( Class<?> resultType,
                                @Optional Specification<Composite> whereClause,
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
     */
    long countEntities( Class<?> resultType,
                        @Optional Specification<Composite> whereClause,
                        Map<String, Object> variables
    )
        throws EntityFinderException;
}