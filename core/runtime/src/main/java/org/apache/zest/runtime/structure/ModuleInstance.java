/*
 * Copyright (c) 2008-2012, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2012, Kent Sølvsten. All Rights Reserved.
 * Copyright (c) 2008-2013, Niclas Hedhman. All Rights Reserved.
 * Copyright (c) 2012-2015, Paul Merlin. All Rights Reserved.
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
package org.apache.zest.runtime.structure;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.apache.zest.api.activation.Activation;
import org.apache.zest.api.activation.ActivationEventListener;
import org.apache.zest.api.activation.ActivationException;
import org.apache.zest.api.activation.PassivationException;
import org.apache.zest.api.association.AssociationDescriptor;
import org.apache.zest.api.common.ConstructionException;
import org.apache.zest.api.common.Visibility;
import org.apache.zest.api.composite.AmbiguousTypeException;
import org.apache.zest.api.composite.Composite;
import org.apache.zest.api.composite.ModelDescriptor;
import org.apache.zest.api.composite.NoSuchTransientException;
import org.apache.zest.api.composite.TransientBuilder;
import org.apache.zest.api.composite.TransientDescriptor;
import org.apache.zest.api.entity.EntityComposite;
import org.apache.zest.api.entity.EntityDescriptor;
import org.apache.zest.api.entity.EntityReference;
import org.apache.zest.api.entity.IdentityGenerator;
import org.apache.zest.api.metrics.MetricsProvider;
import org.apache.zest.api.object.NoSuchObjectException;
import org.apache.zest.api.object.ObjectDescriptor;
import org.apache.zest.api.property.Property;
import org.apache.zest.api.property.PropertyDescriptor;
import org.apache.zest.api.query.QueryBuilder;
import org.apache.zest.api.query.QueryBuilderFactory;
import org.apache.zest.api.service.ImportedServiceDescriptor;
import org.apache.zest.api.service.NoSuchServiceException;
import org.apache.zest.api.service.ServiceDescriptor;
import org.apache.zest.api.service.ServiceReference;
import org.apache.zest.api.structure.Module;
import org.apache.zest.api.unitofwork.UnitOfWork;
import org.apache.zest.api.unitofwork.UnitOfWorkException;
import org.apache.zest.api.unitofwork.UnitOfWorkFactory;
import org.apache.zest.api.usecase.Usecase;
import org.apache.zest.api.util.NullArgumentException;
import org.apache.zest.api.value.NoSuchValueException;
import org.apache.zest.api.value.ValueBuilder;
import org.apache.zest.api.value.ValueComposite;
import org.apache.zest.api.value.ValueDescriptor;
import org.apache.zest.api.value.ValueSerialization;
import org.apache.zest.api.value.ValueSerializationException;
import org.apache.zest.runtime.activation.ActivationDelegate;
import org.apache.zest.runtime.composite.FunctionStateResolver;
import org.apache.zest.runtime.composite.StateResolver;
import org.apache.zest.runtime.composite.TransientBuilderInstance;
import org.apache.zest.runtime.composite.TransientModel;
import org.apache.zest.runtime.composite.TransientStateInstance;
import org.apache.zest.runtime.composite.TransientsModel;
import org.apache.zest.runtime.composite.UsesInstance;
import org.apache.zest.runtime.entity.EntitiesModel;
import org.apache.zest.runtime.entity.EntityInstance;
import org.apache.zest.runtime.entity.EntityModel;
import org.apache.zest.runtime.injection.InjectionContext;
import org.apache.zest.runtime.object.ObjectModel;
import org.apache.zest.runtime.object.ObjectsModel;
import org.apache.zest.runtime.property.PropertyInstance;
import org.apache.zest.runtime.query.QueryBuilderFactoryImpl;
import org.apache.zest.runtime.service.ImportedServicesInstance;
import org.apache.zest.runtime.service.ImportedServicesModel;
import org.apache.zest.runtime.service.ServicesInstance;
import org.apache.zest.runtime.service.ServicesModel;
import org.apache.zest.runtime.unitofwork.UnitOfWorkInstance;
import org.apache.zest.runtime.value.ValueBuilderInstance;
import org.apache.zest.runtime.value.ValueBuilderWithPrototype;
import org.apache.zest.runtime.value.ValueBuilderWithState;
import org.apache.zest.runtime.value.ValueInstance;
import org.apache.zest.runtime.value.ValueModel;
import org.apache.zest.runtime.value.ValuesModel;
import org.apache.zest.spi.entitystore.EntityStore;
import org.apache.zest.spi.metrics.MetricsProviderAdapter;
import org.apache.zest.spi.module.ModelModule;
import org.apache.zest.spi.module.ModuleSpi;
import org.apache.zest.valueserialization.orgjson.OrgJsonValueSerialization;

import static java.util.stream.Stream.concat;
import static org.apache.zest.api.util.Classes.RAW_CLASS;
import static org.apache.zest.api.util.Classes.modelTypeSpecification;
import static org.apache.zest.functional.Iterables.iterable;
import static org.apache.zest.runtime.legacy.Specifications.translate;

/**
 * Instance of a Zest Module. Contains the various composites for this Module.
 */
