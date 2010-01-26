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
 * 1) First look in the same Module as the ServiceLocator
 * 2) Then look in the same Layer as the ServiceLocator. Any Services declared
 * with Visibility Layer and Application should be included
 * 3) Then look in the extended Layers. Any Services declared with Visibility Application
 * should be included
 */
public interface ServiceFinder
{
    /**
     * Find a ServiceReference that implements the given type.
     *
     * @param serviceType the type that the Service must implement
     *
     * @return a ServiceReference if one is found, or null if none exists
     */
    <T> ServiceReference<T> findService( Type serviceType );

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
