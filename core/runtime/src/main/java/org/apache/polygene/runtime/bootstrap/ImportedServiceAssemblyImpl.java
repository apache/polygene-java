/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package org.apache.polygene.runtime.bootstrap;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.apache.polygene.api.activation.Activator;
import org.apache.polygene.api.common.InvalidApplicationException;
import org.apache.polygene.api.common.MetaInfo;
import org.apache.polygene.api.common.Visibility;
import org.apache.polygene.api.identity.Identity;
import org.apache.polygene.api.identity.StringIdentity;
import org.apache.polygene.api.service.ServiceImporter;
import org.apache.polygene.api.service.importer.InstanceImporter;
import org.apache.polygene.api.structure.ModuleDescriptor;
import org.apache.polygene.bootstrap.ImportedServiceAssembly;
import org.apache.polygene.runtime.activation.ActivatorsModel;
import org.apache.polygene.runtime.service.ImportedServiceModel;

/**
 * Declaration of an imported Service.
 *
 * Created by {@link org.apache.polygene.runtime.bootstrap.ModuleAssemblyImpl#importedServices(Class[])}.
 */
public final class ImportedServiceAssemblyImpl
    implements ImportedServiceAssembly
{
    private final Class<?> serviceType;
    private final ModuleAssemblyImpl moduleAssembly;
    @SuppressWarnings( "raw" )
    Class<? extends ServiceImporter> serviceProvider = InstanceImporter.class;
    String identity;
    boolean importOnStartup = false;
    MetaInfo metaInfo = new MetaInfo();
    Visibility visibility = Visibility.module;
    List<Class<? extends Activator<?>>> activators = new ArrayList<>();

    public ImportedServiceAssemblyImpl( Class<?> serviceType, ModuleAssemblyImpl moduleAssembly )
    {
        this.serviceType = serviceType;
        this.moduleAssembly = moduleAssembly;
    }

    @Override
    public Stream<Class<?>> types()
    {
        return Stream.of( serviceType );
    }

    @SuppressWarnings( { "raw", "unchecked" } )
    void addImportedServiceModel( ModuleDescriptor module, List<ImportedServiceModel> serviceModels )
    {
        try
        {
            Identity id;
            if( identity == null )
            {
                id = generateId( serviceModels, serviceType );
            }
            else
            {
                id = new StringIdentity( identity );
            }

            ImportedServiceModel serviceModel = new ImportedServiceModel( module,
                                                                          serviceType,
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

    @SuppressWarnings( "raw" )
    private Identity generateId( List<ImportedServiceModel> serviceModels, Class serviceType )
    {
        // Find reference that is not yet used
        int idx = 0;
        Identity id = new StringIdentity( serviceType.getSimpleName() );
        boolean invalid;
        do
        {
            invalid = false;
            for( ImportedServiceModel serviceModel : serviceModels )
            {
                if( serviceModel.identity().equals( id ) )
                {
                    idx++;
                    id = new StringIdentity( serviceType.getSimpleName() + "_" + idx );
                    invalid = true;
                    break;
                }
            }
        }
        while( invalid );
        return id;
    }
}