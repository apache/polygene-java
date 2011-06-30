/*
 * Copyright (c) 2010, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.api.service.importer;

/**
 * JAVADOC
 */

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.service.*;

/**
 * Use a registered service that implements ServiceImporter to do the actual
 * import. The service id of the service that this importer should delegate to must
 * be set as meta-info on this service. Example:
 * module.services(MyServiceImporterService.class).identifiedBy("someid");
 * module.importedServices(OtherService.class).importedBy(ServiceInstanceImporter.class).setMetaInfo("someid");
 */
public class ServiceInstanceImporter<T>
    implements ServiceImporter<T>
{
    @Structure
    ServiceFinder finder;

    ServiceImporter<T> service;

    String serviceId;

    public T importService( ImportedServiceDescriptor importedServiceDescriptor )
        throws ServiceImporterException
    {
        serviceId = importedServiceDescriptor.metaInfo( String.class );

        return getServiceImporter().importService( importedServiceDescriptor );
    }

    public boolean isActive( T o )
    {
        return getServiceImporter().isActive( o );
    }

    public boolean isAvailable( T instance )
    {
        return getServiceImporter().isAvailable( instance );
    }

    private ServiceImporter<T> getServiceImporter()
    {
        if( service == null )
        {
            for( ServiceReference<ServiceImporter> reference : finder.<ServiceImporter>findServices( ServiceImporter.class ) )
            {
                if( reference.identity().equals( serviceId ) )
                {
                    service = reference.get();
                    break;
                }
            }
        }

        if( service == null )
        {
            throw new ServiceImporterException( "No service importer with id '" + serviceId + "' was found" );
        }

        return service;
    }
}
