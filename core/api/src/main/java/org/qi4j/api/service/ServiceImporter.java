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
 * Import a service from some external source.
 */
public interface ServiceImporter<T>
{
    /**
     * Imports an instance of the service type described in the service descriptor.
     *
     * @param serviceDescriptor The service descriptor.
     *
     * @return The imported service instance.
     *
     * @throws ServiceImporterException if import failed.
     */
    T importService( ImportedServiceDescriptor serviceDescriptor )
        throws ServiceImporterException;

    /**
     * Ask if the service is available or not.
     *
     * @param instance the instance to be checked
     *
     * @return true if the service is available, false if not
     */
    boolean isAvailable( T instance );
}
