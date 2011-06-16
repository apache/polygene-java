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

import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.service.qualifier.Qualifier;
import org.qi4j.api.specification.Specification;
import org.qi4j.api.util.Function;
import org.qi4j.api.util.Iterables;
import org.qi4j.bootstrap.InvalidInjectionException;
import org.qi4j.runtime.injection.DependencyModel;
import org.qi4j.runtime.injection.InjectionContext;
import org.qi4j.runtime.injection.InjectionProvider;
import org.qi4j.runtime.injection.InjectionProviderFactory;
import org.qi4j.runtime.model.Resolution;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import static org.qi4j.api.util.Annotations.hasAnnotation;
import static org.qi4j.api.util.Iterables.*;

public final class ServiceInjectionProviderFactory
    implements InjectionProviderFactory, Serializable
{
    public InjectionProvider newInjectionProvider( Resolution resolution, DependencyModel dependencyModel )
        throws InvalidInjectionException
    {
        // TODO This could be changed to allow multiple @Qualifier annotations
        Annotation qualifierAnnotation = first( filter( hasAnnotation( Qualifier.class ), iterable( dependencyModel.annotations() ) ) );
        Specification<ServiceReference<?>> serviceQualifier = null;
        if( qualifierAnnotation != null )
        {
            Qualifier qualifier = qualifierAnnotation.annotationType().getAnnotation( Qualifier.class );
            try
            {
                serviceQualifier = qualifier.value().newInstance().qualifier( qualifierAnnotation );
            }
            catch( Exception e )
            {
                throw new InvalidInjectionException( "Could not instantiate qualifier serviceQualifier", e );
            }
        }

        if( dependencyModel.rawInjectionType().equals( Iterable.class ) )
        {
            if( dependencyModel.injectionClass().equals( ServiceReference.class ) )
            {
                // @Service Iterable<ServiceReference<MyService>> serviceRefs
                Type[] arguments = ( (ParameterizedType) dependencyModel.injectionType() ).getActualTypeArguments();
                Class<?> serviceType = (Class<Object>) ( (ParameterizedType) arguments[ 0 ] ).getActualTypeArguments()[ 0 ];

                return new IterableServiceReferenceProvider( serviceType, serviceQualifier );
            }
            else
            {
                // @Service Iterable<MyService> services
                Class serviceType = dependencyModel.injectionClass();

                return new IterableServiceProvider( serviceType, serviceQualifier );
            }
        }
        else if( dependencyModel.rawInjectionType().equals( ServiceReference.class ) )
        {
            // @Service ServiceReference<MyService> serviceRef
            Class<?> serviceType = dependencyModel.injectionClass();
            return new ServiceReferenceProvider( serviceType, serviceQualifier );
        }
        else
        {
            // @Service MyService service
            Class<?> serviceType = (Class<Object>) dependencyModel.injectionType();
            return new ServiceProvider( serviceType, serviceQualifier );
        }
    }

    private static class IterableServiceReferenceProvider
        extends ServiceInjectionProvider
    {
        private IterableServiceReferenceProvider( Class<?> serviceType,
                                                  Specification<ServiceReference<?>> serviceQualifier
        )
        {
            super( serviceType, serviceQualifier );
        }

        public synchronized Object provideInjection( InjectionContext context )
            throws InjectionProviderException
        {
            return getServiceReferences( context );
        }
    }

    private static class IterableServiceProvider
        extends ServiceInjectionProvider
        implements Function<ServiceReference<?>, Object>
    {
        private IterableServiceProvider( Class<?> serviceType,
                                         Specification<ServiceReference<?>> serviceQualifier
        )
        {
            super( serviceType, serviceQualifier );
        }

        public synchronized Object provideInjection( final InjectionContext context )
            throws InjectionProviderException
        {
            return Iterables.map( this, getServiceReferences( context ) );
        }

        @Override
        public Object map( ServiceReference<?> objectServiceReference )
        {
            return objectServiceReference.get();
        }
    }

    private static class ServiceReferenceProvider
        extends ServiceInjectionProvider
    {
        ServiceReferenceProvider( Class<?> serviceType, Specification<ServiceReference<?>> qualifier )
        {
            super( serviceType, qualifier );
        }

        public synchronized Object provideInjection( InjectionContext context )
            throws InjectionProviderException
        {
            return getServiceReference( context );
        }
    }

    private static class ServiceProvider
        extends ServiceInjectionProvider
    {
        ServiceProvider( Class<?> serviceType, Specification<ServiceReference<?>> qualifier )
        {
            super( serviceType, qualifier );
        }

        public synchronized Object provideInjection( InjectionContext context )
            throws InjectionProviderException
        {
            ServiceReference<?> ref = getServiceReference( context );

            if( ref != null )
            {
                return ref.get();
            }
            else
            {
                return null;
            }
        }
    }

    private abstract static class ServiceInjectionProvider
        implements InjectionProvider
    {
        private Class<?> serviceType;
        private Specification<ServiceReference<?>> serviceQualifier;

        protected ServiceInjectionProvider( Class<?> serviceType,
                                            Specification<ServiceReference<?>> serviceQualifier
        )
        {
            this.serviceType = serviceType;
            this.serviceQualifier = serviceQualifier;
        }

        protected ServiceReference<?> getServiceReference( InjectionContext context )
        {
            if( serviceQualifier == null )
            {
                return context.moduleInstance().serviceFinder().findService( serviceType );
            }
            else
            {
                return Iterables.first( Iterables.filter( serviceQualifier, context.moduleInstance().serviceFinder().findServices( serviceType ) ) );
            }
        }

        protected Iterable<ServiceReference<?>> getServiceReferences( final InjectionContext context )
        {
            if( serviceQualifier == null )
            {
                return context.moduleInstance().serviceFinder().findServices( (Class) serviceType );
            }
            else
            {
                return Iterables.filter( serviceQualifier, context.moduleInstance().serviceFinder().findServices( (Class)serviceType ) );
            }
        }
    }
}
