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

import java.util.HashMap;
import java.util.Map;
import org.qi4j.composite.Composite;
import org.qi4j.spi.service.ServiceInstance;
import org.qi4j.spi.service.ServiceProviderException;

/**
 * TODO
 */
public final class ServiceMap<T>
{
    private ModuleInstance moduleInstance;
    private Class<T> serviceClass;
    private Map<Class<? extends Composite>, ServiceInstance> instances = new HashMap<Class<? extends Composite>, ServiceInstance>();

    public ServiceMap( ModuleInstance moduleInstance, Class<T> serviceClass )
    {
        this.moduleInstance = moduleInstance;
        this.serviceClass = serviceClass;
    }

    public T getService( Class<? extends Composite> compositeType )
        throws ServiceProviderException
    {
        ServiceInstance instance = instances.get( compositeType );
        if( instance == null )
        {
            ModuleInstance realModule = moduleInstance.getModuleForComposite( compositeType );
            instance = realModule.getServiceRegistry().getServiceProvider( serviceClass ).getInstance();
            instances.put( compositeType, instance );
        }
        return (T) instance.getInstance();
    }

    public void release()
    {
        for( ServiceInstance serviceInstance : instances.values() )
        {
            try
            {
                serviceInstance.release();
            }
            catch( Exception e )
            {
                // TODO: I think we should accumulate Exceptions and aggregate into a single Exception
                //       thrown out of this method.
                e.printStackTrace();
            }
        }
    }
}
