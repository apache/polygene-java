/*  Copyright 2008 Edward Yakop.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
* implied.
*
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.qi4j.library.spring.bootstrap.internal.service;

import org.qi4j.api.object.ObjectDescriptor;
import org.qi4j.api.service.ImportedServiceDescriptor;
import org.qi4j.api.service.ServiceDescriptor;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.ApplicationDescriptor;
import org.qi4j.api.structure.LayerDescriptor;
import org.qi4j.api.structure.Module;
import org.qi4j.api.structure.ModuleDescriptor;
import org.qi4j.functional.HierarchicalVisitor;

final class ServiceLocator
    implements HierarchicalVisitor<Object, Object, RuntimeException>
{
    private final String serviceId;
    private Class serviceType;
    private String moduleName;
    private String layerName;

    private String tempLayerName;
    private String tempModuleName;

    ServiceLocator( String serviceId )
    {
        this.serviceId = serviceId;
    }

    @Override
    public boolean visitEnter( Object visited )
        throws RuntimeException
    {
        if( visited instanceof ApplicationDescriptor )
        {
            return true;
        }
        else if( visited instanceof LayerDescriptor )
        {
            tempLayerName = ( (LayerDescriptor) visited ).name();
            return true;
        }
        else if( visited instanceof ModuleDescriptor )
        {
            tempModuleName = ( (ModuleDescriptor) visited ).name();
            return true;
        }
        else if( visited instanceof ServiceDescriptor )
        {
            ServiceDescriptor aDescriptor = (ServiceDescriptor) visited;
            String identity = aDescriptor.identity();
            if( serviceId.equals( identity ) )
            {
                layerName = tempLayerName;
                moduleName = tempModuleName;
                serviceType = aDescriptor.type();
            }
        }
        else if( visited instanceof ObjectDescriptor )
        {
            return false;
        }

        return true;
    }

    @Override
    public boolean visitLeave( Object visited )
        throws RuntimeException
    {
        return true;
    }

    @Override
    public boolean visit( Object visited )
        throws RuntimeException
    {
        if( visited instanceof ImportedServiceDescriptor )
        {
            ImportedServiceDescriptor aDescriptor = (ImportedServiceDescriptor) visited;
            String identity = aDescriptor.identity();
            if( serviceId.equals( identity ) )
            {
                layerName = tempLayerName;
                moduleName = tempModuleName;
                serviceType = aDescriptor.type();
            }
        }

        return true;
    }

    @SuppressWarnings( "unchecked" )
    ServiceReference locateService( Application anApplication )
    {
        if( layerName != null )
        {
            Module module = anApplication.findModule( layerName, moduleName );
            Iterable<ServiceReference<Object>> serviceRefs = module.findServices( serviceType );
            for( ServiceReference<Object> serviceRef : serviceRefs )
            {
                if( serviceId.equals( serviceRef.identity() ) )
                {
                    return serviceRef;
                }
            }
        }

        return null;
    }
}
