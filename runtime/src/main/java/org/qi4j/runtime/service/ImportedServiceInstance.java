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

package org.qi4j.runtime.service;

import org.qi4j.api.service.ImportedServiceDescriptor;
import org.qi4j.api.service.ServiceImporter;

/**
 * JAVADOC
 */
public final class ImportedServiceInstance<T>
{
    private final T instance;
    private final ServiceImporter factory;
    private final ImportedServiceDescriptor serviceDescriptor;


    public ImportedServiceInstance( T instance,
                            ServiceImporter factory,
                            ImportedServiceDescriptor serviceDescriptor )
    {
        this.factory = factory;
        this.serviceDescriptor = serviceDescriptor;
        this.instance = instance;
    }

    public T instance()
    {
        return instance;
    }

    public ImportedServiceDescriptor importedServiceDescriptor()
    {
        return serviceDescriptor;
    }
}