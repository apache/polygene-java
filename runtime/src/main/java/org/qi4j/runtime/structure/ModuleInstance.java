/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
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
import java.util.List;
import java.util.Stack;
import org.qi4j.composite.Composite;
import org.qi4j.composite.CompositeBuilder;
import org.qi4j.composite.CompositeBuilderFactory;
import org.qi4j.composite.NoSuchCompositeException;
import org.qi4j.entity.EntityComposite;
import org.qi4j.entity.UnitOfWork;
import org.qi4j.entity.UnitOfWorkFactory;
import org.qi4j.object.NoSuchObjectException;
import org.qi4j.object.ObjectBuilder;
import org.qi4j.object.ObjectBuilderFactory;
import org.qi4j.runtime.composite.CompositeModel;
import org.qi4j.runtime.entity.EntityModel;
import org.qi4j.runtime.entity.UnitOfWorkInstance;
import org.qi4j.runtime.object.ObjectModel;
import org.qi4j.service.Activatable;
import org.qi4j.service.ServiceFinder;
import org.qi4j.service.ServiceReference;
import org.qi4j.spi.composite.CompositeDescriptor;
import org.qi4j.structure.Module;
import org.qi4j.structure.Visibility;

/**
 * TODO
 */
public class ModuleInstance
    implements Module, Activatable
{
    private final ModuleModel moduleModel;
    private final LayerInstance layerInstance;
    private final CompositesInstance composites;
    private final EntitiesInstance entities;
    private final ObjectsInstance objects;
    private final ServicesInstance services;

    private final CompositeBuilderFactory compositeBuilderFactory;
    private final ObjectBuilderFactory objectBuilderFactory;
    private final UnitOfWorkFactory unitOfWorkFactory;
    private final ServiceFinder serviceFinder;
    private final ClassLoader classLoader;

    public ModuleInstance( ModuleModel moduleModel, LayerInstance layerInstance, CompositesModel compositesModel, EntitiesModel entitiesModel, ObjectsModel objectsModel, ServicesModel servicesModel )
    {
        this.moduleModel = moduleModel;
        this.layerInstance = layerInstance;
        composites = new CompositesInstance( compositesModel, this );
        entities = new EntitiesInstance( entitiesModel, this );
        objects = new ObjectsInstance( objectsModel, this );
        services = servicesModel.newInstance( this );

        compositeBuilderFactory = new CompositeBuilderFactoryInstance();
        objectBuilderFactory = new ObjectBuilderFactoryInstance();
        unitOfWorkFactory = new UnitOfWorkFactoryInstance();
        serviceFinder = new ServiceFinderInstance();
        classLoader = new ModuleClassLoader( Thread.currentThread().getContextClassLoader() );
    }


    public String name()
    {
        return moduleModel.name();
    }

    public ModuleModel model()
    {
        return moduleModel;
    }

    public LayerInstance layerInstance()
    {
        return layerInstance;
    }

    public CompositesInstance composites()
    {
        return composites;
    }

    public EntitiesInstance entities()
    {
        return entities;
    }

    public ObjectsInstance objects()
    {
        return objects;
    }

    public ServicesInstance services()
    {
        return services;
    }

    public CompositeBuilderFactory compositeBuilderFactory()
    {
        return compositeBuilderFactory;
    }

    public ObjectBuilderFactory objectBuilderFactory()
    {
        return objectBuilderFactory;
    }

    public UnitOfWorkFactory unitOfWorkFactory()
    {
        return unitOfWorkFactory;
    }

    public ServiceFinder serviceFinder()
    {
        return serviceFinder;
    }

    public ClassLoader classLoader()
    {
        return classLoader;
    }

    public ModuleInstance findModuleForComposite( Class mixinType )
    {
        // Check local first
        CompositeModel model = getCompositeModelFor( mixinType, Visibility.module );
        if( model != null )
        {
            return this;
        }

        // Check layer
        return layerInstance.findModuleForComposite( mixinType, Visibility.layer );
    }


    private CompositeModel getCompositeModelFor( Class mixinType, final Visibility visibility )
    {
        return composites.model().getCompositeModelFor( mixinType, visibility );
    }


    public ModuleInstance findModuleForEntity( Class mixinType )
    {
        // Check local first
        EntityModel model = entities.model().getEntityModelFor( mixinType, Visibility.module );

        if( model != null )
        {
            return this;
        }

        // Check layer
        return layerInstance.findModuleForEntity( mixinType, Visibility.layer );
    }

    public ModuleInstance findModuleForObject( Class type )
    {
        // Check local first
        ObjectModel model = objects().model().getObjectModelFor( type, Visibility.module );
        if( model != null )
        {
            return this;
        }

        // Check layer
        return layerInstance.findModuleForObject( type, Visibility.layer );
    }


    public void activate() throws Exception
    {
        services.activate();
    }

    public void passivate() throws Exception
    {
        services.passivate();
    }

    public CompositeDescriptor findCompositeFor( Class<? extends Composite> compositeType )
    {
        //TODO Cache this result
        ModuleInstance realModuleInstance = findModuleForComposite( compositeType );
        if( realModuleInstance == null )
        {
            return null;
        }
        CompositesInstance compositesInstance = realModuleInstance.composites();
        CompositesModel compositesModel = compositesInstance.model();
        return compositesModel.getCompositeModelFor( compositeType );
    }

    public EntityModel findEntityCompositeFor( Class<? extends EntityComposite> entityCompositeType )
    {
        //TODO Cache this result
        ModuleInstance realModuleInstance = findModuleForEntity( entityCompositeType );
        if( realModuleInstance == null )
        {
            return null;
        }
        EntitiesInstance entitiesInstance = realModuleInstance.entities();
        EntitiesModel entitiesModel = entitiesInstance.model();
        return entitiesModel.getEntityModelFor( entityCompositeType );
    }

    public Class findClassForName( String type )
    {
        Class clazz = getClassForName( type );

        if( clazz == null )
        {
            clazz = layerInstance.findClassForName( type );
        }

        return clazz;
    }

    @Override public String toString()
    {
        return moduleModel.toString();
    }

    Class getClassForName( String type )
    {
        Class clazz = composites.model().getClassForName( type );
        if( clazz == null )
        {
            clazz = entities.model().getClassForName( type );
        }
        if( clazz == null )
        {
            clazz = objects.model().getClassForName( type );
        }
        return clazz;
    }

    public <T> ServiceReference<T> findService( Class<T> serviceClass )
    {
        return serviceFinder().findService( serviceClass );
    }

    private class CompositeBuilderFactoryInstance
        implements CompositeBuilderFactory
    {
        public <T> CompositeBuilder<T> newCompositeBuilder( Class<T> mixinType )
            throws NoSuchCompositeException
        {
            ModuleInstance realModuleInstance = findModuleForComposite( mixinType );
            if( realModuleInstance == null )
            {
                throw new NoSuchCompositeException( mixinType.getName(), name() );
            }
            return realModuleInstance.composites().newCompositeBuilder( mixinType );
        }

        public <T> T newComposite( Class<T> compositeType )
            throws NoSuchCompositeException, org.qi4j.composite.InstantiationException
        {
            return newCompositeBuilder( compositeType ).newInstance();
        }
    }

    private class ObjectBuilderFactoryInstance
        implements ObjectBuilderFactory
    {
        public <T> ObjectBuilder<T> newObjectBuilder( Class<T> type )
            throws NoSuchObjectException
        {
            ModuleInstance realModuleInstance = findModuleForObject( type );
            if( realModuleInstance == null )
            {
                throw new NoSuchObjectException( type.getName(), name() );
            }
            return realModuleInstance.objects().newObjectBuilder( type );
        }

        public <T> T newObject( Class<T> type )
            throws NoSuchObjectException
        {
            return newObjectBuilder( type ).newInstance();
        }
    }

    private class UnitOfWorkFactoryInstance
        implements UnitOfWorkFactory
    {
        public UnitOfWorkFactoryInstance()
        {
        }

        public UnitOfWork newUnitOfWork()
        {
            return new UnitOfWorkInstance( ModuleInstance.this );
        }

        public UnitOfWork currentUnitOfWork()
        {
            Stack<UnitOfWork> stack = UnitOfWorkInstance.current.get();
            if( stack.size() == 0 )
            {
                return null;
            }
            return stack.peek();
        }
    }


    private class ServiceFinderInstance
        implements ServiceFinder
    {
        public <T> ServiceReference<T> findService( Class<T> serviceType )
        {
            List<ServiceReference<T>> serviceReferences = new ArrayList<ServiceReference<T>>();

            services.getServiceReferencesFor( serviceType, Visibility.module, serviceReferences );
            if( !serviceReferences.isEmpty() )
            {
                return serviceReferences.get( 0 );
            }

            layerInstance.getServiceReferencesFor( serviceType, Visibility.layer, serviceReferences );
            if( !serviceReferences.isEmpty() )
            {
                return serviceReferences.get( 0 );
            }

            UsedLayersInstance userLayers = layerInstance.usedLayersInstance();
            userLayers.getServiceReferencesFor( serviceType, serviceReferences );

            if( !serviceReferences.isEmpty() )
            {
                return serviceReferences.get( 0 );
            }

            return null; // TODO Throw exception?
        }

        public <T> Iterable<ServiceReference<T>> findServices( Class<T> serviceType )
        {
            List<ServiceReference<T>> serviceReferences = new ArrayList<ServiceReference<T>>();
            services.getServiceReferencesFor( serviceType, Visibility.module, serviceReferences );
            layerInstance.getServiceReferencesFor( serviceType, Visibility.layer, serviceReferences );

            UsedLayersInstance usedLayers = layerInstance.usedLayersInstance();
            usedLayers.getServiceReferencesFor( serviceType, serviceReferences );
            return serviceReferences;
        }
    }

    private class ModuleClassLoader
        extends ClassLoader
    {
        private ModuleClassLoader( ClassLoader classLoader )
        {
            super( classLoader );
        }

        @Override protected Class<?> findClass( String name ) throws ClassNotFoundException
        {
            Class clazz = findClassForName( name );

            if( clazz == null )
            {
                throw new ClassNotFoundException( name );
            }

            return clazz;
        }
    }

}
