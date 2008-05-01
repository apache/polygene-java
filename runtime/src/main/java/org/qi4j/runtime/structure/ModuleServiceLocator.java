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

package org.qi4j.runtime.structure;

import java.util.ArrayList;
import java.util.List;
import org.qi4j.service.ServiceLocator;
import org.qi4j.service.ServiceReference;

/**
 * ServiceProviderRepository implementation for a ModuleInstance.
 * This implements the following lookup rule:
 * 1) Check the local Module for a provider for the given type
 * 2) Check the same Layer for a provider for the given type
 */
public class ModuleServiceLocator
    implements ServiceLocator
{
    private ModuleInstance moduleInstance;
    private ServiceLocator layerServiceLocator;

    public ModuleServiceLocator( ModuleInstance moduleInstance, ServiceLocator layerServiceLocator )
    {
        this.moduleInstance = moduleInstance;
        this.layerServiceLocator = layerServiceLocator;
    }

    public <T> ServiceReference<T> lookupService( Class<T> serviceType )
    {
        // Check for services in the own module
        ServiceReference<T> service = moduleInstance.lookupService( serviceType );
        if( service != null )
        {
            return service;
        }

        // Check for service in the layer and used layers
        service = layerServiceLocator.lookupService( serviceType );

        return service;
    }

    public <T> Iterable<ServiceReference<T>> lookupServices( Class<T> serviceType )
    {
        List<ServiceReference<T>> serviceList = new ArrayList<ServiceReference<T>>();

        {
            // Add services from the own module
            Iterable<ServiceReference<T>> services = moduleInstance.lookupServices( serviceType );
            for( ServiceReference<T> service : services )
            {
                serviceList.add( service );
            }
        }

        // Add service from the layer and used layers
        Iterable<ServiceReference<T>> services = layerServiceLocator.lookupServices( serviceType );
        for( ServiceReference<T> service : services )
        {
            serviceList.add( service );
        }
        return serviceList;
    }
}
