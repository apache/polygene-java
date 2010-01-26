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

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceReference;

/**
 * JAVADOC
 */
public class ImportedServicesInstance
    implements Activatable
{
    private final ImportedServicesModel servicesModel;
    private final List<? extends ServiceReference> serviceReferences;
    private final Activator activator;
    private final Map<String, ServiceReference<?>> mapIdentityServiceReference = new HashMap<String, ServiceReference<?>>();

    public ImportedServicesInstance( ImportedServicesModel servicesModel,
                                     List<? extends ServiceReference> serviceReferences
    )
    {
        this.servicesModel = servicesModel;
        this.serviceReferences = serviceReferences;

        for( ServiceReference serviceReference : serviceReferences )
        {
            mapIdentityServiceReference.put( serviceReference.identity(), serviceReference );
        }

        activator = new Activator();
    }

    public void activate()
        throws Exception
    {
        for( ServiceReference serviceReference : serviceReferences )
        {
            if( serviceReference instanceof Activatable )
            {
                activator.activate( (Activatable) serviceReference );
            }
        }
    }

    public void passivate()
        throws Exception
    {
        activator.passivate();
    }

    public ServiceReference<Object> getServiceWithIdentity( String serviceIdentity )
    {
        return (ServiceReference<Object>) mapIdentityServiceReference.get( serviceIdentity );
    }

    public <T> ServiceReference<T> getServiceFor( Type type, Visibility visibility )
    {
        ImportedServiceModel serviceModel = servicesModel.getServiceFor( type, visibility );

        ServiceReference<T> serviceRef = null;
        if( serviceModel != null )
        {
            serviceRef = (ServiceReference<T>) mapIdentityServiceReference.get( serviceModel.identity() );
        }

        return serviceRef;
    }

    public <T> void getServicesFor( Type type, Visibility visibility, List<ServiceReference<T>> serviceReferences )
    {
        List<ImportedServiceModel> serviceModels = new ArrayList<ImportedServiceModel>();
        servicesModel.getServicesFor( type, visibility, serviceModels );
        for( ImportedServiceModel serviceModel : serviceModels )
        {
            serviceReferences.add( (ServiceReference<T>) mapIdentityServiceReference.get( serviceModel.identity() ) );
        }
    }

    @Override
    public String toString()
    {
        String str = "{";
        String sep = "";
        for( ServiceReference serviceReference : serviceReferences )
        {
            str += sep + serviceReference.identity() + ",active=" + serviceReference.isActive();
            sep = ", ";
        }
        return str += "}";
    }
}