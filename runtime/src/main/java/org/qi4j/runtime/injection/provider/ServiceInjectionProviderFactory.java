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

package org.qi4j.runtime.injection.provider;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import org.qi4j.runtime.composite.Resolution;
import org.qi4j.runtime.injection.DependencyModel;
import org.qi4j.runtime.injection.InjectionContext;
import org.qi4j.runtime.injection.InjectionProvider;
import org.qi4j.runtime.injection.InjectionProviderFactory;
import org.qi4j.service.ServiceReference;

public final class ServiceInjectionProviderFactory
    implements InjectionProviderFactory
{
    public InjectionProvider newInjectionProvider( Resolution resolution, DependencyModel dependencyModel ) throws InvalidInjectionException
    {
        if( dependencyModel.rawInjectionType().equals( Iterable.class ) )
        {
            if( dependencyModel.injectionClass().equals( ServiceReference.class ) )
            {
                // @Service Iterable<ServiceReference<MyService>> serviceRefs
                Type[] arguments = ( (ParameterizedType) dependencyModel.injectionType() ).getActualTypeArguments();
                Type serviceType = ( (ParameterizedType) arguments[ 0 ] ).getActualTypeArguments()[ 0 ];
                return new IterableServiceReferenceProvider( serviceType );

            }
            else
            {
                // @Service Iterable<MyService> services
                Class serviceType = dependencyModel.injectionClass();
                return new IterableServiceProvider( serviceType );
            }

        }
        else if( dependencyModel.rawInjectionType().equals( ServiceReference.class ) )
        {
            // @Service ServiceReference<MyService> serviceRef
            Type serviceType = dependencyModel.injectionClass();
            return new ServiceReferenceProvider( serviceType );
        }
        else
        {
            // @Service MyService service
            Type serviceType = dependencyModel.injectionType();
            return new ServiceProvider( serviceType );
        }
    }

    static class IterableServiceReferenceProvider
        implements InjectionProvider
    {
        private final Type serviceType;

        public IterableServiceReferenceProvider( Type serviceType )
        {
            this.serviceType = serviceType;
        }

        public Object provideInjection( InjectionContext context ) throws InjectionProviderException
        {
            return context.moduleInstance().findServices( serviceType );
        }
    }

    private static class IterableServiceProvider
        implements InjectionProvider
    {
        private final Type serviceType;

        private IterableServiceProvider( Type serviceType )
        {
            this.serviceType = serviceType;
        }

        public Object provideInjection( InjectionContext context ) throws InjectionProviderException
        {
            Iterable<ServiceReference<Object>> serviceReferences = context.moduleInstance().findServices( serviceType );
            List<Object> serviceInstances = new ArrayList<Object>();
            for( ServiceReference<?> serviceReference : serviceReferences )
            {
                serviceInstances.add( serviceReference.get() );
            }
            return serviceInstances;
        }
    }

    private static class ServiceReferenceProvider
        implements InjectionProvider
    {
        private final Type serviceType;

        private ServiceReferenceProvider( Type serviceType )
        {
            this.serviceType = serviceType;
        }

        public Object provideInjection( InjectionContext context ) throws InjectionProviderException
        {
            return context.moduleInstance().findService( serviceType );
        }
    }

    private static class ServiceProvider
        implements InjectionProvider
    {
        private final Type serviceType;

        private ServiceProvider( Type serviceType )
        {
            this.serviceType = serviceType;
        }

        public Object provideInjection( InjectionContext context ) throws InjectionProviderException
        {
            ServiceReference<Object> serviceReference = context.moduleInstance().findService( serviceType );
            if( serviceReference == null )
            {
                return null;
            }

            return serviceReference.get();
        }
    }
}
