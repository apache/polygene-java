/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
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

/**
 * TODO
 */
public interface ServiceInstanceFactory
{
    /**
     * Creates a new instance of service given the service descriptor.
     *
     * @param serviceDescriptor The service descriptor.
     * @return The new service instance.
     * @throws ServiceInstanceFactoryException
     *          Thrown if creational failed.
     */
    Object newInstance( ServiceDescriptor serviceDescriptor )
        throws ServiceInstanceFactoryException;

    /**
     * Called for each client releasing the instance from use.
     *
     * @param instance The instance to release.
     * @throws ServiceInstanceFactoryException
     *          Thrown if release service failed.
     */
    void releaseInstance( Object instance )
        throws ServiceInstanceFactoryException;
}
