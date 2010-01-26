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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.qi4j.api.common.InvalidApplicationException;
import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.bootstrap.ServiceDeclaration;
import org.qi4j.runtime.service.ServiceModel;

/**
 * Declaration of a Service. Created by {@link org.qi4j.runtime.bootstrap.ModuleAssemblyImpl#addServices(Class[])}.
 */
public final class ServiceDeclarationImpl
    implements ServiceDeclaration, Serializable
{
    private Iterable<Class<? extends ServiceComposite>> serviceTypes;
    private List<Class<?>> concerns = new ArrayList<Class<?>>();
    private List<Class<?>> sideEffects = new ArrayList<Class<?>>();
    private List<Class<?>> mixins = new ArrayList<Class<?>>();
    private ModuleAssemblyImpl moduleAssembly;
    private String identity;
    private boolean instantiateOnStartup = false;
    private MetaInfo metaInfo = new MetaInfo();
    private Visibility visibility = Visibility.module;

    public ServiceDeclarationImpl( Iterable<Class<? extends ServiceComposite>> serviceTypes,
                                   ModuleAssemblyImpl moduleAssembly
    )
    {
        this.serviceTypes = serviceTypes;
        this.moduleAssembly = moduleAssembly;
    }

    public ServiceDeclaration visibleIn( Visibility visibility )
    {
        this.visibility = visibility;
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

    public ServiceDeclaration setMetaInfo( Object serviceAttribute )
    {
        metaInfo.set( serviceAttribute );
        return this;
    }

    public ServiceDeclaration withConcerns( Class<?>... concerns )
    {
        this.concerns.addAll( Arrays.asList( concerns ) );
        return this;
    }

    public ServiceDeclaration withSideEffects( Class<?>... sideEffects )
    {
        this.sideEffects.addAll( Arrays.asList( sideEffects ) );
        return this;
    }

    public ServiceDeclaration withMixins( Class<?>... mixins )
    {
        this.mixins.addAll( Arrays.asList( mixins ) );
        return this;
    }

    void addServices( List<ServiceModel> serviceModels )
    {
        for( Class<? extends ServiceComposite> serviceType : serviceTypes )
        {
            try
            {
                String id = identity;
                if( id == null )
                {
                    id = generateId( serviceModels, serviceType );
                }

                ServiceModel serviceModel = ServiceModel.newModel( serviceType,
                                                                   visibility,
                                                                   metaInfo,
                                                                   concerns,
                                                                   sideEffects,
                                                                   mixins,
                                                                   moduleAssembly.name(),
                                                                   id,
                                                                   instantiateOnStartup );
                serviceModels.add( serviceModel );
            }
            catch( Exception e )
            {
                throw new InvalidApplicationException( "Could not register " + serviceType.getName(), e );
            }
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
