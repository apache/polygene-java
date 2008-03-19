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

package org.qi4j.runtime.structure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.qi4j.composite.Composite;
import org.qi4j.composite.CompositeBuilderFactory;
import org.qi4j.composite.ObjectBuilder;
import org.qi4j.composite.ObjectBuilderFactory;
import org.qi4j.entity.UnitOfWorkFactory;
import org.qi4j.runtime.entity.UnitOfWorkFactoryImpl;
import org.qi4j.runtime.service.ServiceReferenceInstance;
import org.qi4j.service.Activatable;
import org.qi4j.service.ActivationListener;
import org.qi4j.service.ActivationStatus;
import org.qi4j.service.ActivationStatusChange;
import org.qi4j.service.ServiceLocator;
import org.qi4j.service.ServiceReference;
import org.qi4j.spi.injection.StructureContext;
import org.qi4j.spi.service.ServiceInstanceProvider;
import org.qi4j.spi.structure.ServiceDescriptor;

/**
 * TODO
 */
public final class ModuleInstance
    implements Activatable, ServiceLocator
{
    private ModuleContext moduleContext;

    private Map<Class<? extends Composite>, ModuleInstance> moduleForPublicComposites;
    private Map<Class, ModuleInstance> moduleForPublicObjects;

    private CompositeBuilderFactory compositeBuilderFactory;
    private ObjectBuilderFactory objectBuilderFactory;
    private UnitOfWorkFactory unitOfWorkFactory;
    private ServiceLocator serviceLocator;
    private StructureContext structureContext;

    private ActivationStatus status = ActivationStatus.INACTIVE;
    private List<ActivationListener> activationListeners = new ArrayList<ActivationListener>();

    // List of active Services in this Module
    private List<ServiceReferenceInstance> serviceInstances = new ArrayList<ServiceReferenceInstance>();

    // For each type there may be zero, one or many active instances
    private Map<Class<?>, List<ServiceReferenceInstance>> serviceReferences = new HashMap<Class<?>, List<ServiceReferenceInstance>>();


    public ModuleInstance( ModuleContext moduleContext,
                           Map<Class<? extends Composite>, ModuleInstance> moduleInstances,
                           Map<Class, ModuleInstance> moduleForPublicObjects,
                           ServiceLocator layerServiceLocator )
    {
        this.moduleForPublicObjects = moduleForPublicObjects;
        this.moduleForPublicComposites = moduleInstances;
        this.moduleContext = moduleContext;

        compositeBuilderFactory = new ModuleCompositeBuilderFactory( this );
        objectBuilderFactory = new ModuleObjectBuilderFactory( this );
        unitOfWorkFactory = new UnitOfWorkFactoryImpl( this );
        serviceLocator = new ModuleServiceLocator( this, layerServiceLocator );

        structureContext = new StructureContext( compositeBuilderFactory, objectBuilderFactory, unitOfWorkFactory, serviceLocator );
    }

    public ModuleContext getModuleContext()
    {
        return moduleContext;
    }

    public StructureContext getStructureContext()
    {
        return structureContext;
    }

    public <T> ServiceReference<T> lookupService( Class<T> serviceType )
    {
        List<ServiceReferenceInstance> serviceRefs = serviceReferences.get( serviceType );
        if( serviceRefs != null )
        {
            if( !serviceRefs.isEmpty() )
            {
                return (ServiceReference<T>) serviceRefs.get( 0 );
            }
        }
        return null;
    }

    public <T> Iterable<ServiceReference<T>> lookupServices( Class<T> serviceType )
    {
        List<ServiceReferenceInstance> serviceRefs = serviceReferences.get( serviceType );
        if( serviceRefs == null )
        {
            return Collections.emptyList();
        }

        // TODO: Can this be done without copying the list?? Generics issue...
        List<ServiceReference<T>> typedServiceRefs = new ArrayList<ServiceReference<T>>( serviceRefs.size() );
        for( ServiceReferenceInstance serviceRef : serviceRefs )
        {
            typedServiceRefs.add( serviceRef );
        }
        return typedServiceRefs;
    }

    public ModuleInstance getModuleForPublicComposite( Class<? extends Composite> compositeType )
    {
        return moduleForPublicComposites.get( compositeType );
    }

    public ModuleInstance getModuleForPublicObject( Class objectType )
    {
        return moduleForPublicObjects.get( objectType );
    }

    public ModuleInstance getModuleForComposite( Class<? extends Composite> compositeType )
    {
        ModuleInstance realInstance = getModuleForPublicComposite( compositeType );
        if( realInstance == null )
        {
            realInstance = this;
        }
        return realInstance;
    }

    public ModuleInstance getModuleForObject( Class objectType )
    {
        ModuleInstance realInstance = getModuleForPublicObject( objectType );
        if( realInstance == null )
        {
            realInstance = this;
        }
        return realInstance;
    }

    public void activate() throws Exception
    {
        if( status == ActivationStatus.INACTIVE )
        {
            // Instantiate all services in this module
            Iterable<ServiceDescriptor> serviceDescriptors = getModuleContext().getModuleBinding().getModuleResolution().getModuleModel().getServiceDescriptors();
            for( ServiceDescriptor serviceDescriptor : serviceDescriptors )
            {
                Class<? extends ServiceInstanceProvider> providerType = serviceDescriptor.getServiceProvider();
                ObjectBuilder<? extends ServiceInstanceProvider> builder = objectBuilderFactory.newObjectBuilder( providerType );
                ServiceInstanceProvider sip = builder.newInstance();
                Class serviceType = serviceDescriptor.getServiceType();
                final ServiceReferenceInstance<Object> serviceReference = new ServiceReferenceInstance<Object>( serviceDescriptor, sip );
                registerServiceReference( serviceType, serviceReference );
                serviceInstances.add( serviceReference );
                activationListeners.add( new ActivationListener()
                {
                    public void onActivationStatusChange( ActivationStatusChange change ) throws Exception
                    {
                        if( change.getNewStatus() == ActivationStatus.STARTING )
                        {
                            serviceReference.activate();
                        }
                        else if( change.getNewStatus() == ActivationStatus.STOPPING )
                        {
                            serviceReference.passivate();
                        }
                    }
                } );
            }

            try
            {
                setActivationStatus( ActivationStatus.STARTING );
                setActivationStatus( ActivationStatus.ACTIVE );
            }
            catch( Exception e )
            {
                setActivationStatus( ActivationStatus.STOPPING );
                setActivationStatus( ActivationStatus.INACTIVE );

                throw e;
            }
        }
    }

    public void passivate() throws Exception
    {
        if( status == ActivationStatus.ACTIVE )
        {
            try
            {
                setActivationStatus( ActivationStatus.STOPPING );
            }
            catch( Exception e )
            {
                // Ignore
            }
            setActivationStatus( ActivationStatus.INACTIVE );

            serviceReferences.clear();
            serviceInstances.clear();
        }
    }

    private void setActivationStatus( ActivationStatus newStatus )
        throws Exception
    {
        status = newStatus;
        ActivationStatusChange change = new ActivationStatusChange( this, status );
        for( ActivationListener activationListener : activationListeners )
        {
            activationListener.onActivationStatusChange( change );
        }
    }

    private void registerServiceReference( Class serviceType, ServiceReferenceInstance<Object> serviceReference )
    {
        // Add to list - create list if none exists
        List<ServiceReferenceInstance> serviceRefs = serviceReferences.get( serviceType );
        if( serviceRefs == null )
        {
            serviceRefs = new ArrayList<ServiceReferenceInstance>();
            serviceReferences.put( serviceType, serviceRefs );
        }
        serviceRefs.add( serviceReference );

        Class[] extended = serviceType.getInterfaces();
        for( Class extendedType : extended )
        {
            registerServiceReference( extendedType, serviceReference );
        }
    }

}
