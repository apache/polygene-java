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

package org.apache.polygene.api.service.importer;

import org.apache.polygene.api.identity.Identity;
import org.apache.polygene.api.injection.scope.Structure;
import org.apache.polygene.api.service.ImportedServiceDescriptor;
import org.apache.polygene.api.service.ServiceFinder;
import org.apache.polygene.api.service.ServiceImporter;
import org.apache.polygene.api.service.ServiceImporterException;
import org.apache.polygene.api.service.ServiceReference;

/**
 * Use a registered service that implements ServiceImporter to do the actual
 * import. The service id of the service that this importer should delegate to must
 * be set as meta-info on this service. Example:
 * <pre><code>
 * module.services(MyServiceImporterService.class).identifiedBy("someid");
 * module.importedServices(OtherService.class).importedBy(ServiceInstanceImporter.class).setMetaInfo("someid");
 * </code></pre>
 */
public class ServiceInstanceImporter<T>
    implements ServiceImporter<T>
{
    @Structure
    private ServiceFinder finder;

    private ServiceImporter<T> service;

    private Identity serviceId;

    @Override
    public T importService( ImportedServiceDescriptor importedServiceDescriptor )
        throws ServiceImporterException
    {
        serviceId = importedServiceDescriptor.metaInfo( Identity.class );
        return serviceImporter().importService( importedServiceDescriptor );
    }

    @Override
    public boolean isAvailable( T instance )
    {
        return serviceImporter().isAvailable( instance );
    }

    @SuppressWarnings( {"raw", "unchecked"} )
    private ServiceImporter<T> serviceImporter()
    {
        if( service == null )
        {
            service = finder.findServices( ServiceImporter.class )
                            .filter( ref -> ref.identity().equals( serviceId ) )
                            .findFirst().map( ServiceReference::get )
                            .orElseThrow( () -> new ServiceImporterException(
                                "No service importer with id '" + serviceId + "' was found" )
                            );
        }
        return service;
    }
}
