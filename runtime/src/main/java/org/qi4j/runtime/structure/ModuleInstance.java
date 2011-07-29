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

import org.json.JSONException;
import org.json.JSONTokener;
import org.qi4j.api.common.ConstructionException;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.composite.*;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.EntityDescriptor;
import org.qi4j.api.event.ActivationEvent;
import org.qi4j.api.event.ActivationEventListener;
import org.qi4j.api.json.JSONDeserializer;
import org.qi4j.api.object.NoSuchObjectException;
import org.qi4j.api.object.ObjectBuilder;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.object.ObjectDescriptor;
import org.qi4j.api.property.Property;
import org.qi4j.api.property.PropertyDescriptor;
import org.qi4j.api.property.StateHolder;
import org.qi4j.api.query.QueryBuilderFactory;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceFinder;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.usecase.Usecase;
import org.qi4j.api.util.Classes;
import org.qi4j.api.util.NullArgumentException;
import org.qi4j.api.value.*;
import org.qi4j.functional.Function;
import org.qi4j.functional.Iterables;
import org.qi4j.functional.Specification;
import org.qi4j.functional.Specifications;
import org.qi4j.runtime.composite.*;
import org.qi4j.runtime.entity.EntitiesInstance;
import org.qi4j.runtime.entity.EntitiesModel;
import org.qi4j.runtime.entity.EntityInstance;
import org.qi4j.runtime.entity.EntityModel;
import org.qi4j.runtime.injection.InjectionContext;
import org.qi4j.runtime.object.ObjectBuilderInstance;
import org.qi4j.runtime.object.ObjectModel;
import org.qi4j.runtime.object.ObjectsInstance;
import org.qi4j.runtime.object.ObjectsModel;
import org.qi4j.runtime.query.QueryBuilderFactoryImpl;
import org.qi4j.runtime.service.ImportedServicesInstance;
import org.qi4j.runtime.service.ImportedServicesModel;
import org.qi4j.runtime.service.ServicesInstance;
import org.qi4j.runtime.service.ServicesModel;
import org.qi4j.runtime.unitofwork.UnitOfWorkInstance;
import org.qi4j.runtime.value.*;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.qi4j.functional.Iterables.*;

/**
 * Instance of a Qi4j Module. Contains the various composites for this Module.
 */
