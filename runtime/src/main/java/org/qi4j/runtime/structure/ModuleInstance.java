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
import org.qi4j.composite.AmbiguousMixinTypeException;
import org.qi4j.composite.Composite;
import org.qi4j.composite.CompositeBuilder;
import org.qi4j.composite.CompositeBuilderFactory;
import org.qi4j.composite.MixinTypeNotAvailableException;
import org.qi4j.composite.ObjectBuilder;
import org.qi4j.composite.ObjectBuilderFactory;
import org.qi4j.entity.UnitOfWorkFactory;
import org.qi4j.property.ComputedPropertyInstance;
import org.qi4j.property.GenericPropertyInfo;
import org.qi4j.property.ImmutableProperty;
import org.qi4j.property.PropertyInfo;
import org.qi4j.runtime.entity.UnitOfWorkFactoryImpl;
import org.qi4j.runtime.service.ServiceReferenceInstance;
import org.qi4j.service.Activatable;
import org.qi4j.service.ServiceDescriptor;
import org.qi4j.service.ServiceFinder;
import org.qi4j.service.ServiceInstanceProvider;
import org.qi4j.service.ServiceReference;
import org.qi4j.spi.injection.StructureContext;
import org.qi4j.spi.structure.ModuleBinding;
import org.qi4j.spi.structure.ModuleModel;
import org.qi4j.spi.structure.ModuleResolution;
import org.qi4j.structure.Module;

/**
 * Instance of a Module.
 */
