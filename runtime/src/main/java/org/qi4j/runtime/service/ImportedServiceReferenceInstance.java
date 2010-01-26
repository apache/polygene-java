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

package org.qi4j.runtime.service;

import org.qi4j.api.service.ServiceImporterException;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.structure.Module;

/**
 * Implementation of ServiceReference. This manages the reference to the imported service
 * <p/>
 * Whenever the service is requested it is returned directly to the client. That means that
 * to handle service passivation and unavailability correctly, any proxying must be done in the
 * service importer.
 */
public final class ImportedServiceReferenceInstance<T>
    implements ServiceReference<T>
{
    private volatile ImportedServiceInstance<T> serviceInstance;
    private T instance;
    private final Module module;
    private final ImportedServiceModel serviceModel;

    public ImportedServiceReferenceInstance( ImportedServiceModel serviceModel, Module module )
    {
        this.module = module;
        this.serviceModel = serviceModel;
    }

    public String identity()
    {
        return serviceModel.identity();
    }

    public <T> T metaInfo( Class<T> infoType )
    {
        return serviceModel.metaInfo( infoType );
    }

    public synchronized T get()
    {
        return getInstance();
    }

    public boolean isActive()
    {
        return serviceInstance != null && serviceInstance.isActive();
    }

    public Module module()
    {
        return module;
    }

    private T getInstance()
        throws ServiceImporterException
    {
        // DCL that works with Java 1.5 volatile semantics
        if( serviceInstance == null )
        {
            synchronized( this )
            {
                if( serviceInstance == null )
                {
                    serviceInstance = (ImportedServiceInstance<T>) serviceModel.<T>importInstance( module );
                    instance = serviceInstance.instance();
                }
            }
        }

        return instance;
    }

    @Override
    public String toString()
    {
        return serviceModel.identity() + ", active=" + isActive() + ", module='" + serviceModel.moduleName() + "'";
    }
}