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

package org.qi4j.spi.service.importer;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.service.*;
import org.qi4j.api.service.qualifier.ServiceQualifier;

import java.util.ArrayList;
import java.util.List;

/**
 * If several services are available with a given type, and you want to constrain
 * the current module to use a specific one, then use this importer. Specify a
 * ServiceQualifier.Selector criteria as meta-info for the service, which will be applied
 * to the list of available services, and the first match will be chosen.
 *
 * This importer will avoid selecting itself, as could be possible if the ServiceQualifier.first()
 * filter is used.
 */
public final class ServiceSelectorImporter
    implements ServiceImporter
{
    @Structure
    private ServiceFinder locator;

    public Object importService( ImportedServiceDescriptor serviceDescriptor )
        throws ServiceImporterException
    {
        ServiceQualifier selector = serviceDescriptor.metaInfo( ServiceQualifier.class );
        Class serviceType = serviceDescriptor.type();
        Iterable<ServiceReference<Object>> services = locator.findServices( serviceType );
        List<ServiceReference<Object>> filteredServices = new ArrayList<ServiceReference<Object>>();
        for( ServiceReference<Object> service : services )
        {
            ServiceQualifier selector1 = service.metaInfo( ServiceQualifier.class );
            if( selector1 != null && selector1 == selector )
            {
                continue;
            }

            filteredServices.add( service );
        }
        return ServiceQualifier.firstService( selector, filteredServices );
    }

    public boolean isActive( Object instance )
    {
        return true;
    }

    public boolean isAvailable( Object instance )
    {
        return !(instance instanceof AvailableService) || ((AvailableService) instance).isAvailable();
    }
}