public final class ModuleInstance
    implements Activatable, ServiceFinder
{
    static final ModuleInstance DUMMY = new ModuleInstance();

    private static final PropertyInfo NAME_INFO;

    static
    {
        try
        {
            NAME_INFO = new GenericPropertyInfo( Module.class.getMethod( "name" ) );
        }
        catch( NoSuchMethodException e )
        {
            throw new InternalError( "Qi4j Core Runtime codebase is corrupted. Contact Qi4j team: Module" );
        }
    }

    private ModuleContext moduleContext;
    private Module module;

    private Map<Class<? extends Composite>, ModuleInstance> moduleForPublicComposites;
    private Map<Class, ModuleInstance> moduleForPublicObjects;
    private Map<Class, ModuleInstance> moduleForPublicMixinTypes;

    private CompositeBuilderFactory compositeBuilderFactory;
    private ObjectBuilderFactory objectBuilderFactory;
    private UnitOfWorkFactory unitOfWorkFactory;
    private ServiceFinder serviceLocator;
    private StructureContext structureContext;

    private ActivationStatus status = ActivationStatus.INACTIVE;
    private List<ActivationListener> activationListeners = new ArrayList<ActivationListener>();

    // List of active Services in this Module
    private List<ServiceReferenceInstance> serviceInstances = new ArrayList<ServiceReferenceInstance>();

    // For each type there may be zero, one or many active instances
    private Map<Class<?>, List<ServiceReferenceInstance>> serviceReferences = new HashMap<Class<?>, List<ServiceReferenceInstance>>();


    private ModuleInstance()
    {
        // Support for the DUMMY
    }

    public ModuleInstance( ModuleContext moduleContext,
                           Map<Class<? extends Composite>, ModuleInstance> moduleInstances,
                           Map<Class, ModuleInstance> moduleForPublicObjects,
                           Map<Class, ModuleInstance> moduleForPublicMixinTypes,
                           ServiceFinder layerServiceLocator )
    {
        this.moduleForPublicObjects = moduleForPublicObjects;
        this.moduleForPublicComposites = moduleInstances;
        this.moduleContext = moduleContext;
        this.moduleForPublicMixinTypes = moduleForPublicMixinTypes;
        this.module = new ModuleDelegate();

        compositeBuilderFactory = new ModuleCompositeBuilderFactory( this );
        objectBuilderFactory = new ModuleObjectBuilderFactory( this );
        unitOfWorkFactory = new UnitOfWorkFactoryImpl( this );
        serviceLocator = new ModuleServiceLocator( this, layerServiceLocator );

        structureContext = new StructureContext( compositeBuilderFactory, objectBuilderFactory, unitOfWorkFactory, serviceLocator );

        // Create service instances
        Iterable<ServiceDescriptor> serviceDescriptors = getModuleContext().getModuleBinding().getModuleResolution().getModuleModel().getServiceDescriptors();
        for( ServiceDescriptor serviceDescriptor : serviceDescriptors )
        {
            Class<? extends ServiceInstanceProvider> providerType = serviceDescriptor.serviceProvider();
            ServiceInstanceProvider sip;
            if( Composite.class.isAssignableFrom( providerType ) )
            {
                CompositeBuilder<? extends ServiceInstanceProvider> builder = compositeBuilderFactory.newCompositeBuilder( providerType );
                sip = builder.newInstance();
            }
            else
            {
                ObjectBuilder<? extends ServiceInstanceProvider> builder = objectBuilderFactory.newObjectBuilder( providerType );
                sip = builder.newInstance();
            }
            Class serviceType = serviceDescriptor.serviceType();
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
    }

    public ModuleContext getModuleContext()
    {
        return moduleContext;
    }

    public StructureContext getStructureContext()
    {
        return structureContext;
    }

    public Module getModule()
    {
        return module;
    }

    /**
     * Lookup a service for a particular type. Only look in the local Module; do not try
     * and delegate to the Layer.
     *
     * @param serviceType the type of the service to lookup
     * @return a service reference to the found service, or null if no service matching the type was found
     */
    public <T> ServiceReference<T> findService( Class<T> serviceType )
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

    public <T> Iterable<ServiceReference<T>> findServices( Class<T> serviceType )
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

    // Activatable implementation
    public void activate() throws Exception
    {
        if( status == ActivationStatus.INACTIVE )
        {
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
        }
    }

    // Module implementation
    public ImmutableProperty<String> name()
    {
        return new ComputedPropertyInstance<String>( NAME_INFO )
        {
            public String get()
            {
                return moduleContext.getModuleBinding().getModuleResolution().getModuleModel().getName();
            }
        };
    }

    public ModuleInstance moduleForMixinType( Class<?> mixinType )
    {
        ModuleInstance module = moduleForPublicMixinTypes.get( mixinType );
        if( module == null )
        {
            return this;
        }
        if( module == DUMMY )
        {
            return null;
        }
        return module;
    }

    public ModuleInstance moduleForComposite( Class<? extends Composite> compositeType )
    {
        ModuleInstance realInstance = getModuleForPublicComposite( compositeType );
        if( realInstance == null )
        {
            realInstance = this;
        }
        return realInstance;
    }

    public ModuleInstance moduleForObject( Class<?> objectType )
    {
        ModuleInstance realInstance = getModuleForPublicObject( objectType );
        if( realInstance == null )
        {
            realInstance = this;
        }
        return realInstance;
    }

    public Class<? extends Composite> lookupCompositeType( Class<?> mixinType )
    {
        Class<? extends Composite> compositeType;
        if( !Composite.class.isAssignableFrom( mixinType ) )
        {
            Class<? extends Composite> compositeType1;
            ModuleInstance module = moduleForMixinType( mixinType );
            if( module == null )
            {
            }
            ModuleContext moduleContext = module.getModuleContext();
            compositeType1 = moduleContext.getCompositeForMixinType( mixinType );
            if( compositeType1 == Composite.class )
            {
                // conflict detected earlier.
                throw new AmbiguousMixinTypeException( mixinType );
            }
            if( compositeType1 == null )
            {
                ModuleBinding moduleBinding = moduleContext.getModuleBinding();
                ModuleResolution moduleResolution = moduleBinding.getModuleResolution();
                ModuleModel moduleModel = moduleResolution.getModuleModel();
                String moduleModelName = moduleModel.getName();

                throw new MixinTypeNotAvailableException( mixinType, moduleModelName );
            }
            compositeType = compositeType1;
        }
        else
        {
            compositeType = (Class<? extends Composite>) mixinType;
        }
        return compositeType;
    }

    public boolean isPublic( Class<?> compositeOrObject )
    {
        return false;
    }

    public Class lookupClass( String className )
        throws ClassNotFoundException
    {
        Class aClass = moduleContext.getModuleBinding().lookupClass( className );
        if( aClass == null )
        {
            throw new ClassNotFoundException( className );
        }
        return aClass;
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

    @Override public String toString()
    {
        return moduleContext.toString();
    }

    public final class ModuleDelegate
        implements Module
    {
        public ModuleInstance getModuleInstance()
        {
            return ModuleInstance.this;
        }

        public ImmutableProperty<String> name()
        {
            return ModuleInstance.this.name();
        }

        public Module findModuleForComposite( Class<? extends Composite> compositetype )
        {
            return ModuleInstance.this.moduleForComposite( compositetype ).getModule();
        }

        public Module findModuleForMixinType( Class<?> mixintype )
        {
            return ModuleInstance.this.moduleForMixinType( mixintype ).getModule();
        }

        public Module findModuleForObject( Class<?> objecttype )
        {
            return ModuleInstance.this.moduleForObject( objecttype ).getModule();
        }

        public boolean isPublic( Class<?> compositeOrObject )
        {
            return ModuleInstance.this.isPublic( compositeOrObject );
        }

        public Class<? extends Composite> findCompositeType( Class<?> mixintype )
        {
            return ModuleInstance.this.lookupCompositeType( mixintype );
        }

        public Class findClass( String className )
            throws ClassNotFoundException
        {
            return ModuleInstance.this.lookupClass( className );
        }

        public <T> ServiceReference<T> findService( Class<T> serviceType )
        {
            return serviceLocator.findService( serviceType );
        }

        public <T> Iterable<ServiceReference<T>> findServices( Class<T> serviceType )
        {
            return serviceLocator.findServices( serviceType );
        }
    }
}
