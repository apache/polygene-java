/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.spi.service.provider;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.service.ServiceFinder;
import org.qi4j.api.service.ServiceImporter;
import org.qi4j.api.service.ServiceImporterException;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.service.ImportedServiceDescriptor;

/**
 * TODO
 */
public class ServiceIdImporter
    implements ServiceImporter
{
    private @Structure ServiceFinder locator;

    private ServiceReference serviceRef;
    private Object instance;

    public Object importService( ImportedServiceDescriptor serviceDescriptor ) throws ServiceImporterException
    {
        if( serviceRef == null )
        {
            ServiceId id = serviceDescriptor.metaInfo().get( ServiceId.class );
            String identityFilter = id == null ? null : id.id();
            Class serviceType = serviceDescriptor.type();
            Iterable<ServiceReference<?>> services = locator.findServices( serviceType );
            for( ServiceReference<?> service : services )
            {
                if( identityFilter == null || service.identity().equals( identityFilter ) )
                {
                    serviceRef = service;
                    instance = service.get();
                    break;
                }
            }
        }

        return instance;
    }
}