public class ModuleInstance
    implements Module, ModuleSpi, Activation
{
    // Constructor parameters
    private final ModuleModel model;
    private final LayerInstance layer;
    private final TransientsModel transients;
    private final ValuesModel values;
    private final ObjectsModel objects;
    private final EntitiesModel entities;
    private final ServicesInstance services;
    private final ImportedServicesInstance importedServices;
    // Eager instance objects
    private final ActivationDelegate activation;
    private final TypeLookup typeLookup;
    private final QueryBuilderFactory queryBuilderFactory;
    private final ClassLoader classLoader;
    private final EntityFunction entityFunction;
    // Lazy assigned on accessors
    private EntityStore store;
    private IdentityGenerator generator;
    private ValueSerialization valueSerialization;
    private MetricsProvider metrics;

    @SuppressWarnings( "LeakingThisInConstructor" )
    public ModuleInstance( ModuleModel moduleModel, LayerInstance layerInstance, TransientsModel transientsModel,
                           EntitiesModel entitiesModel, ObjectsModel objectsModel, ValuesModel valuesModel,
                           ServicesModel servicesModel, ImportedServicesModel importedServicesModel
    )
    {
        // Constructor parameters
        model = moduleModel;
        layer = layerInstance;
        transients = transientsModel;
        values = valuesModel;
        objects = objectsModel;
        entities = entitiesModel;
        services = servicesModel.newInstance( this );
        importedServices = importedServicesModel.newInstance( this );

        // Eager instance objects
        activation = new ActivationDelegate( this );
        typeLookup = new TypeLookup( this );
        queryBuilderFactory = new QueryBuilderFactoryImpl( this );
        classLoader = new ModuleClassLoader( this, Thread.currentThread().getContextClassLoader() );
        entityFunction = new EntityFunction( this );

        // Activation
        services.registerActivationEventListener( activation );
        importedServices.registerActivationEventListener( activation );
    }

    @Override
    public String toString()
    {
        return model.toString();
    }

    // Implementation of Module
    @Override
    public String name()
    {
        return model.name();
    }

    @Override
    public ClassLoader classLoader()
    {
        return classLoader;
    }

    @Override
    public EntityDescriptor entityDescriptor( String name )
    {
        try
        {
            Class<?> type = classLoader().loadClass( name );
            ModelModule<EntityModel> entityModel = typeLookup.lookupEntityModel( type );
            if( entityModel == null )
            {
                return null;
            }
            return entityModel.model();
        }
        catch( ClassNotFoundException e )
        {
            return null;
        }
    }

    @Override
    public ObjectDescriptor objectDescriptor( String typeName )
    {
        try
        {
            Class<?> type = classLoader().loadClass( typeName );
            ModelModule<ObjectModel> objectModel = typeLookup.lookupObjectModel( type );
            if( objectModel == null )
            {
                return null;
            }
            return objectModel.model();
        }
        catch( ClassNotFoundException e )
        {
            return null;
        }
    }

    @Override
    public TransientDescriptor transientDescriptor( String name )
    {
        try
        {
            Class<?> type = classLoader().loadClass( name );
            ModelModule<TransientModel> transientModel = typeLookup.lookupTransientModel( type );
            if( transientModel == null )
            {
                return null;
            }
            return transientModel.model();
        }
        catch( ClassNotFoundException e )
        {
            return null;
        }
    }

    @Override
    public ValueDescriptor valueDescriptor( String name )
    {
        try
        {
            Class<?> type = classLoader().loadClass( name );
            ModelModule<ValueModel> valueModel = typeLookup.lookupValueModel( type );
            if( valueModel == null )
            {
                return null;
            }
            return valueModel.model();
        }
        catch( ClassNotFoundException e )
        {
            return null;
        }
    }

    @Override
    public Stream<? extends TransientDescriptor> transientComposites()
    {
        return transients.stream();
    }

    @Override
    public Stream<? extends ValueDescriptor> valueComposites()
    {
        return values.stream();
    }

    @Override
    public Stream<? extends ServiceDescriptor> serviceComposites()
    {
        return services.stream();
    }

    @Override
    public Stream<? extends EntityDescriptor> entityComposites()
    {
        return entities.stream();
    }

    @Override
    public Stream<? extends ImportedServiceDescriptor> importedServices()
    {
        return importedServices.stream();
    }

    @Override
    public Stream<? extends ObjectDescriptor> objects()
    {
        return objects.stream();
    }

    // Implementation of MetaInfoHolder
    @Override
    public <T> T metaInfo( Class<T> infoType )
    {
        return model.metaInfo( infoType );
    }

    // Implementation of ObjectFactory
    @Override
    public <T> T newObject( Class<T> mixinType, Object... uses )
        throws NoSuchObjectException
    {
        NullArgumentException.validateNotNull( "mixinType", mixinType );
        ModelModule<ObjectModel> modelModule = typeLookup.lookupObjectModel( mixinType );

        if( modelModule == null )
        {
            throw new NoSuchObjectException( mixinType.getName(), name() );
        }

        InjectionContext injectionContext = new InjectionContext( modelModule.module(), UsesInstance.EMPTY_USES.use( uses ) );
        return mixinType.cast( modelModule.model().newInstance( injectionContext ) );
    }

    @Override
    public void injectTo( Object instance, Object... uses )
        throws ConstructionException
    {
        NullArgumentException.validateNotNull( "instance", instance );
        ModelModule<ObjectModel> modelModule = typeLookup.lookupObjectModel( instance.getClass() );

        if( modelModule == null )
        {
            throw new NoSuchObjectException( instance.getClass().getName(), name() );
        }

        InjectionContext injectionContext = new InjectionContext( modelModule.module(), UsesInstance.EMPTY_USES.use( uses ) );
        modelModule.model().inject( injectionContext, instance );
    }

    // Implementation of TransientBuilderFactory
    @Override
    public <T> TransientBuilder<T> newTransientBuilder( Class<T> mixinType )
        throws NoSuchTransientException
    {
        NullArgumentException.validateNotNull( "mixinType", mixinType );
        ModelModule<TransientModel> modelModule = typeLookup.lookupTransientModel( mixinType );

        if( modelModule == null )
        {
            throw new NoSuchTransientException( mixinType.getName(), name() );
        }

        Map<AccessibleObject, Property<?>> properties = new HashMap<>();
        modelModule.model().state().properties().forEach( propertyModel ->
                                                          {
                                                              Property<?> property = new PropertyInstance<>( propertyModel
                                                                                                                 .getBuilderInfo(),
                                                                                                             propertyModel
                                                                                                                 .initialValue( modelModule
                                                                                                                                    .module() ) );
                                                              properties.put( propertyModel.accessor(), property );
                                                          } );

        TransientStateInstance state = new TransientStateInstance( properties );

        return new TransientBuilderInstance<>( modelModule, state, UsesInstance.EMPTY_USES );
    }

    @Override
    public <T> T newTransient( final Class<T> mixinType, Object... uses )
        throws NoSuchTransientException, ConstructionException
    {
        return newTransientBuilder( mixinType ).use( uses ).newInstance();
    }

    // Implementation of ValueBuilderFactory
    @Override
    public <T> T newValue( Class<T> mixinType )
        throws NoSuchValueException, ConstructionException
    {
        return newValueBuilder( mixinType ).newInstance();
    }

    @Override
    public <T> ValueBuilder<T> newValueBuilder( Class<T> mixinType )
        throws NoSuchValueException
    {
        NullArgumentException.validateNotNull( "mixinType", mixinType );
        ModelModule<ValueModel> compositeModelModule = typeLookup.lookupValueModel( mixinType );

        if( compositeModelModule == null )
        {
            throw new NoSuchValueException( mixinType.getName(), name() );
        }

        StateResolver stateResolver = new InitialStateResolver( compositeModelModule.module() );
        return new ValueBuilderInstance<>( compositeModelModule, this, stateResolver );
    }

    @Override
    public <T> ValueBuilder<T> newValueBuilderWithState( Class<T> mixinType,
                                                         Function<PropertyDescriptor, Object> propertyFunction,
                                                         Function<AssociationDescriptor, EntityReference> associationFunction,
                                                         Function<AssociationDescriptor, Iterable<EntityReference>> manyAssociationFunction,
                                                         Function<AssociationDescriptor, Map<String, EntityReference>> namedAssociationFunction
    )
    {
        NullArgumentException.validateNotNull( "propertyFunction", propertyFunction );
        NullArgumentException.validateNotNull( "associationFunction", associationFunction );
        NullArgumentException.validateNotNull( "manyAssociationFunction", manyAssociationFunction );
        NullArgumentException.validateNotNull( "namedAssociationFunction", namedAssociationFunction );

        ModelModule<ValueModel> compositeModelModule = typeLookup.lookupValueModel( mixinType );

        if( compositeModelModule == null )
        {
            throw new NoSuchValueException( mixinType.getName(), name() );
        }

        StateResolver stateResolver = new FunctionStateResolver(
            propertyFunction, associationFunction, manyAssociationFunction, namedAssociationFunction
        );
        return new ValueBuilderWithState<>( compositeModelModule, this, stateResolver );
    }

    private static class InitialStateResolver
        implements StateResolver
    {
        private final Module module;

        private InitialStateResolver( Module module )
        {
            this.module = module;
        }

        @Override
        public Object getPropertyState( PropertyDescriptor propertyDescriptor )
        {
            return propertyDescriptor.initialValue( module );
        }

        @Override
        public EntityReference getAssociationState( AssociationDescriptor associationDescriptor )
        {
            return null;
        }

        @Override
        public List<EntityReference> getManyAssociationState( AssociationDescriptor associationDescriptor )
        {
            return new ArrayList<>();
        }

        @Override
        public Map<String, EntityReference> getNamedAssociationState( AssociationDescriptor associationDescriptor )
        {
            return new HashMap<>();
        }
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public <T> ValueBuilder<T> newValueBuilderWithPrototype( T prototype )
    {
        NullArgumentException.validateNotNull( "prototype", prototype );

        ValueInstance valueInstance = ValueInstance.valueInstanceOf( (ValueComposite) prototype );
        Class<Composite> valueType = (Class<Composite>) valueInstance.types().findFirst().orElse( null );

        ModelModule<ValueModel> modelModule = typeLookup.lookupValueModel( valueType );

        if( modelModule == null )
        {
            throw new NoSuchValueException( valueType.getName(), name() );
        }

        return new ValueBuilderWithPrototype<>( modelModule, this, prototype );
    }

    @Override
    public <T> T newValueFromSerializedState( Class<T> mixinType, String serializedState )
        throws NoSuchValueException, ConstructionException
    {
        NullArgumentException.validateNotNull( "mixinType", mixinType );
        ModelModule<ValueModel> modelModule = typeLookup.lookupValueModel( mixinType );

        if( modelModule == null )
        {
            throw new NoSuchValueException( mixinType.getName(), name() );
        }

        try
        {
            return valueSerialization().deserialize( modelModule.model().valueType(), serializedState );
        }
        catch( ValueSerializationException ex )
        {
            throw new ConstructionException( "Could not create value from serialized state", ex );
        }
    }

    // Implementation of UnitOfWorkFactory
    @Override
    public UnitOfWork newUnitOfWork()
    {
        return newUnitOfWork( Usecase.DEFAULT );
    }

    @Override
    public UnitOfWork newUnitOfWork( long currentTime )
    {
        return newUnitOfWork( Usecase.DEFAULT, currentTime );
    }

    @Override
    public UnitOfWork newUnitOfWork( Usecase usecase )
    {
        return newUnitOfWork( usecase == null ? Usecase.DEFAULT : usecase, System.currentTimeMillis() );
    }

    @Override
    public UnitOfWork newUnitOfWork( Usecase usecase, long currentTime )
    {
        UnitOfWorkInstance unitOfWorkInstance = new UnitOfWorkInstance( usecase, currentTime, metricsProvider() );
        return new ModuleUnitOfWork( ModuleInstance.this, unitOfWorkInstance );
    }

    @Override
    public boolean isUnitOfWorkActive()
    {
        Stack<UnitOfWorkInstance> stack = UnitOfWorkInstance.getCurrent();
        return !stack.isEmpty();
    }

    @Override
    public UnitOfWork currentUnitOfWork()
    {
        Stack<UnitOfWorkInstance> stack = UnitOfWorkInstance.getCurrent();
        if( stack.size() == 0 )
        {
            throw new IllegalStateException( "No current UnitOfWork active" );
        }
        return new ModuleUnitOfWork( ModuleInstance.this, stack.peek() );
    }

    @Override
    public UnitOfWork getUnitOfWork( EntityComposite entity )
    {
        EntityInstance instance = EntityInstance.entityInstanceOf( entity );
        return instance.unitOfWork();
    }

    // Implementation of QueryBuilderFactory
    @Override
    public <T> QueryBuilder<T> newQueryBuilder( final Class<T> resultType )
    {
        return queryBuilderFactory.newQueryBuilder( resultType );
    }

    // Implementation of ServiceFinder
    @Override
    public <T> ServiceReference<T> findService( Class<T> serviceType )
    {
        return typeLookup.lookupServiceReference( (Type) serviceType );
    }

    @Override
    public <T> ServiceReference<T> findService( Type serviceType )
    {
        return typeLookup.lookupServiceReference( serviceType );
    }

    @Override
    public <T> Iterable<ServiceReference<T>> findServices( Class<T> serviceType )
    {
        return typeLookup.lookupServiceReferences( (Type) serviceType );
    }

    @Override
    public <T> Iterable<ServiceReference<T>> findServices( Type serviceType )
    {
        return typeLookup.lookupServiceReferences( serviceType );
    }

    // Implementation of Activation
    @Override
    @SuppressWarnings( "unchecked" )
    public void activate()
        throws ActivationException
    {
        activation.activate( model.newActivatorsInstance(), iterable( services, importedServices ) );
    }

    @Override
    public void passivate()
        throws PassivationException
    {
        activation.passivate();
    }

    @Override
    public void registerActivationEventListener( ActivationEventListener listener )
    {
        activation.registerActivationEventListener( listener );
    }

    @Override
    public void deregisterActivationEventListener( ActivationEventListener listener )
    {
        activation.deregisterActivationEventListener( listener );
    }

    // Other methods
    /* package */ ModuleModel model()
    {
        return model;
    }

    public LayerInstance layerInstance()
    {
        return layer;
    }

    public TypeLookup typeLookup()
    {
        return typeLookup;
    }

    public BiFunction<EntityReference, Type, Object> getEntityFunction()
    {
        return entityFunction;
    }

    private static class EntityFunction
        implements BiFunction<EntityReference, Type, Object>
    {

        private final UnitOfWorkFactory uowf;

        private EntityFunction( UnitOfWorkFactory uowf )
        {
            this.uowf = uowf;
        }

        @Override
        public Object apply( EntityReference entityReference, Type type )
        {
            return uowf.currentUnitOfWork().get( RAW_CLASS.apply( type ), entityReference.identity() );
        }
    }

    public EntityStore entityStore()
    {
        synchronized( this )
        {
            if( store == null )
            {
                ServiceReference<EntityStore> service = findService( EntityStore.class );
                if( service == null )
                {
                    throw new UnitOfWorkException( "No EntityStore service available in module " + name() );
                }
                store = service.get();
            }
        }
        return store;
    }

    public IdentityGenerator identityGenerator()
    {
        synchronized( this )
        {
            if( generator == null )
            {
                ServiceReference<IdentityGenerator> service = findService( IdentityGenerator.class );
                generator = service.get();
            }
            return generator;
        }
    }

    public ValueSerialization valueSerialization()
    {
        synchronized( this )
        {
            if( valueSerialization == null )
            {
                try
                {
                    ServiceReference<ValueSerialization> service = findService( ValueSerialization.class );
                    valueSerialization = service.get();
                }
                catch( NoSuchServiceException e )
                {
                    valueSerialization = new OrgJsonValueSerialization( layer.applicationInstance(), this, this );
                }
            }
        }
        return valueSerialization;
    }

    /* package */ MetricsProvider metricsProvider()
    {
        synchronized( this )
        {
            if( metrics == null )
            {
                try
                {
                    ServiceReference<MetricsProvider> service = findService( MetricsProvider.class );
                    metrics = service.get();
                }
                catch( NoSuchServiceException e )
                {
                    metrics = new MetricsProviderAdapter();
                }
            }
        }
        return metrics;
    }

    public Stream<ModelModule<? extends ModelDescriptor>> visibleObjects( Visibility visibility )
    {
        return objects.stream()
            .filter( new Visibilitypredicate( visibility ) )
            .map( ModelModule.<ObjectDescriptor>modelModuleFunction( this ) );
    }

    public Stream<ModelModule<? extends ModelDescriptor>> visibleTransients( Visibility visibility )
    {
        return transients.models()
            .filter( new Visibilitypredicate( visibility ) )
            .map( ModelModule.<TransientDescriptor>modelModuleFunction( this ) );
    }

    public Stream<ModelModule<? extends ModelDescriptor>> visibleEntities( Visibility visibility )
    {
        return entities.models()
            .filter( new Visibilitypredicate( visibility ) )
            .map( ModelModule.<EntityDescriptor>modelModuleFunction( this ) );
    }

    public Stream<ModelModule<? extends ModelDescriptor>> visibleValues( Visibility visibility )
    {
        return values.models()
            .filter( new Visibilitypredicate( visibility ) )
            .map( ModelModule.<ValueDescriptor>modelModuleFunction( this ) );
    }

    public Stream<ServiceReference<?>> visibleServices( Visibility visibility )
    {
        return concat( services.visibleServices( visibility ),
                       importedServices.visibleServices( visibility ) );
    }

    // Module ClassLoader
    private static class ModuleClassLoader
        extends ClassLoader
    {

        private final ModuleInstance moduleInstance;
        private final Map<String, Class<?>> classes = new ConcurrentHashMap<>();

        private ModuleClassLoader( ModuleInstance moduleInstance, ClassLoader classLoader )
        {
            super( classLoader );
            this.moduleInstance = moduleInstance;
        }

        @Override
        protected Class<?> findClass( String name )
            throws ClassNotFoundException
        {
            Class<?> clazz = classes.get( name );
            if( clazz == null )
            {
                Predicate<ModelDescriptor> modelTypeSpecification = modelTypeSpecification( name );
                Predicate<ModelModule<? extends ModelDescriptor>> translation = translate( ModelModule.modelFunction(), modelTypeSpecification );
                Stream<ModelModule<? extends ModelDescriptor>> moduleModels = concat(
                    moduleInstance.visibleObjects( Visibility.module ),
                    concat(
                        moduleInstance.visibleEntities( Visibility.module ),
                        concat(
                            moduleInstance.visibleTransients( Visibility.module ),
                            moduleInstance.visibleValues( Visibility.module )
                        )
                    )
                ).filter( translation );

                Iterator<ModelModule<? extends ModelDescriptor>> moduleModelsIter = moduleModels.iterator();
                if( moduleModelsIter.hasNext() )
                {
                    clazz = moduleModelsIter.next().model().types().findFirst().orElse( null );

                    if( moduleModelsIter.hasNext() )
                    {
                        // Ambiguous exception
                        throw new ClassNotFoundException(
                            name,
                            new AmbiguousTypeException(
                                "More than one model matches the classname " + name + ":" + moduleModelsIter.next()
                            )
                        );
                    }
                }

                // Check layer
                if( clazz == null )
                {
                    Stream<ModelModule<? extends ModelDescriptor>> modelsInLayer1 = concat(
                        moduleInstance.layerInstance().visibleObjects( Visibility.layer ),
                        concat(
                            moduleInstance.layerInstance().visibleEntities( Visibility.layer ),
                            concat(
                                moduleInstance.layerInstance().visibleTransients( Visibility.layer ),
                                moduleInstance.layerInstance().visibleValues( Visibility.layer )
                            )
                        )
                    );
                    // TODO: What does this actually represents?? Shouldn't 'application' visible models already be handed back from lasyerInstance().visibleXyz() ??
                    Stream<ModelModule<? extends ModelDescriptor>> modelsInLayer2 = concat(
                        moduleInstance.layerInstance().visibleObjects( Visibility.application ),
                        concat(
                            moduleInstance.layerInstance().visibleEntities( Visibility.application ),
                            concat(
                                moduleInstance.layerInstance().visibleTransients( Visibility.application ),
                                moduleInstance.layerInstance().visibleValues( Visibility.application )
                            )
                        )
                    );
                    Stream<ModelModule<? extends ModelDescriptor>> layerModels = concat(
                        modelsInLayer1,
                        modelsInLayer2
                    ).filter( translation );

                    Iterator<ModelModule<? extends ModelDescriptor>> layerModelsIter = layerModels.iterator();
                    if( layerModelsIter.hasNext() )
                    {
                        clazz = layerModelsIter.next().model().types().findFirst().orElse( null );

                        if( layerModelsIter.hasNext() )
                        {
                            // Ambiguous exception
                            throw new ClassNotFoundException(
                                name,
                                new AmbiguousTypeException(
                                    "More than one model matches the classname " + name + ":" + layerModelsIter.next() )
                            );
                        }
                    }
                }

                // Check used layers
                if( clazz == null )
                {
                    Stream<ModelModule<? extends ModelDescriptor>> usedLayersModels = concat(
                        moduleInstance.layerInstance().usedLayersInstance().visibleObjects(),
                        concat(
                            moduleInstance.layerInstance().usedLayersInstance().visibleEntities(),
                            concat(
                                moduleInstance.layerInstance().usedLayersInstance().visibleTransients(),
                                moduleInstance.layerInstance().usedLayersInstance().visibleValues()
                            )
                        )
                    ).filter( translation );

                    Iterator<ModelModule<? extends ModelDescriptor>> usedLayersModelsIter = usedLayersModels.iterator();
                    if( usedLayersModelsIter.hasNext() )
                    {
                        clazz = usedLayersModelsIter.next().model().types().findFirst().orElse( null );

                        if( usedLayersModelsIter.hasNext() )
                        {
                            // Ambiguous exception
                            throw new ClassNotFoundException(
                                name,
                                new AmbiguousTypeException(
                                    "More than one model matches the classname " + name + ":" + usedLayersModelsIter.next()
                                )
                            );
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

    public Stream<ModelModule<? extends ModelDescriptor>> findVisibleValueTypes()
    {
        return concat( visibleValues( Visibility.module ),
                       concat(
                           layerInstance().visibleValues( Visibility.layer ),
                           concat(
                               layerInstance().visibleValues( Visibility.application ),
                               layerInstance().usedLayersInstance().visibleValues()
                           )
                       )
        );
    }

    public Stream<ModelModule<? extends ModelDescriptor>> findVisibleEntityTypes()
    {
        return concat( visibleEntities( Visibility.module ),
                       concat(
                           layerInstance().visibleEntities( Visibility.layer ),
                           concat(
                               layerInstance().visibleEntities( Visibility.application ),
                               layerInstance().usedLayersInstance().visibleEntities()
                           )
                       )
        );
    }

    public Stream<ModelModule<? extends ModelDescriptor>> findVisibleTransientTypes()
    {
        return concat( visibleTransients( Visibility.module ),
                       concat(
                           layerInstance().visibleTransients( Visibility.layer ),
                           concat(
                               layerInstance().visibleTransients( Visibility.application ),
                               layerInstance().usedLayersInstance().visibleTransients()
                           )
                       )
        );
    }

    public Stream<ServiceReference<?>> findVisibleServiceTypes()
    {
        return concat( visibleServices( Visibility.module ),
                       concat(
                           layerInstance().visibleServices( Visibility.layer ),
                           concat(
                               layerInstance().visibleServices( Visibility.application ),
                               layerInstance().usedLayersInstance().visibleServices()
                           )
                       )
        );
    }

    public Stream<ModelModule<? extends ModelDescriptor>> findVisibleObjectTypes()
    {
        return concat( visibleObjects( Visibility.module ),
                       concat(
                           layerInstance().visibleObjects( Visibility.layer ),
                           concat(
                               layerInstance().visibleObjects( Visibility.application ),
                               layerInstance().usedLayersInstance().visibleObjects()
                           )
                       )
        );
    }
}
