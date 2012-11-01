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

package org.qi4j.api.service.importer;

import java.util.ArrayList;
import java.util.List;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.service.Availability;
import org.qi4j.api.service.ImportedServiceDescriptor;
import org.qi4j.api.service.ServiceFinder;
import org.qi4j.api.service.ServiceImporter;
import org.qi4j.api.service.ServiceImporterException;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.service.qualifier.ServiceQualifier;
import org.qi4j.functional.Iterables;
import org.qi4j.functional.Specification;

/**
 * If several services are available with a given type, and you want to constrain
 * the current module to use a specific one, then use this importer. Specify a
 * Specification&lt;ServiceReference&lt;T&gt;&gt; criteria as meta-info for the service, which will be applied
 * to the list of available services, and the first match will be chosen.
 *
 * This importer will avoid selecting itself, as could be possible if the ServiceQualifier.first()
 * filter is used.
 */
public final class ServiceSelectorImporter<T>
    implements ServiceImporter<T>
{
    @Structure
    private ServiceFinder locator;

    @Override
    public T importService( ImportedServiceDescriptor serviceDescriptor )
        throws ServiceImporterException
    {
        Specification<ServiceReference<?>> selector = serviceDescriptor.metaInfo( Specification.class );
        Class serviceType = Iterables.first( serviceDescriptor.types() );
        Iterable<ServiceReference<T>> services = locator.findServices( serviceType );
        List<ServiceReference<T>> filteredServices = new ArrayList<ServiceReference<T>>();
        for( ServiceReference<T> service : services )
        {
            Specification selector1 = service.metaInfo( Specification.class );
            if( selector1 != null && selector1 == selector )
            {
                continue;
            }

            filteredServices.add( service );
        }
        T service = ServiceQualifier.firstService( selector, filteredServices );
        if( service == null )
        {
            throw new ServiceImporterException( "Could not find any service to import that matches the given specification for " + serviceDescriptor
                .identity() );
        }
        return service;
    }

    @Override
    public boolean isAvailable( T instance )
    {
        return !( instance instanceof Availability ) || ( (Availability) instance ).isAvailable();
    }
}
