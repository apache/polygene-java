/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2012, Paul Merlin.
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
import java.util.List;
import org.qi4j.api.activation.Activator;
import org.qi4j.api.common.InvalidApplicationException;
import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.service.ServiceImporter;
import org.qi4j.api.service.importer.InstanceImporter;
import org.qi4j.bootstrap.ImportedServiceAssembly;
import org.qi4j.functional.Iterables;
import org.qi4j.runtime.activation.ActivatorsModel;
import org.qi4j.runtime.service.ImportedServiceModel;

/**
 * Declaration of an imported Service.
 *
 * Created by {@link org.qi4j.runtime.bootstrap.ModuleAssemblyImpl#importedServices(Class[])}.
 */
public final class ImportedServiceAssemblyImpl
    implements ImportedServiceAssembly
{
    private Class<?> serviceType;
    private ModuleAssemblyImpl moduleAssembly;
    Class<? extends ServiceImporter> serviceProvider = InstanceImporter.class;
    String identity;
    boolean importOnStartup = false;
    MetaInfo metaInfo = new MetaInfo();
    Visibility visibility = Visibility.module;
    List<Class<? extends Activator<?>>> activators = new ArrayList<Class<? extends Activator<?>>>();

    public ImportedServiceAssemblyImpl( Class<?> serviceType, ModuleAssemblyImpl moduleAssembly )
    {
        this.serviceType = serviceType;
        this.moduleAssembly = moduleAssembly;
    }

    @Override
    public Iterable<Class<?>> types()
    {
        return Iterables.<Class<?>>iterable( serviceType );
    }

    void addImportedServiceModel( List<ImportedServiceModel> serviceModels )
    {
        try
        {
            String id = identity;
            if( id == null )
            {
                id = generateId( serviceModels, serviceType );
            }

            ImportedServiceModel serviceModel = new ImportedServiceModel( serviceType,
                                                                          visibility,
                                                                          serviceProvider,
                                                                          id,
                                                                          importOnStartup,
                                                                          new MetaInfo( metaInfo ).withAnnotations( serviceType ),
                                                                          new ActivatorsModel( activators ),
                                                                          moduleAssembly.name() );
            serviceModels.add( serviceModel );
        }
        catch( Exception e )
        {
            throw new InvalidApplicationException( "Could not register " + serviceType.getName(), e );
        }
    }

    private String generateId( List<ImportedServiceModel> serviceModels, Class serviceType )
    {
        // Find identity that is not yet used
        int idx = 0;
        String id = serviceType.getSimpleName();
        boolean invalid;
        do
        {
            invalid = false;
            for( ImportedServiceModel serviceModel : serviceModels )
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