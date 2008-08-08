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

package org.qi4j.runtime.bootstrap;

import java.io.Serializable;
import java.util.List;
import org.qi4j.bootstrap.ServiceDeclaration;
import org.qi4j.composite.Composite;
import org.qi4j.runtime.service.ServiceModel;
import org.qi4j.service.ServiceComposite;
import org.qi4j.service.ServiceInstanceFactory;
import org.qi4j.spi.service.provider.DefaultServiceInstanceFactory;
import org.qi4j.structure.Visibility;
import org.qi4j.util.MetaInfo;

/**
 * Declaration of a Service. Created by {@link org.qi4j.runtime.bootstrap.ModuleAssemblyImpl#addServices(Class[])}.
 */
public final class ServiceDeclarationImpl
    implements ServiceDeclaration
{
    private Class<? extends ServiceInstanceFactory> serviceProvider = DefaultServiceInstanceFactory.class;
    private Iterable<Class<? extends ServiceComposite>> serviceTypes;
    private ModuleAssemblyImpl moduleAssembly;
    private String identity;
    private boolean instantiateOnStartup = false;
    private MetaInfo metaInfo = new MetaInfo();
    private Visibility visibility = Visibility.module;

    public ServiceDeclarationImpl( Iterable<Class<? extends ServiceComposite>> serviceTypes, ModuleAssemblyImpl moduleAssembly )
    {
        this.serviceTypes = serviceTypes;
        this.moduleAssembly = moduleAssembly;
    }

    public ServiceDeclaration visibleIn( Visibility visibility )
    {
        this.visibility = visibility;
        return this;
    }

    public ServiceDeclaration providedBy( Class<? extends ServiceInstanceFactory> sip )
    {
        serviceProvider = sip;
        return this;
    }

    public ServiceDeclaration identifiedBy( String identity )
    {
        this.identity = identity;
        return this;
    }

    public ServiceDeclaration instantiateOnStartup()
    {
        instantiateOnStartup = true;
        return this;
    }

    public ServiceDeclaration setMetaInfo( Serializable serviceAttribute )
    {
        metaInfo.set( serviceAttribute );
        return this;
    }

    void addServices( List<ServiceModel> serviceModels )
    {
        for( Class<? extends Composite> serviceType : serviceTypes )
        {
            String id = identity;
            if( id == null )
            {
                id = generateId( serviceModels, serviceType );
            }

            ServiceModel serviceModel = new ServiceModel( serviceType,
                                                          visibility,
                                                          serviceProvider,
                                                          id,
                                                          instantiateOnStartup,
                                                          new MetaInfo( metaInfo ),
                                                          moduleAssembly.name() );
            serviceModels.add( serviceModel );
        }
    }

    private String generateId( List<ServiceModel> serviceModels, Class serviceType )
    {
        // Find identity that is not yet used
        int idx = 0;
        String id = serviceType.getSimpleName();
        boolean invalid;
        do
        {
            invalid = false;
            for( ServiceModel serviceModel : serviceModels )
            {
                if( serviceModel.identity().equals( id ) )
                {
                    idx++;
                    id = serviceType.getSimpleName() + "_" + idx;
                    invalid = true;
                    break;
                }
            }
        }
        while( invalid );
        return id;
    }
}
