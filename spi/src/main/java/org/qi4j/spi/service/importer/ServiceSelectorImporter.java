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
import org.qi4j.api.service.ImportedServiceDescriptor;
import org.qi4j.api.service.ServiceFinder;
import org.qi4j.api.service.ServiceImporter;
import org.qi4j.api.service.ServiceImporterException;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.service.ServiceSelector;

/**
 * If several services are available with a given type, and you want to constrain
 * the current module to use a specific one, then use this importer. Specify a
 * ServiceSelector.Selector criteria as meta-info for the service, which will be applied
 * to the list of available services, and the first match will be chosen.
 */
public class ServiceSelectorImporter
    implements ServiceImporter
{
    private @Structure ServiceFinder locator;

    public Object importService( ImportedServiceDescriptor serviceDescriptor ) throws ServiceImporterException
    {
        ServiceSelector.Selector selector = serviceDescriptor.metaInfo().get( ServiceSelector.Selector.class );
        Class serviceType = serviceDescriptor.type();
        Iterable<ServiceReference<Object>> services = locator.findServices( serviceType );
        return ServiceSelector.service( services, selector );
    }
}
