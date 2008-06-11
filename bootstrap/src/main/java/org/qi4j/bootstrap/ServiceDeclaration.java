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
import java.util.List;
import org.qi4j.runtime.service.qi.ServiceModel;
import org.qi4j.runtime.structure.qi.ModuleModel;
import org.qi4j.service.ServiceInstanceFactory;
import org.qi4j.spi.service.provider.DefaultServiceInstanceFactory;
import org.qi4j.structure.Visibility;
import org.qi4j.util.MetaInfo;

/**
 * Declaration of a Service. Created by {@link org.qi4j.bootstrap.ModuleAssembly#addServices(Class[])}.
 */
public final class ServiceDeclaration
{
    private Class<? extends ServiceInstanceFactory> serviceProvider = DefaultServiceInstanceFactory.class;
    private Iterable<Class> serviceTypes;
    private String identity;
    private boolean instantiateOnStartup = false;
    private MetaInfo metaInfo = new MetaInfo();
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

    public <K extends Serializable> ServiceDeclaration setMetaInfo( Serializable serviceAttribute )
    {
        metaInfo.set( serviceAttribute );
        return this;
    }

    void addServices( ModuleModel moduleModel, List<ServiceModel> serviceModels )
    {
        for( Class serviceType : serviceTypes )
        {
            ServiceModel serviceModel = new ServiceModel( serviceType,
                                                          visibility,
                                                          moduleModel,
                                                          serviceProvider,
                                                          identity,
                                                          instantiateOnStartup,
                                                          new MetaInfo( metaInfo ) );
            serviceModels.add( serviceModel );
        }
    }
}