public class ModuleInstance
        implements Module, Activatable
{
    private final ModuleModel moduleModel;
    private final LayerInstance layerInstance;
    private final TransientsInstance transients;
    private final ValuesInstance values;
    private final ObjectsInstance objects;
    private final EntitiesInstance entities;
    private final ServicesInstance services;
    private final ImportedServicesInstance importedServices;

    private final TransientBuilderFactory transientBuilderFactory;
    private final ObjectBuilderFactory objectBuilderFactory;
    private final ValueBuilderFactory valueBuilderFactory;
    private final UnitOfWorkFactory unitOfWorkFactory;
    private final QueryBuilderFactory queryBuilderFactory;
    private final ServiceFinder serviceFinder;
    private final ClassLoader classLoader;
    private final ActivationEventListenerSupport eventListenerSupport = new ActivationEventListenerSupport();

    // Lookup caches
    private final Map<Class, ModelModule<ObjectModel>> objectModels;
    private final Map<Class, ModelModule<TransientModel>> transientModels;
    private final Map<Class, Iterable<ModelModule<EntityModel>>> entityModels;
    private final Map<Class, ModelModule<ValueModel>> valueModels;

    public ModuleInstance( ModuleModel moduleModel, LayerInstance layerInstance, TransientsModel transientsModel,
                           EntitiesModel entitiesModel, ObjectsModel objectsModel, ValuesModel valuesModel,
                           ServicesModel servicesModel, ImportedServicesModel importedServicesModel
    )
    {
        this.moduleModel = moduleModel;
        this.layerInstance = layerInstance;
        transients = new TransientsInstance( transientsModel, this );
        values = new ValuesInstance( valuesModel, this );
        objects = new ObjectsInstance( objectsModel, this );
        entities = new EntitiesInstance( entitiesModel, this );
        services = servicesModel.newInstance( this );
        importedServices = importedServicesModel.newInstance( this );

        transientBuilderFactory = new TransientBuilderFactoryInstance();
        objectBuilderFactory = new ObjectBuilderFactoryInstance();
        valueBuilderFactory = new ValueBuilderFactoryInstance();
        unitOfWorkFactory = new UnitOfWorkFactoryInstance();
        serviceFinder = new ServiceFinderInstance();
        queryBuilderFactory = new QueryBuilderFactoryImpl( serviceFinder );

        objectModels = new ConcurrentHashMap<Class, ModelModule<ObjectModel>>();
        transientModels = new ConcurrentHashMap<Class, ModelModule<TransientModel>>();
        entityModels = new ConcurrentHashMap<Class, Iterable<ModelModule<EntityModel>>>();
        valueModels = new ConcurrentHashMap<Class, ModelModule<ValueModel>>();

        this.classLoader = new ModuleClassLoader( Thread.currentThread().getContextClassLoader() );

        services.registerActivationEventListener( eventListenerSupport );
    }

    public String name()
    {
        return moduleModel.name();
    }

    public <T> T metaInfo( Class<T> infoType )
    {
        return moduleModel.metaInfo( infoType );
    }

    public ModuleModel model()
    {
        return moduleModel;
    }

    public LayerInstance layerInstance()
    {
        return layerInstance;
    }

    public EntitiesInstance entities()
    {
        return entities;
    }

    public ServicesInstance services()
    {
        return services;
    }

    public ImportedServicesInstance importedServices()
    {
        return importedServices;
    }

    public EntityDescriptor entityDescriptor( String name )
    {
        try
        {
            Class<?> type = classLoader().loadClass( name );
            Iterable<ModelModule<EntityModel>> entityModels = findEntityModels( type );
            return first( map( ModelModule.<EntityModel>modelFunction(), entityModels ) );
        } catch( ClassNotFoundException e )
        {
            return null;
        }
    }

    public ObjectDescriptor objectDescriptor( String typeName )
    {
        try
        {
            Class<?> type = classLoader().loadClass( typeName );
            return findObjectModels( type ).model();
        } catch( ClassNotFoundException e )
        {
            return null;
        }
    }

    public TransientDescriptor transientDescriptor( String name )
    {
        try
        {
            Class<?> type = classLoader().loadClass( name );
            return findTransientModels( type ).model();
        } catch( ClassNotFoundException e )
        {
            return null;
        }
    }

    public ValueDescriptor valueDescriptor( String name )
    {
        try
        {
            Class<?> type = classLoader().loadClass( name );
            return findValueModels( type ).model();
        } catch( ClassNotFoundException e )
        {
            return null;
        }
    }

    public TransientBuilderFactory transientBuilderFactory()
    {
        return transientBuilderFactory;
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

    public QueryBuilderFactory queryBuilderFactory()
    {
        return queryBuilderFactory;
    }

    public ServiceFinder serviceFinder()
    {
        return serviceFinder;
    }

    public ClassLoader classLoader()
    {
        return classLoader;
    }

    @Override
    public void registerActivationEventListener( ActivationEventListener listener )
    {
        eventListenerSupport.registerActivationEventListener( listener );
    }

    @Override
    public void deregisterActivationEventListener( ActivationEventListener listener )
    {
        eventListenerSupport.deregisterActivationEventListener( listener );
    }

    public void activate()
            throws Exception
    {
        eventListenerSupport.fireEvent( new ActivationEvent( this, ActivationEvent.EventType.ACTIVATING ) );
        services.activate();
        eventListenerSupport.fireEvent( new ActivationEvent( this, ActivationEvent.EventType.ACTIVATED ) );
    }

    public void passivate()
            throws Exception
    {
        eventListenerSupport.fireEvent( new ActivationEvent( this, ActivationEvent.EventType.PASSIVATING ) );
        services.passivate();
        eventListenerSupport.fireEvent( new ActivationEvent( this, ActivationEvent.EventType.PASSIVATED ) );
    }

    @Override
    public String toString()
    {
        return moduleModel.toString();
    }

    public Iterable<ModelModule<EntityModel>> findEntityModels( final Class type )
    {
        Iterable<ModelModule<EntityModel>> models = entityModels.get( type );

        if( models == null )
        {
            Specification<EntityModel> hasRole = new Specification<EntityModel>()
            {
                @Override
                public boolean satisfiedBy( EntityModel item )
                {
                    return item.hasRole( type );
                }
            };

            LinkedHashSet<ModelModule<EntityModel>> result = new LinkedHashSet<ModelModule<EntityModel>>(  );
            Iterables.addAll( result, ambiguousCheck( type, findModels( exactTypeSpecification( type ), visibleEntities(Visibility.module), layerInstance().visibleEntities(Visibility.layer), layerInstance().visibleEntities(Visibility.application), layerInstance().usedLayersInstance().visibleEntities() ) ) );
            Iterables.addAll( result, findModels( hasRole, visibleEntities( Visibility.module ), layerInstance().visibleEntities( Visibility.layer ), layerInstance().visibleEntities( Visibility.application ), layerInstance().usedLayersInstance().visibleEntities() ) );

            models = result;

            entityModels.put( type, models );
        }

        return models;
    }

    public ModelModule<TransientModel> findTransientModels( final Class type )
    {
        ModelModule<TransientModel> model = transientModels.get( type );

        if( model == null )
        {
            Iterable<ModelModule<TransientModel>> flatten = flatten( ambiguousCheck( type, findModels( exactTypeSpecification( type ), visibleTransients( Visibility.module ), layerInstance().visibleTransients( Visibility.layer ), layerInstance().visibleTransients( Visibility.application ), layerInstance().usedLayersInstance().visibleTransients() ) ),
                    ambiguousCheck( type, findModels( assignableTypeSpecification( type ), visibleTransients( Visibility.module ), layerInstance().visibleTransients( Visibility.layer ), layerInstance().visibleTransients( Visibility.application ), layerInstance().usedLayersInstance().visibleTransients() ) ) );
            model = Iterables.first( flatten );

            if (model != null)
                transientModels.put( type, model );
        }

        return model;
    }

    public ModelModule<ObjectModel> findObjectModels( final Class type )
    {
        ModelModule<ObjectModel> model = objectModels.get( type );

        if( model == null )
        {
            Iterable<ModelModule<ObjectModel>> flatten = Iterables.flatten( ambiguousCheck( type, findModels( exactTypeSpecification( type ), visibleObjects( Visibility.module ), layerInstance().visibleObjects( Visibility.layer ), layerInstance().visibleObjects(Visibility.application), layerInstance().usedLayersInstance().visibleObjects() ) ),
                    ambiguousCheck( type, findModels( assignableTypeSpecification( type ), visibleObjects( Visibility.module ), layerInstance().visibleObjects( Visibility.layer ), layerInstance().visibleObjects(Visibility.application), layerInstance().usedLayersInstance().visibleObjects() ) ) );

            model = Iterables.first( flatten );

            if (model != null)
                objectModels.put( type, model );
        }

        return model;
    }

    public ModelModule<ValueModel> findValueModels( final Class type )
    {
        ModelModule<ValueModel> model = valueModels.get( type );

        if( model == null )
        {
            Iterable<ModelModule<ValueModel>> flatten =  Iterables.flatten( ambiguousCheck( type,
                                            findModels( exactTypeSpecification( type ),
                                                    visibleValues( Visibility.module ),
                                                    layerInstance().visibleValues( Visibility.layer ),
                                                    layerInstance().visibleValues(Visibility.application),
                                                    layerInstance().usedLayersInstance().visibleValues() )),
                                        ambiguousCheck( type,
                                            findModels( assignableTypeSpecification( type ),
                                                    visibleValues( Visibility.module ),
                                                    layerInstance().visibleValues( Visibility.layer ),
                                                    layerInstance().visibleValues(Visibility.application),
                                                    layerInstance().usedLayersInstance().visibleValues() ) ) );

            model = Iterables.first( flatten );

            if (model != null)
                valueModels.put( type, model );
        }

        return model;
    }

    private <T extends ModelDescriptor> Iterable<ModelModule<T>> findModels( Specification<? super T> specification,
                                                                              Iterable<ModelModule<T>>... models )
    {
        Specification<ModelModule<T>> spec = Specifications.translate( ModelModule.<T>modelFunction(), specification );
        return Iterables.filter( spec, Iterables.flattenIterables( Iterables.iterable( models ) ) );
    }

    Iterable<ModelModule<ObjectModel>> visibleObjects(Visibility visibility)
    {
        return objects.visibleObjects( visibility );
    }

    Iterable<ModelModule<TransientModel>> visibleTransients(Visibility visibility)
    {
        return transients.visibleTransients( visibility );
    }

    Iterable<ModelModule<EntityModel>> visibleEntities(Visibility visibility)
    {
        return entities.visibleEntities( visibility );
    }

    Iterable<ModelModule<ValueModel>> visibleValues(Visibility visibility)
    {
        return values.visibleValues( visibility );
    }

    Iterable<ServiceReference> visibleServices(Visibility visibility)
    {
        return Iterables.flatten( services.visibleServices( visibility ),
                importedServices.visibleServices( visibility ) );
    }

    private class TransientBuilderFactoryInstance
            implements TransientBuilderFactory
    {
        public <T> TransientBuilder<T> newTransientBuilder( Class<T> mixinType )
                throws NoSuchCompositeException
        {
            NullArgumentException.validateNotNull( "mixinType", mixinType );

            ModelModule<TransientModel> model = findTransientModels( mixinType );

            if( model == null )
            {
                throw new NoSuchCompositeException( mixinType.getName(), name() );
            }

            return new TransientBuilderInstance<T>( model );
        }

        public <T> T newTransient( final Class<T> mixinType )
                throws NoSuchCompositeException, ConstructionException
        {
            NullArgumentException.validateNotNull( "mixinType", mixinType );

            ModelModule<TransientModel> model = findTransientModels( mixinType );

            if( model == null )
            {
                throw new NoSuchCompositeException( mixinType.getName(), name() );
            }

            StateHolder stateHolder = model.model().newInitialState(model.module());
            model.model().state().checkConstraints( stateHolder );
            return model.model().newCompositeInstance( model.module(), UsesInstance.EMPTY_USES, stateHolder ).<T>proxy();
        }
    }

    private class ObjectBuilderFactoryInstance
            implements ObjectBuilderFactory
    {
        public <T> ObjectBuilder<T> newObjectBuilder( Class<T> mixinType )
                throws NoSuchObjectException
        {
            NullArgumentException.validateNotNull( "mixinType", mixinType );
            ModelModule<ObjectModel> model = findObjectModels( mixinType );

            if( model == null )
            {
                throw new NoSuchObjectException( mixinType.getName(), name() );
            }
            InjectionContext injectionContext = new InjectionContext( model.module(), UsesInstance.EMPTY_USES );
            return new ObjectBuilderInstance<T>( injectionContext, model.model() );
        }

        public <T> T newObject( Class<T> mixinType )
                throws NoSuchObjectException
        {
            NullArgumentException.validateNotNull( "mixinType", mixinType );
            ModelModule<ObjectModel> model = findObjectModels( mixinType );

            if( model == null )
            {
                throw new NoSuchObjectException( mixinType.getName(), name() );
            }

            InjectionContext injectionContext = new InjectionContext( model.module(), UsesInstance.EMPTY_USES );
            return mixinType.cast( model.model().newInstance( injectionContext ) );
        }
    }

    private class ValueBuilderFactoryInstance
            implements ValueBuilderFactory
    {

        public <T> T newValue( Class<T> mixinType )
                throws NoSuchValueException, ConstructionException
        {
            NullArgumentException.validateNotNull( "mixinType", mixinType );
            ModelModule<ValueModel> model = findValueModels( mixinType );

            if( model == null )
            {
                throw new NoSuchValueException( mixinType.getName(), name() );
            }

            StateHolder initialState = model.model().newInitialState(model.module());
            model.model().checkConstraints( initialState );
            return mixinType.cast( model.model().newValueInstance( model.module(), initialState ).proxy() );
        }

        public <T> ValueBuilder<T> newValueBuilder( Class<T> mixinType )
                throws NoSuchValueException
        {
            NullArgumentException.validateNotNull( "mixinType", mixinType );
            ModelModule<ValueModel> model = findValueModels( mixinType );

            if( model == null )
            {
                throw new NoSuchValueException( mixinType.getName(), name() );
            }

            return new ValueBuilderInstance<T>( model, model.model().newBuilderState( model.module() ) );
        }

        @Override
        public <T> ValueBuilder<T> newValueBuilderWithPrototype( T prototype )
        {
            NullArgumentException.validateNotNull( "prototype", prototype );

            final ValueInstance valueInstance = ValueInstance.getValueInstance( (ValueComposite) prototype );
            Class<Composite> valueType = (Class<Composite>) valueInstance.type();

            ModelModule<ValueModel> model = findValueModels( valueType );

            if( model == null )
            {
                throw new NoSuchValueException( valueType.getName(), name() );
            }

            Function<PropertyDescriptor, Object> state = new Function<PropertyDescriptor, Object>()
            {
                @Override
                public Object map( PropertyDescriptor propertyDescriptor )
                {
                    Property<?> property = valueInstance.state().propertyFor( propertyDescriptor.accessor() );
                    return property == null ? null : property.get();
                }
            };

            return new ValueBuilderInstance<T>( model, model.model().newBuilderState( state ) );
        }

        @Override
        public <T> ValueBuilder<T> newValueBuilderWithState( Class<T> mixinType, Function<PropertyDescriptor, Object> stateFunction )
        {
            NullArgumentException.validateNotNull( "prototype", stateFunction );

            ModelModule<ValueModel> model = findValueModels( mixinType );

            if( model == null )
            {
                throw new NoSuchValueException( mixinType.getName(), name() );
            }

            return new ValueBuilderInstance<T>( model, model.model().newBuilderState( stateFunction ) );
        }

        public <T> T newValueFromJSON( Class<T> mixinType, String jsonValue )
                throws NoSuchValueException, ConstructionException
        {
            NullArgumentException.validateNotNull( "mixinType", mixinType );
            ModelModule<ValueModel> model = findValueModels( mixinType );

            if( model == null )
            {
                throw new NoSuchValueException( mixinType.getName(), name() );
            }

            try
            {
                return (T) new JSONDeserializer( model.module() ).deserialize( new JSONTokener( jsonValue ).nextValue(), model.model().valueType() );
            } catch( JSONException e )
            {
                throw new ConstructionException( "Could not create value from JSON", e );
            }
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

        public UnitOfWork newUnitOfWork( long currentTime )
        {
            return newUnitOfWork( Usecase.DEFAULT, currentTime );
        }

        public UnitOfWork newUnitOfWork( Usecase usecase )
        {
            return newUnitOfWork( usecase, System.currentTimeMillis() );
        }

        @Override
        public UnitOfWork newUnitOfWork( Usecase usecase, long currentTime )
        {
            return new ModuleUnitOfWork( ModuleInstance.this, new UnitOfWorkInstance( usecase, currentTime ) );
        }

        public UnitOfWork currentUnitOfWork()
        {
            Stack<UnitOfWorkInstance> stack = UnitOfWorkInstance.getCurrent();
            if( stack.size() == 0 )
            {
                return null;
            }
            return new ModuleUnitOfWork( ModuleInstance.this, stack.peek() );
        }

        public UnitOfWork getUnitOfWork( EntityComposite entity )
        {
            EntityInstance instance = EntityInstance.getEntityInstance( entity );
            return instance.unitOfWork();
        }
    }

    private class ServiceFinderInstance
            implements ServiceFinder
    {
        Map<Type, ServiceReference> service = new ConcurrentHashMap<Type, ServiceReference>();
        Map<Type, Iterable<ServiceReference>> services = new ConcurrentHashMap<Type, Iterable<ServiceReference>>();

        public <T> ServiceReference<T> findService( final Class<T> serviceType )
        {
            ServiceReference serviceReference = service.get( serviceType );
            if( serviceReference == null )
            {
                serviceReference = Iterables.first( findServices( serviceType ));
                if( serviceReference != null )
                {
                    service.put( serviceType, serviceReference );
                }
            }

            return serviceReference;
        }

        @Override
        public <T> ServiceReference<T> findService( Type serviceType )
        {
            ServiceReference serviceReference = service.get( serviceType );
            if( serviceReference == null )
            {
                serviceReference = Iterables.first( findServices( serviceType ));
                if( serviceReference != null )
                {
                    service.put( serviceType, serviceReference );
                }
            }

            return serviceReference;
        }

        @Override
        public <T> Iterable<ServiceReference<T>> findServices( Class<T> serviceType )
        {
            return findServices( (Type)serviceType );
        }

        public <T> Iterable<ServiceReference<T>> findServices( final Type serviceType )
        {
            Iterable<ServiceReference> iterable = services.get( serviceType );
            if( iterable == null )
            {
                Specification<Class> typeSpecification = new Specification<Class>()
                {
                    @Override
                    public boolean satisfiedBy( Class item )
                    {
                        if (serviceType instanceof Class)
                        {
                            // Straight class assignability check
                            return ((Class)serviceType).isAssignableFrom( item );
                        } else if (serviceType instanceof ParameterizedType)
                        {
                            // Foo<Bar> check
                            // First check Foo
                            ParameterizedType parameterizedType = (ParameterizedType) serviceType;
                            if (!((Class) parameterizedType.getRawType()).isAssignableFrom( item ))
                                return false;

                            // Then check Bar
                            for( Type intf : Classes.INTERFACES_OF.map( item ) )
                            {
                                if (intf.equals( serviceType ))
                                    return true;
                            }

                            // All parameters are the same - ok!
                            return false;
                        } else
                            return false;
                    }
                };

                Specification<ServiceReference> referenceTypeCheck = Specifications.translate( new Function<ServiceReference, Class>()
                {
                    @Override
                    public Class map( ServiceReference serviceReference )
                    {
                        return serviceReference.type();
                    }
                }, typeSpecification);

                Iterable<ServiceReference> matchingServices = Iterables.flatten(
                        Iterables.filter( referenceTypeCheck, visibleServices(Visibility.module)),
                        Iterables.filter( referenceTypeCheck, layerInstance.visibleServices( Visibility.layer ) ),
                        Iterables.filter( referenceTypeCheck, layerInstance.visibleServices( Visibility.application ) ),
                        Iterables.filter( referenceTypeCheck, layerInstance.usedLayersInstance().visibleServices() ));

                iterable = Iterables.toList( matchingServices );
                services.put( serviceType, iterable );
            }

            return Iterables.cast( iterable);
        }
    }

    private class ModuleClassLoader
            extends ClassLoader
    {
        Map<String, Class> classes = new ConcurrentHashMap<String, Class>();

        private ModuleClassLoader( ClassLoader classLoader )
        {
            super( classLoader );
        }

        @Override
        protected Class<?> findClass( String name )
                throws ClassNotFoundException
        {
            Class clazz = classes.get( name );
            if( clazz == null )
            {
                Specification<ModelDescriptor> modelTypeSpecification = modelTypeSpecification( name );
                Specification<ModelModule<ModelDescriptor>> translate = Specifications.translate( ModelModule.modelFunction(), modelTypeSpecification );

                // Check module
                {
                    Iterable<ModelModule<ModelDescriptor>> i = cast( flatten( cast( visibleObjects(Visibility.module) ),
                            cast( visibleEntities(Visibility.module) ),
                            cast( visibleTransients(Visibility.module) ),
                            cast( visibleValues(Visibility.module) ) ) );

                    Iterable<ModelModule<ModelDescriptor>> moduleModels = filter( translate, i );
                    Iterator<ModelModule<ModelDescriptor>> iter = moduleModels.iterator();
                    if( iter.hasNext() )
                    {
                        clazz = iter.next().model().type();

                        if( iter.hasNext() )
                        {
                            // Ambiguous exception
                            throw new ClassNotFoundException( name, new AmbiguousTypeException( "More than one model matches the classname "+name+":"+Iterables.toList( moduleModels )) );
                        }
                    }
                }

                // Check layer
                if( clazz == null )
                {
                    Iterable<ModelModule<ModelDescriptor>> flatten = cast( flatten(
                            cast( layerInstance().visibleObjects( Visibility.layer ) ),
                            cast( layerInstance().visibleTransients( Visibility.layer ) ),
                            cast( layerInstance().visibleEntities( Visibility.layer ) ),
                            cast( layerInstance().visibleValues( Visibility.layer ) ),
                            cast( layerInstance().visibleObjects( Visibility.application ) ),
                            cast( layerInstance().visibleTransients( Visibility.application ) ),
                            cast( layerInstance().visibleEntities( Visibility.application ) ),
                            cast( layerInstance().visibleValues( Visibility.application ) ) ) );
                    Iterable<ModelModule<ModelDescriptor>> layerModels = filter( translate, flatten );
                    Iterator<ModelModule<ModelDescriptor>> iter = layerModels.iterator();
                    if( iter.hasNext() )
                    {
                        clazz = iter.next().model().type();

                        if( iter.hasNext() )
                        {
                            // Ambiguous exception
                            throw new ClassNotFoundException( name, new AmbiguousTypeException( "More than one model matches the classname "+name+":"+Iterables.toList( layerModels )) );
                        }
                    }
                }

                // Check used layers
                if( clazz == null )
                {
                    Iterable<ModelModule<ModelDescriptor>> flatten = cast( flatten(
                            cast( layerInstance().usedLayersInstance().visibleObjects() ),
                            cast( layerInstance().usedLayersInstance().visibleTransients() ),
                            cast( layerInstance().usedLayersInstance().visibleEntities() ),
                            cast( layerInstance().usedLayersInstance().visibleValues() ) ) );
                    Iterable<ModelModule<ModelDescriptor>> usedLayersModels = filter( translate, flatten );
                    Iterator<ModelModule<ModelDescriptor>> iter = usedLayersModels.iterator();
                    if( iter.hasNext() )
                    {
                        clazz = iter.next().model().type();

                        if( iter.hasNext() )
                        {
                            // Ambiguous exception
                            throw new ClassNotFoundException( name, new AmbiguousTypeException( "More than one model matches the classname "+name+":"+ Iterables.toList( usedLayersModels )) );
                        }
                    }
                }

                if( clazz == null )
                {
                    throw new ClassNotFoundException( name );
                }
                classes.put( name, clazz );
            }

            return clazz;
        }
    }

    /**
     * Check if the list of models contains several ones with the same visibility. If yes, then
     * throw an AmbiguousTypeException
     *
     * @param type   the type that was checked
     * @param models
     * @param <T>
     * @return
     */
    private <T extends ModelDescriptor> Iterable<ModelModule<T>> ambiguousCheck( final Class type, final Iterable<ModelModule<T>> models )
    {
        return new Iterable<ModelModule<T>>()
        {
            @Override
            public Iterator<ModelModule<T>> iterator()
            {
                ModelModule current = null;
                List<ModelModule<T>> ambiguous = null;
                List<ModelModule<T>> results = new ArrayList<ModelModule<T>>();

                for( ModelModule<T> model : models )
                {
                    if( current != null && !model.equals(current) )
                    {
                        if( model.model().visibility() == current.model().visibility() )
                        {
                            if (ambiguous == null)
                                ambiguous = new ArrayList<ModelModule<T>>();
                            ambiguous.add( model );
                        }
                    } else
                    {
                        current = model;
                    }

                    results.add( model );
                }

                if (ambiguous != null)
                {
                    // Check if we had any ambiguities
                    ambiguous.add( current );
                    throw new AmbiguousTypeException( "More than one type matches "+type.getName()+":"+ambiguous);
                }

                // Ambiguity check done, and no ambiguities found. Return results
                return results.iterator();
            }
        };
    }

    public Specification<ModelDescriptor> modelTypeSpecification( final String className)
    {
        return new Specification<ModelDescriptor>()
        {
            @Override
            public boolean satisfiedBy( ModelDescriptor item )
            {
                return item.type().getName().equals(className);
            }
        };
    }

    public Specification<ModelDescriptor> exactTypeSpecification( final Class type)
    {
        return new Specification<ModelDescriptor>()
        {
            @Override
            public boolean satisfiedBy( ModelDescriptor item )
            {
                return item.type().equals(type);
            }
        };
    }

    public Specification<ModelDescriptor> assignableTypeSpecification( final Class type)
    {
        return new Specification<ModelDescriptor>()
        {
            @Override
            public boolean satisfiedBy( ModelDescriptor item )
            {
                return !type.equals( item.type() ) && type.isAssignableFrom( item.type());
            }
        };
    }
}
