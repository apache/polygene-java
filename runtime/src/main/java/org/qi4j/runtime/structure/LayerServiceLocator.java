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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.qi4j.service.ServiceFinder;
import org.qi4j.service.ServiceReference;

/**
 * ServiceLocator implementation for a LayerInstance.
 * This implements rules 2 and 3 in the ServiceLocator description
 *
 * @see org.qi4j.service.ServiceFinder
 */
public class LayerServiceLocator
    implements ServiceFinder
{
    private Map<Class, List<ModuleInstance>> modulesForPublicServices;

    public LayerServiceLocator( Map<Class, List<ModuleInstance>> modulesForPublicServices )
    {
        this.modulesForPublicServices = modulesForPublicServices;
    }

    public <T> ServiceReference<T> findService( Class<T> serviceType )
    {
        // Look in the used Layers
        List<ModuleInstance> modules = modulesForPublicServices.get( serviceType );
        if( modules != null )
        {
            return modules.get( 0 ).findService( serviceType );
        }

        return null;
    }

    public <T> Iterable<ServiceReference<T>> findServices( Class<T> serviceType )
    {
        // Look in the used Layers
        List<ModuleInstance> modules = modulesForPublicServices.get( serviceType );
        if( modules != null )
        {
            List<ServiceReference<T>> serviceReferences = new ArrayList<ServiceReference<T>>();
            for( ModuleInstance module : modules )
            {
                Iterable<ServiceReference<T>> moduleServices = module.findServices( serviceType );
                for( ServiceReference<T> moduleService : moduleServices )
                {
                    serviceReferences.add( moduleService );
                }
            }
            return serviceReferences;
        }
        return Collections.emptyList();
    }
}
