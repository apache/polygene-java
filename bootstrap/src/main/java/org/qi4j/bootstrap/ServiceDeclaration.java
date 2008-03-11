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

package org.qi4j.bootstrap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.qi4j.spi.service.ServiceInstanceProvider;
import org.qi4j.spi.service.provider.DefaultServiceInstanceProvider;
import org.qi4j.spi.structure.ServiceDescriptor;
import org.qi4j.spi.structure.Visibility;

/**
 * TODO
 */
public final class ServiceDeclaration
{
    private Class<? extends ServiceInstanceProvider> serviceProvider = DefaultServiceInstanceProvider.class;
    private Iterable<Class> serviceTypes;
    private String identity;
    private boolean activateOnStartup = false;
    private Map<Class, Serializable> serviceInfos = new HashMap<Class, Serializable>();
    private Visibility visibility = Visibility.module;

    public ServiceDeclaration( Iterable<Class> serviceTypes )
    {
        this.serviceTypes = serviceTypes;
    }

    public ServiceDeclaration visibleIn( Visibility visibility )
    {
        this.visibility = visibility;
        return this;
    }

    public ServiceDeclaration providedBy( Class<? extends ServiceInstanceProvider> sip )
    {
        serviceProvider = sip;

        return this;
    }

    public ServiceDeclaration identifiedBy( String identity )
    {
        this.identity = identity;
        return this;
    }

    public ServiceDeclaration activateOnStartup()
    {
        activateOnStartup = true;
        return this;
    }

    public <K extends Serializable> ServiceDeclaration setServiceInfo( Class<K> infoType, K serviceInfo )
    {
        serviceInfos.put( infoType, serviceInfo );
        return this;
    }

    List<ServiceDescriptor> getServiceDescriptors()
    {
        List<ServiceDescriptor> serviceDescriptors = new ArrayList<ServiceDescriptor>();
        for( Class serviceType : serviceTypes )
        {
            String id = identity;
            if( id == null )
            {
                id = serviceType.getName();
            }
            ServiceDescriptor serviceDescriptor = new ServiceDescriptor( serviceType, serviceProvider, identity, visibility, activateOnStartup, serviceInfos );
            serviceDescriptors.add( serviceDescriptor );
        }
        return serviceDescriptors;
    }
}
