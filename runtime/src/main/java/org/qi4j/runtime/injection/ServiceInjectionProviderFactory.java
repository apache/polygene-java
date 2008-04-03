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

package org.qi4j.runtime.injection;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import org.qi4j.service.ServiceReference;
import org.qi4j.spi.injection.BindingContext;
import org.qi4j.spi.injection.InjectionContext;
import org.qi4j.spi.injection.InjectionProvider;
import org.qi4j.spi.injection.InjectionProviderException;
import org.qi4j.spi.injection.InjectionProviderFactory;
import org.qi4j.spi.injection.InjectionResolution;
import org.qi4j.spi.injection.InvalidInjectionException;

public final class ServiceInjectionProviderFactory
    implements InjectionProviderFactory
{
    public InjectionProvider newInjectionProvider( BindingContext bindingContext ) throws InvalidInjectionException
    {
        InjectionResolution resolution = bindingContext.getInjectionResolution();

        if( resolution.getInjectionModel().getRawInjectionType().equals( Iterable.class ) )
        {
            if( resolution.getInjectionModel().getInjectionClass().equals( ServiceReference.class ) )
            {
                // @Service Iterable<ServiceReference<MyService>> serviceRefs
                Type[] arguments = ( (ParameterizedType) resolution.getInjectionModel().getInjectionType() ).getActualTypeArguments();
                Class serviceType = (Class) ( (ParameterizedType) arguments[ 0 ] ).getActualTypeArguments()[ 0 ];
                return new IterableServiceReferenceProvider( serviceType );

            }
            else
            {
                // @Service Iterable<MyService> services
                Class serviceType = resolution.getInjectionModel().getInjectionClass();
                return new IterableServiceProvider( serviceType );
            }

        }
        else if( resolution.getInjectionModel().getRawInjectionType().equals( ServiceReference.class ) )
        {
            // @Service ServiceReference<MyService> serviceRef
            Class serviceType = resolution.getInjectionModel().getInjectionClass();
            return new ServiceReferenceProvider( serviceType );
        }
        else
        {
            // @Service MyService service
            Class serviceType = resolution.getInjectionModel().getInjectionClass();
            return new ServiceProvider( serviceType );
        }
    }

    static class IterableServiceReferenceProvider
        implements InjectionProvider
    {
        private Class serviceType;

        public IterableServiceReferenceProvider( Class serviceType )
        {
            this.serviceType = serviceType;
        }

        public Object provideInjection( InjectionContext context ) throws InjectionProviderException
        {
            Iterable<ServiceReference> serviceReferences = context.getStructureContext().getServiceLocator().lookupServices( serviceType );
            return serviceReferences;
        }
    }

    static class IterableServiceProvider
        implements InjectionProvider
    {
        private Class serviceType;

        public IterableServiceProvider( Class serviceType )
        {
            this.serviceType = serviceType;
        }

        public Object provideInjection( InjectionContext context ) throws InjectionProviderException
        {
            Iterable<ServiceReference> serviceReferences = context.getStructureContext().getServiceLocator().lookupServices( serviceType );
            List serviceInstances = new ArrayList();
            for( ServiceReference serviceReference : serviceReferences )
            {
                serviceInstances.add( serviceReference.get() );
            }
            return serviceInstances;
        }
    }

    static class ServiceReferenceProvider
        implements InjectionProvider
    {
        private Class serviceType;

        public ServiceReferenceProvider( Class serviceType )
        {
            this.serviceType = serviceType;
        }

        public Object provideInjection( InjectionContext context ) throws InjectionProviderException
        {
            return context.getStructureContext().getServiceLocator().lookupServices( serviceType );
        }
    }

    static class ServiceProvider
        implements InjectionProvider
    {
        private Class serviceType;

        public ServiceProvider( Class serviceType )
        {
            this.serviceType = serviceType;
        }

        public Object provideInjection( InjectionContext context ) throws InjectionProviderException
        {
            ServiceReference serviceReference = context.getStructureContext().getServiceLocator().lookupService( serviceType );
            if( serviceReference == null )
            {
                return null;
            }

            Object service = serviceReference.get();
            return service;
        }
    }
}
