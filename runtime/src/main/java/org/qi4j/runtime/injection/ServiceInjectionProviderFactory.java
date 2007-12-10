/*
 * Copyright (c) 2007, Rickard …berg. All Rights Reserved.
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

import org.qi4j.spi.injection.BindingContext;
import org.qi4j.spi.injection.InjectionContext;
import org.qi4j.spi.injection.InjectionProvider;
import org.qi4j.spi.injection.InjectionProviderException;
import org.qi4j.spi.injection.InjectionProviderFactory;
import org.qi4j.spi.injection.InjectionResolution;
import org.qi4j.spi.injection.InvalidInjectionException;
import org.qi4j.spi.service.ServiceProvider;
import org.qi4j.spi.service.ServiceProviderException;

public class ServiceInjectionProviderFactory
    implements InjectionProviderFactory
{
    public InjectionProvider newInjectionProvider( BindingContext bindingContext ) throws InvalidInjectionException
    {
        InjectionResolution resolution = bindingContext.getInjectionResolution();
        Class serviceType = resolution.getInjectionModel().getInjectionClass();
        ServiceProvider provider = bindingContext.getModuleResolution().getServiceProvider( serviceType );
        if( provider == null )
        {
            provider = bindingContext.getLayerResolution().getServiceProvider( serviceType );
        }

        if( provider == null )
        {
            throw new InvalidInjectionException( "No service provider found for type " + serviceType.getName() );
        }

        return new ServiceInjectionProvider( provider, resolution );
    }

    static class ServiceInjectionProvider
        implements InjectionProvider
    {
        private ServiceProvider serviceProvider;
        private InjectionResolution injectionResolution;

        public ServiceInjectionProvider( ServiceProvider serviceProvider, InjectionResolution injectionResolution )
        {
            this.serviceProvider = serviceProvider;
            this.injectionResolution = injectionResolution;
        }

        public Object provideInjection( InjectionContext context ) throws InjectionProviderException
        {
            try
            {
                Object service = serviceProvider.getService( injectionResolution, context );
                return service;
            }
            catch( ServiceProviderException e )
            {
                throw new InjectionProviderException( "Could not provide injected value", e );
            }
        }
    }
}
