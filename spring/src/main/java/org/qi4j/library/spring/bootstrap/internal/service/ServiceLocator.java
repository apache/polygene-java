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

import org.qi4j.api.service.ServiceFinder;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.Module;
import org.qi4j.spi.service.ServiceDescriptor;
import org.qi4j.spi.structure.ApplicationDescriptor;
import org.qi4j.spi.structure.DescriptorVisitor;
import org.qi4j.spi.structure.LayerDescriptor;
import org.qi4j.spi.structure.ModuleDescriptor;

final class ServiceLocator extends DescriptorVisitor
{
    private final String serviceId;
    private String moduleName;
    private String layerName;
    private ServiceDescriptor serviceDescriptor;

    private String tempLayerName;
    private String tempModuleName;

    ServiceLocator( String serviceId )
    {
        this.serviceId = serviceId;
    }

    public void visit( ApplicationDescriptor aDescriptor )
    {
        moduleName = null;
        layerName = null;
        serviceDescriptor = null;
    }

    @Override
    public final void visit( LayerDescriptor aDescriptor )
    {
        tempLayerName = aDescriptor.name();
    }

    @Override
    public final void visit( ModuleDescriptor aDescriptor )
    {
        tempModuleName = aDescriptor.name();
    }

    @Override
    public final void visit( ServiceDescriptor aDescriptor )
    {
        String identity = aDescriptor.identity();
        if( serviceId.equals( identity ) )
        {
            layerName = tempLayerName;
            moduleName = tempModuleName;

            serviceDescriptor = aDescriptor;
        }
    }

    @SuppressWarnings( "unchecked" ) ServiceReference locateService( Application anApplication )
    {
        if( layerName != null )
        {
            Module module = anApplication.findModule( layerName, moduleName );
            ServiceFinder serviceFinder = module.serviceFinder();
            Class type = serviceDescriptor.type();
            Iterable<ServiceReference<Object>> serviceRefs = serviceFinder.<Object>findServices( type );
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

    ServiceDescriptor serviceDescriptor()
    {
        return serviceDescriptor;
    }
}
