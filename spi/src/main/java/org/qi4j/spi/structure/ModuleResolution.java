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

package org.qi4j.spi.structure;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.qi4j.composite.Composite;
import org.qi4j.spi.composite.CompositeResolution;
import org.qi4j.spi.composite.ObjectResolution;

/**
 * TODO
 */
public final class ModuleResolution
    implements Serializable
{
    private ModuleModel moduleModel;
    private List<ObjectResolution> objectResolutions;
    private Map<Class<? extends Composite>, ModuleModel> instantiableComposites;
    private Map<Class, ServiceDescriptor> availableServices;
    private ApplicationModel applicationModel;
    private LayerModel layerModel;
    private Iterable<CompositeResolution> compositeResolutions;

    public ModuleResolution( ModuleModel moduleModel,
                             ApplicationModel applicationModel,
                             LayerModel layerModel,
                             Map<Class<? extends Composite>, ModuleModel> instantiableComposites,
                             Iterable<CompositeResolution> compositeResolutions,
                             List<ObjectResolution> objectResolutions )
    {
        this.availableServices = availableServices;
        this.applicationModel = applicationModel;
        this.layerModel = layerModel;
        this.compositeResolutions = compositeResolutions;
        this.instantiableComposites = instantiableComposites;
        this.moduleModel = moduleModel;
        this.objectResolutions = objectResolutions;

        // Figure out what service types are available from this module
        this.availableServices = new HashMap<Class, ServiceDescriptor>();

        // Add extended interfaces, unless they have been specifically registered already
        for( ServiceDescriptor serviceDescriptor : moduleModel.getServiceDescriptors() )
        {
            Class serviceType = serviceDescriptor.getServiceType();
            availableServices.put( serviceType, serviceDescriptor );
            addSuperTypeProviders( serviceType, serviceDescriptor, availableServices );
        }
    }

    public ModuleModel getModuleModel()
    {
        return moduleModel;
    }

    public ApplicationModel getApplicationModel()
    {
        return applicationModel;
    }

    public LayerModel getLayerModel()
    {
        return layerModel;
    }

    public Map<Class<? extends Composite>, ModuleModel> getInstantiableComposites()
    {
        return instantiableComposites;
    }

    public Iterable<CompositeResolution> getCompositeResolutions()
    {
        return compositeResolutions;
    }

    public List<ObjectResolution> getObjectResolutions()
    {
        return objectResolutions;
    }

    public ServiceDescriptor getServiceDescriptor( Class serviceType )
    {
        do
        {
            ServiceDescriptor serviceDescriptor = availableServices.get( serviceType );
            if( serviceDescriptor != null )
            {
                return serviceDescriptor;
            }

            serviceType = serviceType.getSuperclass();
        }
        while( serviceType != null );

        return null;
    }

    private void addSuperTypeProviders( Class serviceType, ServiceDescriptor serviceDescriptor, Map<Class, ServiceDescriptor> availableServices )
    {
        Class[] extendedInterfaces = serviceType.getInterfaces();
        for( Class extendedInterface : extendedInterfaces )
        {
            if( extendedInterface.getMethods().length > 0 )
            {
                ServiceDescriptor specificDescriptor = availableServices.get( extendedInterface );
                if( specificDescriptor == null )
                {
                    availableServices.put( extendedInterface, serviceDescriptor );
                }
                addSuperTypeProviders( extendedInterface, serviceDescriptor, availableServices );
            }
        }
    }

    @Override public String toString()
    {
        return moduleModel.toString();
    }
}
