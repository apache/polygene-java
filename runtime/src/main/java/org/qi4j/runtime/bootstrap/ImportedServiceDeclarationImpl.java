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
import org.qi4j.api.common.InvalidApplicationException;
import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.service.ServiceImporter;
import org.qi4j.bootstrap.ImportedServiceDeclaration;
import org.qi4j.runtime.service.ImportedServiceModel;
import org.qi4j.spi.service.importer.InstanceImporter;

/**
 * Declaration of an imported Service. Created by {@link ModuleAssemblyImpl#importServices(Class[])}.
 */
public final class ImportedServiceDeclarationImpl
    implements ImportedServiceDeclaration, Serializable
{
    private Class<? extends ServiceImporter> serviceProvider = InstanceImporter.class;
    private Iterable<Class> serviceTypes;
    private ModuleAssemblyImpl moduleAssembly;
    private String identity;
    private MetaInfo metaInfo = new MetaInfo();
    private Visibility visibility = Visibility.module;

    public ImportedServiceDeclarationImpl( Iterable<Class> serviceTypes,
                                           ModuleAssemblyImpl moduleAssembly
    )
    {
        this.serviceTypes = serviceTypes;
        this.moduleAssembly = moduleAssembly;
    }

    public ImportedServiceDeclaration visibleIn( Visibility visibility )
    {
        this.visibility = visibility;
        return this;
    }

    public ImportedServiceDeclaration importedBy( Class<? extends ServiceImporter> sip )
    {
        serviceProvider = sip;
        return this;
    }

    public ImportedServiceDeclaration identifiedBy( String identity )
    {
        this.identity = identity;
        return this;
    }

    public ImportedServiceDeclaration setMetaInfo( Object serviceAttribute )
    {
        metaInfo.set( serviceAttribute );
        return this;
    }

    void addServices( List<ImportedServiceModel> serviceModels )
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

                ImportedServiceModel serviceModel = new ImportedServiceModel( serviceType,
                                                                              visibility,
                                                                              serviceProvider,
                                                                              id,
                                                                              new MetaInfo( metaInfo ).withAnnotations( serviceType ),
                                                                              moduleAssembly.name() );
                serviceModels.add( serviceModel );
            }
            catch( Exception e )
            {
                throw new InvalidApplicationException( "Could not register " + serviceType.getName(), e );
            }
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