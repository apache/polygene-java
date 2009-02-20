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

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import org.qi4j.api.common.ConstructionException;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.composite.AmbiguousTypeException;
import org.qi4j.api.composite.CompositeBuilder;
import org.qi4j.api.composite.CompositeBuilderFactory;
import org.qi4j.api.composite.NoSuchCompositeException;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.object.NoSuchObjectException;
import org.qi4j.api.object.ObjectBuilder;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.property.StateHolder;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceFinder;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.usecase.Usecase;
import org.qi4j.api.value.NoSuchValueException;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.qi4j.runtime.composite.CompositeModel;
import org.qi4j.runtime.composite.UsesInstance;
import org.qi4j.runtime.entity.EntityModel;
import org.qi4j.runtime.object.ObjectModel;
import org.qi4j.runtime.unitofwork.UnitOfWorkInstance;
import org.qi4j.runtime.value.ValueModel;
import org.qi4j.spi.composite.CompositeDescriptor;
import org.qi4j.spi.object.ObjectDescriptor;

/**
 * JAVADOC
 */
public class ModuleInstance
    implements Module, Activatable
{
    private final ModuleModel moduleModel;
    private final LayerInstance layerInstance;
    private final CompositesInstance composites;
    private final EntitiesInstance entities;
    private final ObjectsInstance objects;
    private final ValuesInstance values;
    private final ServicesInstance services;

    private final CompositeBuilderFactory compositeBuilderFactory;
    private final ObjectBuilderFactory objectBuilderFactory;
    private final ValueBuilderFactory valueBuilderFactory;
    private final UnitOfWorkFactory unitOfWorkFactory;
    private final ServiceFinder serviceFinder;
    private final ClassLoader classLoader;

    public ModuleInstance( ModuleModel moduleModel, LayerInstance layerInstance, CompositesModel compositesModel,
                           EntitiesModel entitiesModel, ObjectsModel objectsModel, ValuesModel valuesModel,
                           ServicesModel servicesModel )
    {
        this.moduleModel = moduleModel;
        this.layerInstance = layerInstance;
        composites = new CompositesInstance( compositesModel, this );
        entities = new EntitiesInstance( entitiesModel, this );
        objects = new ObjectsInstance( objectsModel, this );
        values = new ValuesInstance( valuesModel, this );
        services = servicesModel.newInstance( this );

        compositeBuilderFactory = new CompositeBuilderFactoryInstance();
        objectBuilderFactory = new ObjectBuilderFactoryInstance();
        valueBuilderFactory = new ValueBuilderFactoryInstance();
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

    public ValuesInstance values()
    {
        return values;
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

    public ValueBuilderFactory valueBuilderFactory()
    {
        return valueBuilderFactory;
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

    public boolean isEntity( Class mixinType )
    {
        return entities.model().getEntityModelFor( mixinType ) != null;
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

    public ModuleInstance findModuleForValue( Class valueType )
    {
        // Check local first
        ValueModel model = getValueModelFor( valueType, Visibility.module );
        if( model != null )
        {
            return this;
        }

        // Check layer
        return layerInstance.findModuleForValue( valueType, Visibility.layer );
    }

    private ValueModel getValueModelFor( Class valueType, final Visibility visibility )
    {
        return values.model().getValueModelFor( valueType, visibility );
    }


    public ModuleInstance findModuleForEntity( Class mixinType )
        throws AmbiguousTypeException
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
        throws AmbiguousTypeException
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


    public void activate()
        throws Exception
    {
        services.activate();
    }

    public void passivate()
        throws Exception
    {
        services.passivate();
    }

    public CompositeDescriptor findCompositeFor( Class mixinType )
    {
        //TODO Cache this result
        ModuleInstance realModuleInstance = findModuleForComposite( mixinType );
        if( realModuleInstance == null )
        {
            return null;
        }
        CompositesInstance compositesInstance = realModuleInstance.composites();
        CompositesModel compositesModel = compositesInstance.model();
        return compositesModel.getCompositeModelFor( mixinType );
    }

    public CompositeDescriptor findValueFor( Class valueType )
    {
        //TODO Cache this result
        ModuleInstance realModuleInstance = findModuleForValue( valueType );
        if( realModuleInstance == null )
        {
            return null;
        }
        ValuesInstance valuesInstance = realModuleInstance.values();
        ValuesModel valuesModel = valuesInstance.model();
        return valuesModel.getValueModelFor( valueType );
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

    public ObjectDescriptor findObjectFor( Class objectType )
    {
        //TODO Cache this result
        ModuleInstance realModuleInstance = findModuleForObject( objectType );
        if( realModuleInstance == null )
        {
            return null;
        }
        ObjectsInstance objectsInstance = realModuleInstance.objects();
        ObjectsModel objectsModel = objectsInstance.model();
        return objectsModel.getObjectModelFor( objectType );
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
            clazz = values.model().getClassForName( type );
        }
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

    public <T> ServiceReference<T> findService( Type serviceType )
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

    public <T> Iterable<ServiceReference<T>> findServices( Type serviceType )
    {
        List<ServiceReference<T>> serviceReferences = new ArrayList<ServiceReference<T>>();
        services.getServiceReferencesFor( serviceType, Visibility.module, serviceReferences );
        layerInstance.getServiceReferencesFor( serviceType, Visibility.layer, serviceReferences );

        return serviceReferences;
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

        public <T> T newComposite( Class<T> mixinType )
            throws NoSuchCompositeException, ConstructionException
        {
            ModuleInstance realModuleInstance = findModuleForComposite( mixinType );
            if( realModuleInstance == null )
            {
                throw new NoSuchCompositeException( mixinType.getName(), name() );
            }

            CompositeModel compositeModel = realModuleInstance.composites().model().getCompositeModelFor( mixinType );
            return compositeModel.newCompositeInstance( realModuleInstance, UsesInstance.NO_USES, compositeModel.newInitialState() ).<T>proxy();
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
            ModuleInstance realModuleInstance = findModuleForObject( type );
            if( realModuleInstance == null )
            {
                throw new NoSuchObjectException( type.getName(), name() );
            }
            ObjectModel objectModel = realModuleInstance.objects().model().getObjectModelFor( type );
            return type.cast( objectModel.newInstance( realModuleInstance, UsesInstance.NO_USES ) );
        }
    }

    private class ValueBuilderFactoryInstance
        implements ValueBuilderFactory
    {
        public <T> ValueBuilder<T> newValueBuilder( Class<T> valueType ) throws NoSuchValueException
        {
            ModuleInstance realModuleInstance = findModuleForValue( valueType );
            if( realModuleInstance == null )
            {
                throw new NoSuchValueException( valueType.getName(), name() );
            }
            return realModuleInstance.values().newValueBuilder( valueType );
        }

        public <T> T newValue( Class<T> valueType ) throws NoSuchValueException, ConstructionException
        {
            ModuleInstance realModuleInstance = findModuleForValue( valueType );
            if( realModuleInstance == null )
            {
                throw new NoSuchValueException( valueType.getName(), name() );
            }

            ValueModel valueModel = realModuleInstance.values().model().getValueModelFor( valueType );
            StateHolder initialState = valueModel.newInitialState();
            valueModel.checkConstraints( initialState, false );
            return valueModel.newValueInstance( realModuleInstance, initialState ).<T>proxy();
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
            return newUnitOfWork( Usecase.DEFAULT );
        }

        public UnitOfWork newUnitOfWork( Usecase usecase )
        {
            return new UnitOfWorkInstance( ModuleInstance.this, usecase );
        }

        public UnitOfWork nestedUnitOfWork()
        {
            return nestedUnitOfWork( Usecase.DEFAULT );
        }

        public UnitOfWork nestedUnitOfWork( Usecase usecase )
        {
            UnitOfWorkInstance current = currentUnitOfWork();
            if (current == null)
                return newUnitOfWork( usecase );
            else
                return new UnitOfWorkInstance(ModuleInstance.this, usecase, current.newEntityStore());
        }

        public UnitOfWorkInstance currentUnitOfWork()
        {
            Stack<UnitOfWorkInstance> stack = UnitOfWorkInstance.current.get();
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
            return ModuleInstance.this.findService( serviceType );
        }

        public <T> Iterable<ServiceReference<T>> findServices( Class<T> serviceType )
        {
            return ModuleInstance.this.findServices( serviceType );
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
