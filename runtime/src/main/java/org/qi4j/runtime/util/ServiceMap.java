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

package org.qi4j.runtime.util;

import java.util.HashMap;
import java.util.Map;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.runtime.structure.ModuleInstance;
import org.qi4j.api.service.ServiceReference;

/**
 * This class helps you manage references to services which are acquired
 * from the perspective of given Composite types. Once you are done
 * with the Map, make sure to call {@link #release()} on it to release
 * all the services that have been used.
 */
public final class ServiceMap<T>
{
    private final ModuleInstance moduleInstance;
    private final Class<T> serviceClass;
    private final Map<Class<? extends Composite>, ServiceReference> instances = new HashMap<Class<? extends Composite>, ServiceReference>();

    public ServiceMap( ModuleInstance moduleInstance, Class<T> serviceClass )
    {
        this.moduleInstance = moduleInstance;
        this.serviceClass = serviceClass;
    }

    public T getService( Class<? extends Composite> compositeType )
    {
        ServiceReference serviceReference = instances.get( compositeType );
        if( serviceReference == null )
        {
            ModuleInstance realModule = getRealModule( compositeType );
            if( realModule == null )
            {
                return null; // todo is that ok?
            }
            serviceReference = realModule.findService( serviceClass );
            if( serviceReference == null )
            {
                return null;
            }
            instances.put( compositeType, serviceReference );
        }
        return (T) serviceReference.get();
    }

    private ModuleInstance getRealModule( Class<? extends Composite> compositeType )
    {
        if( EntityComposite.class.isAssignableFrom( compositeType ) )
        {
            return moduleInstance.findModuleForEntity( compositeType );
        }
        return moduleInstance.findModuleForComposite( compositeType );
    }

    public void release()
    {
        for( ServiceReference serviceReference : instances.values() )
        {
            serviceReference.releaseService();
        }
    }
}
