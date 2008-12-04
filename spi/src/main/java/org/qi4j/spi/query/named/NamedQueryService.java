/*
 * Copyright 2008 Niclas Hedhman.
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
package org.qi4j.spi.query.named;

import org.qi4j.entity.UnitOfWork;
import org.qi4j.query.Query;

/**
 * NamedQueryService is a provider for named, native and other special query types.
 *
 * Named queries can also be used to centralize the query management, and avoid having
 * query code spread out in the domain model, even if the implementation of the NamedQueryService
 * is using the Qi4j Query API.
 */
public interface NamedQueryService
{
    /**
     * Returns a Query with the given name and result type.
     *
     * @param name The name of the query. To avoid naming conflicts, FQDNs should be used.
     * @param resultType the Query result type.
     * @param unitOfWork The UnitOfWork that the Query shall belong to.
     * @return a Query instance.
     */
    <T> Query<T> newQuery( String name, Class<T> resultType, UnitOfWork unitOfWork );
}
