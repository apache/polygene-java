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

import org.qi4j.spi.service.ServiceInstanceProvider;
import org.qi4j.spi.service.ServiceRegistry;

/**
 * ServiceRegistry implementation for a ModuleInstance.
 * This implements the following lookup rule:
 * 1) Check the local Module for a provider for the given type
 * 2) Check the same Layer for a provider for the given type
 */
public final class ModuleServiceRegistry
    implements ServiceRegistry
{
    private ModuleInstance moduleInstance;
    private ServiceRegistry layerRegistry;

    public ModuleServiceRegistry( ModuleInstance moduleInstance, ServiceRegistry layerRegistry )
    {
        this.moduleInstance = moduleInstance;
        this.layerRegistry = layerRegistry;
    }

    public ServiceInstanceProvider getServiceProvider( Class type )
    {
        ServiceInstanceProvider provider = moduleInstance.getServiceProvider( type );
        if( provider != null )
        {
            return provider;
        }

        return layerRegistry.getServiceProvider( type );
    }
}
