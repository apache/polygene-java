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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.service.ServiceReference;
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

public final class ServiceInjectionProviderFactory
    implements InjectionProviderFactory, Serializable
{
    public InjectionProvider newInjectionProvider( Resolution resolution, DependencyModel dependencyModel )
        throws InvalidInjectionException
    {
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

                return new IterableServiceReferenceProvider( servicesFinder );
            }
            else
            {
                // @Service Iterable<MyService> services
                Class serviceType = dependencyModel.injectionClass();

                ServicesFinder servicesFinder = new ServicesFinder();
                servicesFinder.serviceType = serviceType;

                resolution.module().visitModules( servicesFinder );

                return new IterableServiceProvider( servicesFinder );
            }
        }
        else if( dependencyModel.rawInjectionType().equals( ServiceReference.class ) )
        {
            // @Service ServiceReference<MyService> serviceRef
            Type serviceType = dependencyModel.injectionClass();
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
            // @Service MyService service
            Type serviceType = dependencyModel.injectionType();
            ServiceFinder serviceFinder = new ServiceFinder();
            serviceFinder.serviceType = serviceType;
            resolution.module().visitModules( serviceFinder );

            if( serviceFinder.identity == null )
            {
                return null;
            }

            return new ServiceProvider( serviceFinder );
        }
    }

    public interface ServiceInjector
    {
        List<String> injectedServices();
    }

    private static class IterableServiceReferenceProvider
        implements InjectionProvider, ServiceInjector, Serializable
    {
        private final ServicesFinder servicesFinder;

        public IterableServiceReferenceProvider( ServicesFinder servicesFinder )
        {
            this.servicesFinder = servicesFinder;
        }

        public synchronized Object provideInjection( InjectionContext context )
            throws InjectionProviderException
        {
            List<ServiceReference<Object>> serviceReferences;

            ModuleMapper mapper = new ModuleMapper();
            context.moduleInstance().visitModules( mapper );

            serviceReferences = new ArrayList<ServiceReference<Object>>();
            for( Map.Entry<ModuleModel, List<String>> entry : servicesFinder.serviceIdentities.entrySet() )
            {
                ModuleInstance moduleInstance = mapper.modules.get( entry.getKey() );
                for( String identity : entry.getValue() )
                {
                    serviceReferences.add( moduleInstance.services().getServiceWithIdentity( identity ) );
                }
            }
            for( Map.Entry<ModuleModel, List<String>> entry : servicesFinder.importedServiceIdentities.entrySet() )
            {
                ModuleInstance moduleInstance = mapper.modules.get( entry.getKey() );
                for( String identity : entry.getValue() )
                {
                    serviceReferences.add( moduleInstance.importedServices().getServiceWithIdentity( identity ) );
                }
            }

            return serviceReferences;
        }

        public List<String> injectedServices()
        {
            List<String> services = new ArrayList<String>();
            Collection<List<String>> stringLists = servicesFinder.serviceIdentities.values();
            for( List<String> stringList : stringLists )
            {
                services.addAll( stringList );
            }
            stringLists = servicesFinder.importedServiceIdentities.values();
            for( List<String> stringList : stringLists )
            {
                services.addAll( stringList );
            }
            return services;
        }
    }

    private static class IterableServiceProvider
        implements InjectionProvider, ServiceInjector, Serializable
    {
        private final ServicesFinder servicesFinder;

        private IterableServiceProvider( ServicesFinder servicesFinder )
        {
            this.servicesFinder = servicesFinder;
        }

        public synchronized Object provideInjection( InjectionContext context )
            throws InjectionProviderException
        {
            List<Object> serviceInstances;
            ModuleMapper mapper = new ModuleMapper();
            context.moduleInstance().visitModules( mapper );

            serviceInstances = new ArrayList<Object>();
            for( Map.Entry<ModuleModel, List<String>> entry : servicesFinder.serviceIdentities.entrySet() )
            {
                ModuleInstance moduleInstance = mapper.modules.get( entry.getKey() );
                for( String identity : entry.getValue() )
                {
                    Object service = moduleInstance.services().getServiceWithIdentity( identity ).get();
                    if( service != null )
                    {
                        serviceInstances.add( service );
                    }
                }
            }
            for( Map.Entry<ModuleModel, List<String>> entry : servicesFinder.importedServiceIdentities.entrySet() )
            {
                ModuleInstance moduleInstance = mapper.modules.get( entry.getKey() );
                for( String identity : entry.getValue() )
                {
                    Object service = moduleInstance.importedServices().getServiceWithIdentity( identity ).get();
                    if( service != null )
                    {
                        serviceInstances.add( service );
                    }
                }
            }

            return serviceInstances;
        }

        public List<String> injectedServices()
        {
            List<String> services = new ArrayList<String>();
            Collection<List<String>> stringLists = servicesFinder.serviceIdentities.values();
            for( List<String> stringList : stringLists )
            {
                services.addAll( stringList );
            }
            stringLists = servicesFinder.importedServiceIdentities.values();
            for( List<String> stringList : stringLists )
            {
                services.addAll( stringList );
            }
            return services;
        }
    }

    private static class ServiceReferenceProvider
        implements InjectionProvider, ServiceInjector, Serializable
    {
        private final ServiceFinder serviceFinder;

        private ServiceReferenceProvider( ServiceFinder serviceFinder )
        {
            this.serviceFinder = serviceFinder;
        }

        public synchronized Object provideInjection( InjectionContext context )
            throws InjectionProviderException
        {
            ServiceReference reference = null;

            if( serviceFinder.identity != null )
            {
                ModuleMapper mapper = new ModuleMapper();
                context.moduleInstance().visitModules( mapper );
                if( serviceFinder.imported )
                {
                    reference = mapper.modules
                        .get( serviceFinder.module )
                        .importedServices()
                        .getServiceWithIdentity( serviceFinder.identity );
                }
                else
                {
                    reference = mapper.modules
                        .get( serviceFinder.module )
                        .services()
                        .getServiceWithIdentity( serviceFinder.identity );
                }
            }

            return reference;
        }

        public List<String> injectedServices()
        {
            List<String> services = new ArrayList<String>();
            if( serviceFinder.identity != null )
            {
                services.add( serviceFinder.identity );
            }
            return services;
        }
    }

    private static class ServiceProvider
        implements InjectionProvider, ServiceInjector, Serializable
    {
        private final ServiceFinder serviceFinder;

        private ServiceProvider( ServiceFinder serviceFinder )
        {
            this.serviceFinder = serviceFinder;
        }

        public synchronized Object provideInjection( InjectionContext context )
            throws InjectionProviderException
        {
            Object instance = null;
            if( serviceFinder.identity != null )
            {
                ModuleMapper mapper = new ModuleMapper();
                context.moduleInstance().visitModules( mapper );

                if( serviceFinder.imported )
                {
                    instance = mapper.modules
                        .get( serviceFinder.module )
                        .importedServices()
                        .getServiceWithIdentity( serviceFinder.identity )
                        .get();
                }
                else
                {
                    instance = mapper.modules
                        .get( serviceFinder.module )
                        .services()
                        .getServiceWithIdentity( serviceFinder.identity )
                        .get();
                }
            }

            return instance;
        }

        public List<String> injectedServices()
        {
            List<String> services = new ArrayList<String>();
            if( serviceFinder.identity != null )
            {
                services.add( serviceFinder.identity );
            }
            return services;
        }
    }

    static class ServiceFinder
        implements ModuleVisitor
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
        implements ModuleVisitor, Serializable
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
        implements ModuleVisitor
    {
        public Map<ModuleModel, ModuleInstance> modules = new HashMap<ModuleModel, ModuleInstance>();

        public boolean visitModule( ModuleInstance moduleInstance, ModuleModel moduleModel, Visibility visibility )
        {
            modules.put( moduleModel, moduleInstance );

            return true;
        }
    }
}
