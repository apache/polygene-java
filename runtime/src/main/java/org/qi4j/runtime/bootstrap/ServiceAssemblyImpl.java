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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.qi4j.api.common.InvalidApplicationException;
import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.bootstrap.ServiceAssembly;
import org.qi4j.bootstrap.ServiceDeclaration;
import org.qi4j.runtime.service.ServiceModel;

/**
 * Assembly of a Service.
 */
public final class ServiceAssemblyImpl
    implements ServiceAssembly
{
    private ModuleAssemblyImpl moduleAssembly;
    private Class<? extends ServiceComposite> serviceType;
    List<Class<?>> concerns = new ArrayList<Class<?>>();
    List<Class<?>> sideEffects = new ArrayList<Class<?>>();
    List<Class<?>> mixins = new ArrayList<Class<?>>();
    List<Class<?>> roles = new ArrayList<Class<?>>();
    String identity;
    boolean instantiateOnStartup = false;
    MetaInfo metaInfo = new MetaInfo();
    Visibility visibility = Visibility.module;

    public ServiceAssemblyImpl( Class<? extends ServiceComposite> serviceType,
                                ModuleAssemblyImpl moduleAssembly
    )
    {
        this.serviceType = serviceType;
        this.moduleAssembly = moduleAssembly;
    }

    @Override
    public Class<? extends ServiceComposite> type()
    {
        return serviceType;
    }

   @Override
   public String identity()
   {
      return identity;
   }

   void addServiceModel( List<ServiceModel> serviceModels, AssemblyHelper helper )
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
                                                               roles,
                                                               moduleAssembly.name(),
                                                               id,
                                                               instantiateOnStartup, helper );
            serviceModels.add( serviceModel );
        }
        catch( Exception e )
        {
            throw new InvalidApplicationException( "Could not register " + serviceType.getName(), e );
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
