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

package org.apache.zest.api.service.importer;

import java.util.function.Predicate;
import java.util.stream.Stream;
import org.apache.zest.api.injection.scope.Structure;
import org.apache.zest.api.service.Availability;
import org.apache.zest.api.service.ImportedServiceDescriptor;
import org.apache.zest.api.service.ServiceFinder;
import org.apache.zest.api.service.ServiceImporter;
import org.apache.zest.api.service.ServiceImporterException;
import org.apache.zest.api.service.ServiceReference;

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
    @SuppressWarnings( { "raw", "unchecked" } )
    public T importService( ImportedServiceDescriptor serviceDescriptor )
        throws ServiceImporterException
    {
        Predicate<ServiceReference<?>> selector = serviceDescriptor.metaInfo( Predicate.class );
        Class serviceType = serviceDescriptor.types().findFirst().orElse( null );

        Stream<ServiceReference<T>> services = locator.findServices( serviceType );
        Predicate<ServiceReference<T>> filter = ref ->
        {
            Predicate selector1 = ref.metaInfo( Predicate.class );
            return selector1 == null || selector == selector1;
        };
        return services.filter( filter.and( selector ) )
                       .findFirst().map( ServiceReference::get )
                       .orElseThrow(
                           () -> new ServiceImporterException(
                               "Could not find any service to import that matches the given specification for "
                               + serviceDescriptor.identity() ) );
    }

    @Override
    public boolean isAvailable( T instance )
    {
        return !( instance instanceof Availability ) || ( (Availability) instance ).isAvailable();
    }
}
