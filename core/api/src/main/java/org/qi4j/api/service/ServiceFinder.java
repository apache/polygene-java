/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.qi4j.api.service;

import java.lang.reflect.Type;

/**
 * Interface used to query for ServiceReferences. Each ServiceFinder is
 * obtained from a specific Module, and the lookup rules are the following:
 * <ol>
 * <li>First look in the same Module as the ServiceFinder</li>
 * <li>Then look in the same Layer as the ServiceFinder. Any Services declared
 * with Visibility Layer and Application should be included</li>
 * <li>Then look in the used Layers. Any Services declared with Visibility Application
 * should be included</li>
 * </ol>
 *
 * Both native Qi4j services and imported services are considered, with preference to native services.
 */
public interface ServiceFinder
{
    /**
     * Find a ServiceReference that implements the given type.
     *
     * @param serviceType the type that the Service must implement
     *
     * @return a ServiceReference if one is found
     *
     * @throws NoSuchServiceException if no service of serviceType is found
     */
    <T> ServiceReference<T> findService( Class<T> serviceType )
        throws NoSuchServiceException;

    /**
     * Find a ServiceReference that implements the given type.
     *
     * @param serviceType the type that the Service must implement
     *
     * @return a ServiceReference if one is found
     *
     * @throws NoSuchServiceException if no service of serviceType is found
     */
    <T> ServiceReference<T> findService( Type serviceType )
        throws NoSuchServiceException;

    /**
     * Find ServiceReferences that implements the given type.
     * <p/>
     * The order of the references is such that Services more local to the querying
     * Module is earlier in the list.
     *
     * @param serviceType the type that the Services must implement
     *
     * @return an iterable of ServiceReferences for the given type. It is empty if none exist
     */
    <T> Iterable<ServiceReference<T>> findServices( Class<T> serviceType );

    /**
     * Find ServiceReferences that implements the given type.
     * <p/>
     * The order of the references is such that Services more local to the querying
     * Module is earlier in the list.
     *
     * @param serviceType the type that the Services must implement
     *
     * @return an iterable of ServiceReferences for the given type. It is empty if none exist
     */
    <T> Iterable<ServiceReference<T>> findServices( Type serviceType );
}
