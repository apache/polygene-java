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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.qi4j.composite.Composite;
import org.qi4j.composite.CompositeBuilderFactory;
import org.qi4j.composite.ObjectBuilder;
import org.qi4j.composite.ObjectBuilderFactory;
import org.qi4j.entity.EntitySessionFactory;
import org.qi4j.runtime.entity.EntitySessionFactoryImpl;
import org.qi4j.service.Activatable;
import org.qi4j.service.ActivationListener;
import org.qi4j.service.ActivationStatus;
import org.qi4j.service.ActivationStatusChange;
import org.qi4j.service.ServiceStatus;
import org.qi4j.spi.service.ServiceInstanceProvider;
import org.qi4j.spi.service.ServiceRegistry;
import org.qi4j.spi.structure.ServiceDescriptor;

/**
 * TODO
 */
public final class ModuleInstance
    implements Activatable, ServiceStatus, ServiceRegistry
{
    private ModuleContext moduleContext;

    private Map<Class<? extends Composite>, ModuleInstance> moduleForPublicComposites;
    private Map<Class, ModuleInstance> moduleForPublicObjects;

    private CompositeBuilderFactory compositeBuilderFactory;
    private ObjectBuilderFactory objectBuilderFactory;
    private EntitySessionFactory entitySessionFactory;
    private ServiceRegistry serviceRegistry;

    private ActivationStatus status = ActivationStatus.INACTIVE;
    private boolean available = false;
    private List<ActivationListener> activationListeners = new ArrayList<ActivationListener>();
    private Map<Class, ServiceInstanceProvider> providers = new HashMap<Class, ServiceInstanceProvider>();

    public ModuleInstance( ModuleContext moduleContext, Map<Class<? extends Composite>, ModuleInstance> moduleInstances, Map<Class, ModuleInstance> moduleForPublicObjects, ServiceRegistry layerRegistry )
    {
        this.moduleForPublicObjects = moduleForPublicObjects;
        this.moduleForPublicComposites = moduleInstances;
        this.moduleContext = moduleContext;

        compositeBuilderFactory = new ModuleCompositeBuilderFactory( this );
        objectBuilderFactory = new ModuleObjectBuilderFactory( this );
        entitySessionFactory = new EntitySessionFactoryImpl( this );

        this.serviceRegistry = new ModuleServiceRegistry( this, layerRegistry );

        injectServiceProvidersIntoObjectBuilders( moduleContext );
    }

    public ModuleContext getModuleContext()
    {
        return moduleContext;
    }

    public CompositeBuilderFactory getCompositeBuilderFactory()
    {
        return compositeBuilderFactory;
    }

    public ObjectBuilderFactory getObjectBuilderFactory()
    {
        return objectBuilderFactory;
    }

    public EntitySessionFactory getEntitySessionFactory()
    {
        return entitySessionFactory;
    }

    public ServiceRegistry getServiceRegistry()
    {
        return serviceRegistry;
    }

    public ServiceInstanceProvider getServiceProvider( Class type )
    {
        return providers.get( type );
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
            // Instantiate all service providers in this module
            Iterable<ServiceDescriptor> serviceDescriptors = getModuleContext().getModuleBinding().getModuleResolution().getModuleModel().getServiceDescriptors();
            for( ServiceDescriptor serviceDescriptor : serviceDescriptors )
            {
                Class<? extends ServiceInstanceProvider> providerType = serviceDescriptor.getServiceProvider();
                ObjectBuilder<? extends ServiceInstanceProvider> builder = getObjectBuilderFactory().newObjectBuilder( providerType );
                builder.adapt( serviceDescriptor );
                ServiceInstanceProvider sip = builder.newInstance();
                activationListeners.add( sip );
                Class serviceType = serviceDescriptor.getServiceType();
                registerServiceProvider( serviceType, sip );
            }


            try
            {
                setActivationStatus( ActivationStatus.STARTING );
                setActivationStatus( ActivationStatus.ACTIVE );
                available = true;
            }
            catch( Exception e )
            {
                setActivationStatus( ActivationStatus.STOPPING );
                setActivationStatus( ActivationStatus.INACTIVE );

                throw e;
            }
        }
    }

    private void registerServiceProvider( Class serviceType, ServiceInstanceProvider sip )
    {
        providers.put( serviceType, sip );

        Class[] extended = serviceType.getInterfaces();
        for( Class extendedType : extended )
        {
            registerServiceProvider( extendedType, sip );
        }
    }

    public void passivate() throws Exception
    {
        if( status == ActivationStatus.ACTIVE )
        {
            available = false;
            try
            {
                setActivationStatus( ActivationStatus.STOPPING );
            }
            catch( Exception e )
            {
                // Ignore
            }
            setActivationStatus( ActivationStatus.INACTIVE );

            for( ServiceInstanceProvider serviceInstanceProvider : providers.values() )
            {
                activationListeners.remove( serviceInstanceProvider );
            }
            providers.clear();
        }
    }

    public ActivationStatus getActivationStatus()
    {
        return status;
    }

    public boolean isAvailable()
    {
        return available;
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

    private void injectServiceProvidersIntoObjectBuilders( ModuleContext moduleContext )
    {
        // Inject service providers
/* TODO Fix this
        Map<Class, ServiceInstanceProvider> providers = moduleContext.getModuleBinding().getModuleResolution().getModuleModel().getServiceProviders();
        for( ServiceInstanceProvider serviceInstanceProvider : providers.values() )
        {
            Class serviceProviderType = serviceInstanceProvider.getClass();
            ObjectBuilder builder = objectBuilderFactory.newObjectBuilder( serviceProviderType );
            builder.inject( serviceInstanceProvider );
        }
*/
    }
}
