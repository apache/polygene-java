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

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.service.qualifier.Qualifier;
import org.qi4j.api.specification.Specification;
import org.qi4j.bootstrap.InvalidInjectionException;
import org.qi4j.runtime.injection.DependencyModel;
import org.qi4j.runtime.injection.InjectionContext;
import org.qi4j.runtime.injection.InjectionProvider;
import org.qi4j.runtime.injection.InjectionProviderFactory;
import org.qi4j.runtime.model.Resolution;
import org.qi4j.runtime.service.ImportedServiceModel;
import org.qi4j.runtime.service.ServiceModel;
import org.qi4j.runtime.structure.ModuleInstance;
import org.qi4j.runtime.structure.ModuleModel;
import org.qi4j.runtime.structure.ModuleVisitor;

import static org.qi4j.api.util.Annotations.*;
import static org.qi4j.api.util.Iterables.*;

public final class ServiceInjectionProviderFactory
    implements InjectionProviderFactory, Serializable
{
    public InjectionProvider newInjectionProvider( Resolution resolution, DependencyModel dependencyModel )
        throws InvalidInjectionException
    {
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
                Type serviceType = ( (ParameterizedType) arguments[ 0 ] ).getActualTypeArguments()[ 0 ];

                ServicesFinder servicesFinder = new ServicesFinder();
                servicesFinder.serviceType = serviceType;

                resolution.module().visitModules( servicesFinder );

                return new IterableServiceReferenceProvider( servicesFinder, serviceQualifier );
            }
            else
            {
                // @Service Iterable<MyService> services
                Class serviceType = dependencyModel.injectionClass();

                ServicesFinder servicesFinder = new ServicesFinder();
                servicesFinder.serviceType = serviceType;

                resolution.module().visitModules( servicesFinder );

                return new IterableServiceProvider( servicesFinder, serviceQualifier );
            }
        }
        else if( dependencyModel.rawInjectionType().equals( ServiceReference.class ) )
        {
            // @Service ServiceReference<MyService> serviceRef
            Type serviceType = dependencyModel.injectionClass();
            if( serviceQualifier == null )
            {
                ServiceFinder serviceFinder = new ServiceFinder();
                serviceFinder.serviceType = serviceType;
                resolution.module().visitModules( serviceFinder );

                if( serviceFinder.identity == null )
                {
                    return null;
                }

                return new ServiceReferenceProvider( serviceFinder );
            }
            else
            {
                ServicesFinder serviceFinder = new ServicesFinder();
                serviceFinder.serviceType = serviceType;
                resolution.module().visitModules( serviceFinder );

                if( serviceFinder.serviceIdentities.size() + serviceFinder.importedServiceIdentities.size() == 0 )
                {
                    return null;
                }

                return new ServiceReferenceProvider( serviceFinder, serviceQualifier );
            }
        }
        else
        {
            // @Service MyService service
            Type serviceType = dependencyModel.injectionType();
            if( serviceQualifier == null )
            {
                ServiceFinder serviceFinder = new ServiceFinder();
                serviceFinder.serviceType = serviceType;
                resolution.module().visitModules( serviceFinder );

                if( serviceFinder.identity == null )
                {
                    return null;
                }

                return new ServiceProvider( serviceFinder );
            }
            else
            {
                ServicesFinder serviceFinder = new ServicesFinder();
                serviceFinder.serviceType = serviceType;
                resolution.module().visitModules( serviceFinder );

                if( serviceFinder.serviceIdentities.size() + serviceFinder.importedServiceIdentities.size() == 0 )
                {
                    return null;
                }

                return new ServiceProvider( serviceFinder, serviceQualifier );
            }
        }
    }

    private static class IterableServiceReferenceProvider
        extends ServiceInjectionProvider
    {
        private IterableServiceReferenceProvider( ServiceFinder serviceFinder )
        {
            super( serviceFinder );
        }

        private IterableServiceReferenceProvider( ServicesFinder servicesFinder,
                                                  Specification<ServiceReference<?>> serviceQualifier
        )
        {
            super( servicesFinder, serviceQualifier );
        }

        public synchronized Object provideInjection( InjectionContext context )
            throws InjectionProviderException
        {
            return getServiceReferences( context );
        }
    }

    private static class IterableServiceProvider
        extends ServiceInjectionProvider
    {
        private IterableServiceProvider( ServiceFinder serviceFinder )
        {
            super( serviceFinder );
        }

        private IterableServiceProvider( ServicesFinder servicesFinder,
                                         Specification<ServiceReference<?>> serviceQualifier
        )
        {
            super( servicesFinder, serviceQualifier );
        }

        public synchronized Object provideInjection( final InjectionContext context )
            throws InjectionProviderException
        {
            return new Iterable()
            {
                public Iterator iterator()
                {
                    final Iterator<ServiceReference<Object>> iter = getServiceReferences( context ).iterator();
                    return new Iterator()
                    {
                        public boolean hasNext()
                        {
                            return iter.hasNext();
                        }

                        public Object next()
                        {
                            ServiceReference<Object> serviceRef = iter.next();
                            return serviceRef.get();
                        }

                        public void remove()
                        {
                        }
                    };
                }
            };
        }
    }

    private static class ServiceReferenceProvider
        extends ServiceInjectionProvider
    {
        ServiceReferenceProvider( ServiceFinder serviceFinder )
        {
            super( serviceFinder );
        }

        ServiceReferenceProvider( ServicesFinder servicesFinder, Specification<ServiceReference<?>> serviceQualifier )
        {
            super( servicesFinder, serviceQualifier );
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
        ServiceProvider( ServiceFinder serviceFinder )
        {
            super( serviceFinder );
        }

        ServiceProvider( ServicesFinder servicesFinder, Specification<ServiceReference<?>> selector )
        {
            super( servicesFinder, selector );
        }

        public synchronized Object provideInjection( InjectionContext context )
            throws InjectionProviderException
        {
            ServiceReference<Object> ref = getServiceReference( context );

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
        private ServiceFinder serviceFinder;
        private ServicesFinder servicesFinder;
        private Specification<ServiceReference<?>> serviceQualifier;

        protected ServiceInjectionProvider( ServiceFinder serviceFinder )
        {
            this.serviceFinder = serviceFinder;
        }

        protected ServiceInjectionProvider( ServicesFinder servicesFinder,
                                            Specification<ServiceReference<?>> serviceQualifier
        )
        {
            this.servicesFinder = servicesFinder;
            this.serviceQualifier = serviceQualifier;
        }

        protected ServiceReference<Object> getServiceReference( InjectionContext context )
        {
            ModuleMapper mapper = new ModuleMapper();
            context.moduleInstance().visitModules( mapper );
            if( serviceQualifier == null )
            {
                if( serviceFinder.identity != null )
                {
                    if( serviceFinder.imported )
                    {
                        return mapper.modules
                            .get( serviceFinder.module )
                            .importedServices()
                            .getServiceWithIdentity( serviceFinder.identity );
                    }
                    else
                    {
                        return mapper.modules
                            .get( serviceFinder.module )
                            .services()
                            .getServiceWithIdentity( serviceFinder.identity );
                    }
                }

                return null;
            }
            else
            {
                for( Map.Entry<ModuleModel, List<String>> entry : servicesFinder.serviceIdentities.entrySet() )
                {
                    ModuleInstance moduleInstance = mapper.modules.get( entry.getKey() );
                    for( String identity : entry.getValue() )
                    {
                        ServiceReference<Object> serviceRef = moduleInstance.services()
                            .getServiceWithIdentity( identity );
                        if( serviceQualifier.satisfiedBy( serviceRef ) )
                        {
                            return serviceRef;
                        }
                    }
                }
                for( Map.Entry<ModuleModel, List<String>> entry : servicesFinder.importedServiceIdentities.entrySet() )
                {
                    ModuleInstance moduleInstance = mapper.modules.get( entry.getKey() );
                    for( String identity : entry.getValue() )
                    {
                        ServiceReference<Object> serviceRef = moduleInstance.importedServices()
                            .getServiceWithIdentity( identity );
                        if( serviceQualifier.satisfiedBy( serviceRef ) )
                        {
                            return serviceRef;
                        }
                    }
                }

                return null;
            }
        }

        protected Iterable<ServiceReference<Object>> getServiceReferences( final InjectionContext context )
        {
            return new Iterable<ServiceReference<Object>>()
            {
                public Iterator<ServiceReference<Object>> iterator()
                {
                    List<ServiceReference<Object>> serviceReferences = new ArrayList<ServiceReference<Object>>();

                    ModuleMapper mapper = new ModuleMapper();
                    context.moduleInstance().visitModules( mapper );

                    for( Map.Entry<ModuleModel, List<String>> entry : servicesFinder.serviceIdentities.entrySet() )
                    {
                        ModuleInstance moduleInstance = mapper.modules.get( entry.getKey() );
                        for( String identity : entry.getValue() )
                        {
                            ServiceReference<Object> serviceRef = moduleInstance.services()
                                .getServiceWithIdentity( identity );
                            if( serviceQualifier == null || serviceQualifier.satisfiedBy( serviceRef ) )
                            {
                                serviceReferences.add( serviceRef );
                            }
                        }
                    }
                    for( Map.Entry<ModuleModel, List<String>> entry : servicesFinder.importedServiceIdentities
                        .entrySet() )
                    {
                        ModuleInstance moduleInstance = mapper.modules.get( entry.getKey() );
                        for( String identity : entry.getValue() )
                        {
                            ServiceReference<Object> serviceRef = moduleInstance.importedServices()
                                .getServiceWithIdentity( identity );
                            if( serviceQualifier == null || serviceQualifier.satisfiedBy( serviceRef ) )
                            {
                                serviceReferences.add( serviceRef );
                            }
                        }
                    }

                    return serviceReferences.iterator();
                }
            };
        }
    }

    static class ServiceFinder
        implements ModuleVisitor<RuntimeException>
    {
        public Type serviceType;

        public boolean imported;
        public String identity;
        public ModuleModel module;

        public boolean visitModule( ModuleInstance moduleInstance, ModuleModel moduleModel, Visibility visibility )
        {
            ServiceModel model = moduleModel.services().getServiceFor( serviceType, visibility );
            if( model != null )
            {
                identity = model.identity();
                module = moduleModel;
            }

            ImportedServiceModel importedServiceModel = moduleModel.importedServicesModel()
                .getServiceFor( serviceType, visibility );
            if( importedServiceModel != null )
            {
                identity = importedServiceModel.identity();
                module = moduleModel;
                imported = true;
            }

            return identity == null;
        }
    }

    static class ServicesFinder
        implements ModuleVisitor<RuntimeException>, Serializable
    {
        public Type serviceType;

        private Map<ModuleModel, List<String>> serviceIdentities = new LinkedHashMap<ModuleModel, List<String>>();
        private Map<ModuleModel, List<String>> importedServiceIdentities = new LinkedHashMap<ModuleModel, List<String>>();

        private List<ServiceModel> services = new ArrayList<ServiceModel>();
        private List<ImportedServiceModel> importedServices = new ArrayList<ImportedServiceModel>();

        public boolean visitModule( ModuleInstance moduleInstance, ModuleModel moduleModel, Visibility visibility )
        {
            moduleModel.services().getServicesFor( serviceType, visibility, services );
            if( !services.isEmpty() )
            {
                List<String> identities = serviceIdentities.get( moduleModel );
                if( identities == null )
                {
                    serviceIdentities.put( moduleModel, identities = new ArrayList<String>( services.size() ) );
                }
                for( ServiceModel service : services )
                {
                    identities.add( service.identity() );
                }
            }

            moduleModel.importedServicesModel().getServicesFor( serviceType, visibility, importedServices );
            if( !importedServices.isEmpty() )
            {
                List<String> identities = importedServiceIdentities.get( moduleModel );
                if( identities == null )
                {
                    importedServiceIdentities.put( moduleModel, identities = new ArrayList<String>( importedServices.size() ) );
                }
                for( ImportedServiceModel service : importedServices )
                {
                    identities.add( service.identity() );
                }
            }

            services.clear();
            importedServices.clear();

            return true;
        }
    }

    static class ModuleMapper
        implements ModuleVisitor<RuntimeException>
    {
        public Map<ModuleModel, ModuleInstance> modules = new HashMap<ModuleModel, ModuleInstance>();

        public boolean visitModule( ModuleInstance moduleInstance, ModuleModel moduleModel, Visibility visibility )
        {
            modules.put( moduleModel, moduleInstance );

            return true;
        }
    }
}
